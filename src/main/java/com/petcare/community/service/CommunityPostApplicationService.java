package com.petcare.community.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.petcare.common.exception.BusinessException;
import com.petcare.common.exception.ErrorCode;
import com.petcare.common.pagination.PageResponse;
import com.petcare.community.domain.CommunityContentType;
import com.petcare.community.dto.CommentCreateRequest;
import com.petcare.community.dto.CommentResponse;
import com.petcare.community.dto.PostCreateRequest;
import com.petcare.community.dto.PostDetailResponse;
import com.petcare.community.dto.PostResponse;
import com.petcare.community.dto.TopicResponse;
import com.petcare.community.entity.Post;
import com.petcare.community.entity.PostComment;
import com.petcare.community.entity.PostImage;
import com.petcare.community.entity.Topic;
import com.petcare.community.mapper.PostCommentMapper;
import com.petcare.community.mapper.PostImageMapper;
import com.petcare.community.mapper.PostMapper;
import com.petcare.community.mapper.TopicMapper;
import com.petcare.moderation.dto.ContentReviewResult;
import com.petcare.moderation.service.ContentModerationService;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Application service for community post and comment operations.
 * Handles creation with moderation, listing, and detail retrieval.
 */
@Service
public class CommunityPostApplicationService {

    private final PostMapper postMapper;
    private final PostCommentMapper commentMapper;
    private final PostImageMapper imageMapper;
    private final TopicMapper topicMapper;
    private final ContentModerationService moderationService;

    public CommunityPostApplicationService(PostMapper postMapper,
                                            PostCommentMapper commentMapper,
                                            PostImageMapper imageMapper,
                                            TopicMapper topicMapper,
                                            ContentModerationService moderationService) {
        this.postMapper = postMapper;
        this.commentMapper = commentMapper;
        this.imageMapper = imageMapper;
        this.topicMapper = topicMapper;
        this.moderationService = moderationService;
    }

    // ==================== Topic Queries ====================

    /**
     * Lists all active topics ordered by sort asc, createTime desc.
     */
    public List<TopicResponse> listTopics() {
        List<Topic> topics = topicMapper.selectList(
                new LambdaQueryWrapper<Topic>()
                        .eq(Topic::getStatus, "ACTIVE")
                        .eq(Topic::getDeleted, 0)
                        .orderByAsc(Topic::getSort)
                        .orderByDesc(Topic::getCreateTime)
        );
        return topics.stream()
                .map(t -> new TopicResponse(t.getId(), t.getName(), t.getDescription(),
                        t.getSort(), t.getCreateTime()))
                .toList();
    }

    /**
     * Gets a single active topic by ID.
     */
    public TopicResponse getTopic(Long id) {
        Topic topic = topicMapper.selectOne(
                new LambdaQueryWrapper<Topic>()
                        .eq(Topic::getId, id)
                        .eq(Topic::getStatus, "ACTIVE")
                        .eq(Topic::getDeleted, 0)
        );
        if (topic == null) {
            throw new BusinessException(ErrorCode.COMMUNITY_TOPIC_NOT_FOUND, "话题不存在");
        }
        return new TopicResponse(topic.getId(), topic.getName(), topic.getDescription(),
                topic.getSort(), topic.getCreateTime());
    }

    // ==================== Post Operations ====================

    /**
     * Creates a new post with content moderation.
     * User identity comes from currentUserId parameter, not request body.
     */
    @Transactional
    public PostResponse createPost(Long currentUserId, PostCreateRequest request) {
        // Validate topic if provided
        if (request.topicId() != null) {
            Topic topic = topicMapper.selectOne(
                    new LambdaQueryWrapper<Topic>()
                            .eq(Topic::getId, request.topicId())
                            .eq(Topic::getStatus, "ACTIVE")
                            .eq(Topic::getDeleted, 0)
            );
            if (topic == null) {
                throw new BusinessException(ErrorCode.COMMUNITY_TOPIC_NOT_FOUND, "话题不存在或已禁用");
            }
        }

        Post post = new Post();
        post.setUserId(currentUserId);
        post.setPetId(request.petId());
        post.setTopicId(request.topicId());
        post.setTitle(request.title());
        post.setContent(request.content());
        post.setViewCount(0);
        post.setLikeCount(0);
        post.setCommentCount(0);
        post.setFavoriteCount(0);

        // Run content moderation
        String textToCheck = request.title() + " " + request.content();
        ContentReviewResult modResult = moderationService.moderateAndRecord(
                CommunityContentType.POST, post.getId(), currentUserId, textToCheck);

        post.setStatus(modResult.contentStatus());
        post.setRiskLevel(modResult.riskLevel());

        if ("PUBLISHED".equals(modResult.contentStatus())) {
            post.setPublishTime(LocalDateTime.now());
        }

        postMapper.insert(post);

        return toPostResponse(post);
    }

