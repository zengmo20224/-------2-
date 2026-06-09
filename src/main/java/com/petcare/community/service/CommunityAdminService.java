package com.petcare.community.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.petcare.common.exception.BusinessException;
import com.petcare.common.exception.ErrorCode;
import com.petcare.common.pagination.PageResponse;
import com.petcare.community.domain.CommunityContentType;
import com.petcare.community.dto.AdminReportHandleRequest;
import com.petcare.community.dto.CommentResponse;
import com.petcare.community.dto.PostResponse;
import com.petcare.community.entity.Post;
import com.petcare.community.entity.PostComment;
import com.petcare.community.entity.PostReport;
import com.petcare.community.mapper.PostCommentMapper;
import com.petcare.community.mapper.PostMapper;
import com.petcare.community.mapper.PostReportMapper;
import com.petcare.moderation.service.ContentModerationService;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Admin service for community content moderation.
 * Handles post/comment review, hide, delete, and report processing.
 */
@Service
public class CommunityAdminService {

    private final PostMapper postMapper;
    private final PostCommentMapper commentMapper;
    private final PostReportMapper reportMapper;
    private final ContentModerationService moderationService;

    public CommunityAdminService(PostMapper postMapper,
                                  PostCommentMapper commentMapper,
                                  PostReportMapper reportMapper,
                                  ContentModerationService moderationService) {
        this.postMapper = postMapper;
        this.commentMapper = commentMapper;
        this.reportMapper = reportMapper;
        this.moderationService = moderationService;
    }

    // ==================== Post Management ====================

    /**
     * Lists all posts for admin (includes all statuses).
     */
    public PageResponse<PostResponse> listPosts(String status, int page, int size) {
        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<Post>()
                .eq(Post::getDeleted, 0);
        if (status != null && !status.isEmpty()) {
            wrapper.eq(Post::getStatus, status);
        }
        wrapper.orderByDesc(Post::getCreateTime);

        Page<Post> pageResult = postMapper.selectPage(new Page<>(page, size), wrapper);
        var items = pageResult.getRecords().stream()
                .map(this::toPostResponse)
                .toList();
        return PageResponse.of(items, pageResult.getTotal(), page, size);
    }

    /**
     * Gets a single post by ID for admin (any status).
     */
    public PostResponse getPost(Long id) {
        Post post = postMapper.selectOne(
                new LambdaQueryWrapper<Post>()
                        .eq(Post::getId, id)
                        .eq(Post::getDeleted, 0)
        );
        if (post == null) {
            throw new BusinessException(ErrorCode.COMMUNITY_POST_NOT_FOUND, "帖子不存在");
        }
        return toPostResponse(post);
    }

    /**
     * Approves a pending post: PENDING_REVIEW -> PUBLISHED.
     */
    @Transactional
    public PostResponse approvePost(Long adminId, Long postId, String remark) {
        Post post = getPostEntity(postId);

        if (!"PENDING_REVIEW".equals(post.getStatus())) {
            throw new BusinessException(ErrorCode.COMMUNITY_REVIEW_STATUS_INVALID,
                    "只能审核待审核状态的帖子");
        }

        post.setStatus("PUBLISHED");
        post.setPublishTime(LocalDateTime.now());
        postMapper.updateById(post);

        moderationService.approveRecord(CommunityContentType.POST, postId, adminId, remark);

        return toPostResponse(post);
    }

    /**
     * Rejects a pending post: PENDING_REVIEW -> REJECTED.
     */
    @Transactional
    public PostResponse rejectPost(Long adminId, Long postId, String remark) {
        Post post = getPostEntity(postId);

        if (!"PENDING_REVIEW".equals(post.getStatus())) {
            throw new BusinessException(ErrorCode.COMMUNITY_REVIEW_STATUS_INVALID,
                    "只能审核待审核状态的帖子");
        }

        post.setStatus("REJECTED");
        post.setRejectReason(remark);
        postMapper.updateById(post);

        moderationService.rejectRecord(CommunityContentType.POST, postId, adminId, remark);

        return toPostResponse(post);
    }

