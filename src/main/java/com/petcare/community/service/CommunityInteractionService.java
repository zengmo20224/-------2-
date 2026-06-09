package com.petcare.community.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.petcare.common.exception.BusinessException;
import com.petcare.common.exception.ErrorCode;
import com.petcare.community.dto.ReportPostRequest;
import com.petcare.community.entity.Post;
import com.petcare.community.entity.PostFavorite;
import com.petcare.community.entity.PostLike;
import com.petcare.community.entity.PostReport;
import com.petcare.community.mapper.PostFavoriteMapper;
import com.petcare.community.mapper.PostLikeMapper;
import com.petcare.community.mapper.PostMapper;
import com.petcare.community.mapper.PostReportMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for community interactions: likes, favorites, and reports.
 * All operations are idempotent and update counts within the same transaction.
 */
@Service
public class CommunityInteractionService {

    private final PostMapper postMapper;
    private final PostLikeMapper likeMapper;
    private final PostFavoriteMapper favoriteMapper;
    private final PostReportMapper reportMapper;

    public CommunityInteractionService(PostMapper postMapper,
                                        PostLikeMapper likeMapper,
                                        PostFavoriteMapper favoriteMapper,
                                        PostReportMapper reportMapper) {
        this.postMapper = postMapper;
        this.likeMapper = likeMapper;
        this.favoriteMapper = favoriteMapper;
        this.reportMapper = reportMapper;
    }

    // ==================== Like Operations ====================

    /**
     * Likes a published post. Idempotent — duplicate likes do not increase count.
     *
     * @param currentUserId the user performing the like
     * @param postId        the post to like
     */
    @Transactional
    public void likePost(Long currentUserId, Long postId) {
        Post post = getPublishedPost(postId);

        boolean alreadyLiked = likeMapper.exists(
                new LambdaQueryWrapper<PostLike>()
                        .eq(PostLike::getPostId, postId)
                        .eq(PostLike::getUserId, currentUserId)
        );
        if (alreadyLiked) {
            return; // Idempotent: no error, no count change
        }

        PostLike like = new PostLike();
        like.setPostId(postId);
        like.setUserId(currentUserId);
        likeMapper.insert(like);

        post.setLikeCount(post.getLikeCount() + 1);
        postMapper.updateById(post);
    }

    /**
     * Removes a like from a published post. Idempotent — no error if not liked.
     */
    @Transactional
    public void unlikePost(Long currentUserId, Long postId) {
        Post post = getPublishedPost(postId);

        PostLike existing = likeMapper.selectOne(
                new LambdaQueryWrapper<PostLike>()
                        .eq(PostLike::getPostId, postId)
                        .eq(PostLike::getUserId, currentUserId)
        );
        if (existing == null) {
            return; // Idempotent: no error
        }

        likeMapper.deleteById(existing.getId());
        post.setLikeCount(Math.max(0, post.getLikeCount() - 1));
        postMapper.updateById(post);
    }

    // ==================== Favorite Operations ====================

    /**
     * Favorites a published post. Idempotent — duplicate favorites do not increase count.
     */
    @Transactional
    public void favoritePost(Long currentUserId, Long postId) {
        Post post = getPublishedPost(postId);

        boolean alreadyFavorited = favoriteMapper.exists(
                new LambdaQueryWrapper<PostFavorite>()
                        .eq(PostFavorite::getPostId, postId)
                        .eq(PostFavorite::getUserId, currentUserId)
        );
        if (alreadyFavorited) {
            return; // Idempotent: no error, no count change
        }

        PostFavorite favorite = new PostFavorite();
        favorite.setPostId(postId);
        favorite.setUserId(currentUserId);
        favoriteMapper.insert(favorite);

        post.setFavoriteCount(post.getFavoriteCount() + 1);
        postMapper.updateById(post);
    }

    /**
     * Removes a favorite from a published post. Idempotent — no error if not favorited.
     */
    @Transactional
    public void unfavoritePost(Long currentUserId, Long postId) {
        Post post = getPublishedPost(postId);

        PostFavorite existing = favoriteMapper.selectOne(
                new LambdaQueryWrapper<PostFavorite>()
                        .eq(PostFavorite::getPostId, postId)
                        .eq(PostFavorite::getUserId, currentUserId)
        );
        if (existing == null) {
            return; // Idempotent: no error
        }

        favoriteMapper.deleteById(existing.getId());
        post.setFavoriteCount(Math.max(0, post.getFavoriteCount() - 1));
        postMapper.updateById(post);
    }

    // ==================== Report Operations ====================

    /**
     * Reports a post. Duplicate reports by the same user for the same post return 409.
     */
    @Transactional
    public void reportPost(Long currentUserId, Long postId, ReportPostRequest request) {
        // Verify post exists and not deleted
        Post post = postMapper.selectOne(
                new LambdaQueryWrapper<Post>()
                        .eq(Post::getId, postId)
                        .eq(Post::getDeleted, 0)
        );
        if (post == null) {
            throw new BusinessException(ErrorCode.COMMUNITY_POST_NOT_FOUND, "帖子不存在");
        }

        // Check for duplicate report
        boolean alreadyReported = reportMapper.exists(
                new LambdaQueryWrapper<PostReport>()
                        .eq(PostReport::getPostId, postId)
                        .eq(PostReport::getReporterId, currentUserId)
        );
        if (alreadyReported) {
            throw new BusinessException(ErrorCode.COMMUNITY_DUPLICATE_REPORT, "你已经举报过该帖子");
        }

        PostReport report = new PostReport();
        report.setPostId(postId);
        report.setReporterId(currentUserId);
        report.setReasonType(request.reasonType());
        report.setReason(request.reason());
        report.setStatus("PENDING");
        reportMapper.insert(report);
    }

    // ==================== Private Helpers ====================

    private Post getPublishedPost(Long postId) {
        Post post = postMapper.selectOne(
                new LambdaQueryWrapper<Post>()
                        .eq(Post::getId, postId)
                        .eq(Post::getStatus, "PUBLISHED")
                        .eq(Post::getDeleted, 0)
        );
        if (post == null) {
            throw new BusinessException(ErrorCode.COMMUNITY_POST_NOT_FOUND, "帖子不存在或不可操作");
        }
        return post;
    }
}
