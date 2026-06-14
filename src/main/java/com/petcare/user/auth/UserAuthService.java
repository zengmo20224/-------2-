package com.petcare.user.auth;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.petcare.common.exception.BusinessException;
import com.petcare.common.exception.ErrorCode;
import com.petcare.common.security.JwtTokenService;
import com.petcare.user.dto.*;
import com.petcare.user.entity.User;
import com.petcare.user.entity.UserSecurityQuestion;
import com.petcare.user.service.UserSecurityQuestionService;
import com.petcare.user.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Application service for user registration, password login, and password recovery.
 * All multi-step operations are transactional.
 */
@Service
public class UserAuthService {

    private final UserService userService;
    private final UserSecurityQuestionService securityQuestionService;
    private final JwtTokenService jwtTokenService;
    private final PasswordEncoder passwordEncoder;

    public UserAuthService(UserService userService,
                           UserSecurityQuestionService securityQuestionService,
                           JwtTokenService jwtTokenService,
                           PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.securityQuestionService = securityQuestionService;
        this.jwtTokenService = jwtTokenService;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Register a new user with phone, password, and security questions.
     * Security questions must be chosen from preset list; no duplicates allowed.
     */
    @Transactional
    public PasswordLoginResponse register(RegisterRequest request) {
        // Check phone uniqueness
        long existing = userService.count(
                new LambdaQueryWrapper<User>().eq(User::getPhone, request.phone())
        );
        if (existing > 0) {
            throw new BusinessException(ErrorCode.PHONE_ALREADY_REGISTERED, "该手机号已注册");
        }

        // Validate security questions: at least 2, from preset list, no duplicate indices
        if (request.securityQuestions() == null || request.securityQuestions().size() < 2) {
            throw new BusinessException(ErrorCode.BUSINESS_RULE_VIOLATION, "请至少选择 2 个安全问题");
        }
        Set<Integer> seenIndices = new HashSet<>();
        for (RegisterRequest.SecurityQuestionItem item : request.securityQuestions()) {
            int idx = item.questionIndex();
            if (idx < 0 || idx >= PresetSecurityQuestions.QUESTIONS.size()) {
                throw new BusinessException(ErrorCode.BUSINESS_RULE_VIOLATION, "安全问题选择无效");
            }
            if (!seenIndices.add(idx)) {
                throw new BusinessException(ErrorCode.BUSINESS_RULE_VIOLATION, "安全问题不能重复");
            }
        }

        // Create user
        User user = new User();
        user.setPhone(request.phone());
        user.setNickname(request.nickname());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setStatus("ACTIVE");
        userService.save(user);

        // Save security questions (resolved from preset list by index)
        List<UserSecurityQuestion> questions = request.securityQuestions().stream()
                .map(item -> {
                    UserSecurityQuestion q = new UserSecurityQuestion();
                    q.setUserId(user.getId());
                    q.setQuestion(PresetSecurityQuestions.QUESTIONS.get(item.questionIndex()));
                    q.setAnswerHash(passwordEncoder.encode(item.answer().trim().toLowerCase()));
                    return q;
                })
                .toList();
        for (int i = 0; i < questions.size(); i++) {
            questions.get(i).setSort(i);
        }
        securityQuestionService.saveBatch(questions);

        String token = jwtTokenService.signUserToken(user.getId());

        return new PasswordLoginResponse(
                "Bearer",
                token,
                jwtTokenService.getExpirationSeconds(),
                new PasswordLoginResponse.UserInfo(
                        String.valueOf(user.getId()),
                        user.getNickname()
                )
        );
    }

    /**
     * Login with phone + password.
     */
    public PasswordLoginResponse login(PasswordLoginRequest request) {
        User user = userService.getOne(
                new LambdaQueryWrapper<User>().eq(User::getPhone, request.phone())
        );

        if (user == null || !user.getStatus().equals("ACTIVE") || user.getPasswordHash() == null) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS, "手机号或密码不正确");
        }

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS, "手机号或密码不正确");
        }

        // Update last login time
        user.setLastLoginTime(LocalDateTime.now());
        userService.updateById(user);

        String token = jwtTokenService.signUserToken(user.getId());

        return new PasswordLoginResponse(
                "Bearer",
                token,
                jwtTokenService.getExpirationSeconds(),
                new PasswordLoginResponse.UserInfo(
                        String.valueOf(user.getId()),
                        user.getNickname()
                )
        );
    }

    /**
     * Get security questions for a phone number (step 1 of password recovery).
     * Returns question text and IDs — never returns answers.
     */
    public List<SecurityQuestionView> getSecurityQuestions(ForgotPasswordQuestionsRequest request) {
        User user = userService.getOne(
                new LambdaQueryWrapper<User>().eq(User::getPhone, request.phone())
        );

        if (user == null) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS, "该手机号未注册");
        }

        List<UserSecurityQuestion> questions = securityQuestionService.list(
                new LambdaQueryWrapper<UserSecurityQuestion>()
                        .eq(UserSecurityQuestion::getUserId, user.getId())
                        .orderByAsc(UserSecurityQuestion::getSort)
        );

        if (questions.isEmpty()) {
            throw new BusinessException(ErrorCode.SECURITY_QUESTION_NOT_SET, "该账号未设置安全问题");
        }

        return questions.stream()
                .map(q -> new SecurityQuestionView(q.getId(), q.getQuestion()))
                .toList();
    }

    /**
     * Reset password after verifying security question answers.
     */
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        User user = userService.getOne(
                new LambdaQueryWrapper<User>().eq(User::getPhone, request.phone())
        );

        if (user == null) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS, "该手机号未注册");
        }

        // Verify all answers
        for (ResetPasswordRequest.AnswerItem answer : request.answers()) {
            UserSecurityQuestion question = securityQuestionService.getById(Long.parseLong(answer.questionId()));
            if (question == null || !question.getUserId().equals(user.getId())) {
                throw new BusinessException(ErrorCode.SECURITY_ANSWER_INCORRECT, "安全问题验证失败");
            }
            if (!passwordEncoder.matches(answer.answer().trim().toLowerCase(), question.getAnswerHash())) {
                throw new BusinessException(ErrorCode.SECURITY_ANSWER_INCORRECT, "安全问题验证失败");
            }
        }

        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userService.updateById(user);
    }
}