    /**
     * Lists published posts with pagination. Only shows PUBLISHED and non-deleted posts.
     */
    public PageResponse<PostResponse> listPosts(Long topicId, int page, int size) {
        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<Post>()
                .eq(Post::getStatus, "PUBLISHED")
                .eq(Post::getDeleted, 0);

        if (topicId != null) {
            wrapper.eq(Post::getTopicId, topicId);
        }
        wrapper.orderByDesc(Post::getPublishTime);

        Page<Post> pageResult = postMapper.selectPage(new Page<>(page, size), wrapper);
        List<PostResponse> items = pageResult.getRecords().stream()
                .map(this::toPostResponse)
                .toList();
        return PageResponse.of(items, pageResult.getTotal(), page, size);
    }

    /**
     * Gets post detail. Users can only see PUBLISHED posts or their own non-deleted posts.
     */
    public PostDetailResponse getPostDetail(Long currentUserId, Long postId) {
        Post post = postMapper.selectOne(
                new LambdaQueryWrapper<Post>()
                        .eq(Post::getId, postId)
                        .eq(Post::getDeleted, 0)
        );
        if (post == null) {
            throw new BusinessException(ErrorCode.COMMUNITY_POST_NOT_FOUND, "帖子不存在");
        }

        // Check visibility: published posts visible to all; non-published only to author
        if (!"PUBLISHED".equals(post.getStatus())) {
            if (currentUserId == null || !currentUserId.equals(post.getUserId())) {
                throw new BusinessException(ErrorCode.COMMUNITY_POST_NOT_VISIBLE, "帖子不可查看");
            }
        }

        List<String> imageUrls = imageMapper.selectList(
                new LambdaQueryWrapper<PostImage>()
                        .eq(PostImage::getPostId, postId)
                        .orderByAsc(PostImage::getSort)
        ).stream().map(PostImage::getImageUrl).toList();

        return new PostDetailResponse(
                post.getId(), post.getUserId(), post.getPetId(), post.getTopicId(),
                post.getTitle(), post.getContent(), post.getStatus(),
                post.getViewCount(), post.getLikeCount(), post.getCommentCount(),
                post.getFavoriteCount(), post.getPublishTime(), post.getCreateTime(),
                imageUrls
        );
    }

    // ==================== Comment Operations ====================

    /**
     * Creates a comment on a published post with content moderation.
     */
    @Transactional
    public CommentResponse createComment(Long currentUserId, Long postId,
                                          CommentCreateRequest request) {
        // Verify post exists and is published
        Post post = postMapper.selectOne(
                new LambdaQueryWrapper<Post>()
                        .eq(Post::getId, postId)
                        .eq(Post::getStatus, "PUBLISHED")
                        .eq(Post::getDeleted, 0)
        );
        if (post == null) {
            throw new BusinessException(ErrorCode.COMMUNITY_POST_NOT_FOUND, "帖子不存在或不可评论");
        }

        // Validate parent comment if provided
        if (request.parentId() != null) {
            PostComment parent = commentMapper.selectOne(
                    new LambdaQueryWrapper<PostComment>()
                            .eq(PostComment::getId, request.parentId())
                            .eq(PostComment::getPostId, postId)
                            .eq(PostComment::getDeleted, 0)
            );
            if (parent == null) {
                throw new BusinessException(ErrorCode.COMMUNITY_COMMENT_NOT_FOUND, "父评论不存在");
            }
        }

        PostComment comment = new PostComment();
        comment.setPostId(postId);
        comment.setUserId(currentUserId);
        comment.setParentId(request.parentId());
        comment.setContent(request.content());
        comment.setLikeCount(0);

        // Run content moderation
        ContentReviewResult modResult = moderationService.moderateAndRecord(
                CommunityContentType.COMMENT, comment.getId(), currentUserId, request.content());

        comment.setStatus(modResult.contentStatus());
        comment.setRiskLevel(modResult.riskLevel());

        commentMapper.insert(comment);

        // Only increment comment count if published
        if ("PUBLISHED".equals(modResult.contentStatus())) {
            post.setCommentCount(post.getCommentCount() + 1);
            postMapper.updateById(post);
        }

        return toCommentResponse(comment);
    }

    /**
     * Lists published comments for a post.
     */
    public PageResponse<CommentResponse> listComments(Long postId, int page, int size) {
        // Verify post exists
        Long postCount = postMapper.selectCount(
                new LambdaQueryWrapper<Post>()
                        .eq(Post::getId, postId)
                        .eq(Post::getDeleted, 0)
        );
        if (postCount == 0) {
            throw new BusinessException(ErrorCode.COMMUNITY_POST_NOT_FOUND, "帖子不存在");
        }

        LambdaQueryWrapper<PostComment> wrapper = new LambdaQueryWrapper<PostComment>()
                .eq(PostComment::getPostId, postId)
                .eq(PostComment::getStatus, "PUBLISHED")
                .eq(PostComment::getDeleted, 0)
                .orderByAsc(PostComment::getCreateTime);

        Page<PostComment> pageResult = commentMapper.selectPage(new Page<>(page, size), wrapper);
        List<CommentResponse> items = pageResult.getRecords().stream()
                .map(this::toCommentResponse)
                .toList();
        return PageResponse.of(items, pageResult.getTotal(), page, size);
    }

    // ==================== Private Helpers ====================

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