    /**
     * Hides a published post: PUBLISHED -> HIDDEN.
     */
    @Transactional
    public PostResponse hidePost(Long adminId, Long postId) {
        Post post = getPostEntity(postId);

        if (!"PUBLISHED".equals(post.getStatus())) {
            throw new BusinessException(ErrorCode.COMMUNITY_REVIEW_STATUS_INVALID,
                    "只能隐藏已发布的帖子");
        }

        post.setStatus("HIDDEN");
        postMapper.updateById(post);

        return toPostResponse(post);
    }

    /**
     * Deletes a post (logical delete).
     */
    @Transactional
    public void deletePost(Long adminId, Long postId) {
        Post post = postMapper.selectOne(
                new LambdaQueryWrapper<Post>()
                        .eq(Post::getId, postId)
                        .eq(Post::getDeleted, 0)
        );
        if (post == null) {
            throw new BusinessException(ErrorCode.COMMUNITY_POST_NOT_FOUND, "帖子不存在");
        }

        post.setDeleted(1);
        postMapper.updateById(post);
    }

    // ==================== Comment Management ====================

    /**
     * Lists all comments for admin.
     */
    public PageResponse<CommentResponse> listComments(String status, int page, int size) {
        LambdaQueryWrapper<PostComment> wrapper = new LambdaQueryWrapper<PostComment>()
                .eq(PostComment::getDeleted, 0);
        if (status != null && !status.isEmpty()) {
            wrapper.eq(PostComment::getStatus, status);
        }
        wrapper.orderByDesc(PostComment::getCreateTime);

        Page<PostComment> pageResult = commentMapper.selectPage(new Page<>(page, size), wrapper);
        var items = pageResult.getRecords().stream()
                .map(this::toCommentResponse)
                .toList();
        return PageResponse.of(items, pageResult.getTotal(), page, size);
    }

    /**
     * Approves a pending comment: PENDING_REVIEW -> PUBLISHED, increments post comment_count.
     */
    @Transactional
    public CommentResponse approveComment(Long adminId, Long commentId, String remark) {
        PostComment comment = getCommentEntity(commentId);

        if (!"PENDING_REVIEW".equals(comment.getStatus())) {
            throw new BusinessException(ErrorCode.COMMUNITY_REVIEW_STATUS_INVALID,
                    "只能审核待审核状态的评论");
        }

        comment.setStatus("PUBLISHED");
        commentMapper.updateById(comment);

        // Increment post comment count
        Post post = postMapper.selectById(comment.getPostId());
        if (post != null) {
            post.setCommentCount(post.getCommentCount() + 1);
            postMapper.updateById(post);
        }

        moderationService.approveRecord(CommunityContentType.COMMENT, commentId, adminId, remark);

        return toCommentResponse(comment);
    }

    /**
     * Rejects a pending comment: PENDING_REVIEW -> REJECTED.
     */
    @Transactional
    public CommentResponse rejectComment(Long adminId, Long commentId, String remark) {
        PostComment comment = getCommentEntity(commentId);

        if (!"PENDING_REVIEW".equals(comment.getStatus())) {
            throw new BusinessException(ErrorCode.COMMUNITY_REVIEW_STATUS_INVALID,
                    "只能审核待审核状态的评论");
        }

        comment.setStatus("REJECTED");
        commentMapper.updateById(comment);

        moderationService.rejectRecord(CommunityContentType.COMMENT, commentId, adminId, remark);

        return toCommentResponse(comment);
    }

    /**
     * Hides a published comment: PUBLISHED -> HIDDEN, decrements post comment_count.
     */
    @Transactional
    public CommentResponse hideComment(Long adminId, Long commentId) {
        PostComment comment = getCommentEntity(commentId);

        if (!"PUBLISHED".equals(comment.getStatus())) {
            throw new BusinessException(ErrorCode.COMMUNITY_REVIEW_STATUS_INVALID,
                    "只能隐藏已发布的评论");
        }

        comment.setStatus("HIDDEN");
        commentMapper.updateById(comment);

        // Decrement post comment count
        Post post = postMapper.selectById(comment.getPostId());
        if (post != null) {
            post.setCommentCount(Math.max(0, post.getCommentCount() - 1));
            postMapper.updateById(post);
        }

        return toCommentResponse(comment);
    }

