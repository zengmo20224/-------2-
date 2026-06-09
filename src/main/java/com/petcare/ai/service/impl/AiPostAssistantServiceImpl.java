package com.petcare.ai.service.impl;

import com.petcare.ai.domain.AiOutputSafetyPolicy;
import com.petcare.ai.domain.PromptFactory;
import com.petcare.ai.dto.PostAssistantRequest;
import com.petcare.ai.dto.PostAssistantResponse;
import com.petcare.ai.entity.AiUsageLog;
import com.petcare.ai.mapper.AiUsageLogMapper;
import com.petcare.ai.provider.*;
import com.petcare.ai.service.AiPostAssistantService;
import com.petcare.common.exception.BusinessException;
import com.petcare.common.exception.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Implementation of AI post assistant service.
 * Generates suggested post drafts based only on user-provided facts.
 * Never auto-publishes or modifies post review status.
 */
@Service
public class AiPostAssistantServiceImpl implements AiPostAssistantService {

    private static final Logger log = LoggerFactory.getLogger(AiPostAssistantServiceImpl.class);

    private final AiProviderClient providerClient;
    private final AiUsageLogMapper usageLogMapper;

    public AiPostAssistantServiceImpl(
            AiProviderClient providerClient,
            AiUsageLogMapper usageLogMapper) {
        this.providerClient = providerClient;
        this.usageLogMapper = usageLogMapper;
    }

    @Override
    public PostAssistantResponse generateDraft(Long currentUserId, PostAssistantRequest request) {
        if (currentUserId == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "请先登录");
        }

        List<AiProviderMessage> messages = PromptFactory.buildPostAssistantMessages(
                request.petName(),
                request.petType(),
                request.event(),
                request.tone(),
                request.originalText()
        );

        try {
            AiProviderResponse response = providerClient.complete(
                    new AiProviderRequest(AiApiType.CONTENT_GENERATE, messages, null));

            String output = response.assistantText();

            // Check output safety
            if (AiOutputSafetyPolicy.isUnsafe(output)) {
                return new PostAssistantResponse("抱歉，生成内容未通过安全检查，请修改后重试。");
            }

            // Log successful usage
            logSuccessUsage(currentUserId, response);

            // Always return as draft
            return new PostAssistantResponse(output);
        } catch (AiProviderUnavailableException e) {
            logFailedUsage(currentUserId, "provider_unavailable");
            throw e;
        } catch (AiProviderException e) {
            logFailedUsage(currentUserId, e.getInternalCode());
            throw e;
        }
    }

    private void logSuccessUsage(Long userId, AiProviderResponse response) {
        try {
            AiUsageLog usageLog = new AiUsageLog();
            usageLog.setUserId(userId);
            usageLog.setApiType(AiApiType.CONTENT_GENERATE.name());
            usageLog.setModelName(response.modelName());
            if (response.usage() != null) {
                usageLog.setPromptTokens(response.usage().promptTokens());
                usageLog.setCompletionTokens(response.usage().completionTokens());
                usageLog.setTotalTokens(response.usage().totalTokens());
            }
            usageLog.setSuccess(1);
            usageLogMapper.insert(usageLog);
        } catch (Exception e) {
            log.warn("Failed to log AI usage: {}", e.getMessage());
        }
    }

    private void logFailedUsage(Long userId, String errorCode) {
        try {
            AiUsageLog usageLog = new AiUsageLog();
            usageLog.setUserId(userId);
            usageLog.setApiType(AiApiType.CONTENT_GENERATE.name());
            usageLog.setSuccess(0);
            usageLog.setErrorMessage(errorCode);
            usageLogMapper.insert(usageLog);
        } catch (Exception e) {
            log.warn("Failed to log AI usage: {}", e.getMessage());
        }
    }
}