    /**
     * Deletes a comment (logical delete). If was published, decrements post comment_count.
     */
    @Transactional
    public void deleteComment(Long adminId, Long commentId) {
        PostComment comment = commentMapper.selectOne(
                new LambdaQueryWrapper<PostComment>()
                        .eq(PostComment::getId, commentId)
                        .eq(PostComment::getDeleted, 0)
        );
        if (comment == null) {
            throw new BusinessException(ErrorCode.COMMUNITY_COMMENT_NOT_FOUND, "评论不存在");
        }

        // Decrement post comment count if was published
        if ("PUBLISHED".equals(comment.getStatus())) {
            Post post = postMapper.selectById(comment.getPostId());
            if (post != null) {
                post.setCommentCount(Math.max(0, post.getCommentCount() - 1));
                postMapper.updateById(post);
            }
        }

        comment.setDeleted(1);
        commentMapper.updateById(comment);
    }

    // ==================== Report Management ====================

    /**
     * Lists pending reports for admin.
     */
    public PageResponse<PostReport> listReports(String status, int page, int size) {
        LambdaQueryWrapper<PostReport> wrapper = new LambdaQueryWrapper<>();
        if (status != null && !status.isEmpty()) {
            wrapper.eq(PostReport::getStatus, status);
        }
        wrapper.orderByDesc(PostReport::getCreateTime);

        Page<PostReport> pageResult = reportMapper.selectPage(new Page<>(page, size), wrapper);
        return PageResponse.of(pageResult.getRecords(), pageResult.getTotal(), page, size);
    }

    /**
     * Handles a report. Can optionally hide the associated post.
     */
    @Transactional
    public void handleReport(Long adminId, Long reportId, AdminReportHandleRequest request) {
        PostReport report = reportMapper.selectById(reportId);
        if (report == null) {
            throw new BusinessException(ErrorCode.COMMUNITY_POST_NOT_FOUND, "举报记录不存在");
        }

        if (!"PENDING".equals(report.getStatus())) {
            throw new BusinessException(ErrorCode.COMMUNITY_REVIEW_STATUS_INVALID,
                    "该举报已被处理");
        }

        report.setStatus(request.handleResult());
        report.setHandlerId(adminId);
        report.setHandleTime(LocalDateTime.now());
        report.setHandleResult(request.handleResult());
        reportMapper.updateById(report);

        // Optionally hide the post
        if (request.hidePost()) {
            Post post = postMapper.selectOne(
                    new LambdaQueryWrapper<Post>()
                            .eq(Post::getId, report.getPostId())
                            .eq(Post::getDeleted, 0)
            );
            if (post != null && "PUBLISHED".equals(post.getStatus())) {
                post.setStatus("HIDDEN");
                postMapper.updateById(post);
            }
        }
    }

    // ==================== Private Helpers ====================

    private Post getPostEntity(Long postId) {
        Post post = postMapper.selectOne(
                new LambdaQueryWrapper<Post>()
                        .eq(Post::getId, postId)
                        .eq(Post::getDeleted, 0)
        );
        if (post == null) {
            throw new BusinessException(ErrorCode.COMMUNITY_POST_NOT_FOUND, "帖子不存在");
        }
        return post;
    }

    private PostComment getCommentEntity(Long commentId) {
        PostComment comment = commentMapper.selectOne(
                new LambdaQueryWrapper<PostComment>()
                        .eq(PostComment::getId, commentId)
                        .eq(PostComment::getDeleted, 0)
        );
        if (comment == null) {
            throw new BusinessException(ErrorCode.COMMUNITY_COMMENT_NOT_FOUND, "评论不存在");
        }
        return comment;
    }

    private PostResponse toPostResponse(Post post) {
        return new PostResponse(
                post.getId(), post.getUserId(), post.getPetId(), post.getTopicId(),
                post.getTitle(), post.getContent(), post.getStatus(),
                post.getViewCount(), post.getLikeCount(), post.getCommentCount(),
                post.getFavoriteCount(), post.getPublishTime(), post.getCreateTime()
        );
    }

    private CommentResponse toCommentResponse(PostComment comment) {
        return new CommentResponse(
                comment.getId(), comment.getPostId(), comment.getUserId(),
                comment.getParentId(), comment.getContent(), comment.getStatus(),
                comment.getLikeCount(), comment.getCreateTime()
        );
    }
}
