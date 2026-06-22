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
import com.petcare.notification.service.NotificationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for CommunityInteractionService.
 * Uses Mockito to mock mapper dependencies — no Spring context.
 */
@ExtendWith(MockitoExtension.class)
class CommunityInteractionServiceTest {

    @Mock
    private PostMapper postMapper;

    @Mock
    private PostLikeMapper likeMapper;

    @Mock
    private PostFavoriteMapper favoriteMapper;

    @Mock
    private PostReportMapper reportMapper;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private CommunityInteractionService service;

    private static final Long USER_ID = 1001L;
    private static final Long POST_ID = 3001L;
    private static final Long LIKE_ID = 4001L;
    private static final Long FAVORITE_ID = 5001L;

    // ==================== Like Tests ====================

    @Nested
    @DisplayName("likePost")
    class LikePostTests {

        @Test
        @DisplayName("First like: inserts like, increments like_count")
        void firstLikeInsertsAndIncrements() {
            // Arrange
            Post post = buildPublishedPost(POST_ID);
            post.setLikeCount(5);
            when(postMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(post);
            when(likeMapper.exists(any(LambdaQueryWrapper.class))).thenReturn(false);
            when(likeMapper.insert(any(PostLike.class))).thenReturn(1);

            // Act
            service.likePost(USER_ID, POST_ID);

            // Assert
            verify(likeMapper).insert(any(PostLike.class));
            verify(postMapper).updateById(post);
            assertThat(post.getLikeCount()).isEqualTo(6);
        }

        @Test
        @DisplayName("Duplicate like: no error, count unchanged (idempotent)")
        void duplicateLikeIsIdempotent() {
            // Arrange
            Post post = buildPublishedPost(POST_ID);
            post.setLikeCount(5);
            when(postMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(post);
            when(likeMapper.exists(any(LambdaQueryWrapper.class))).thenReturn(true);

            // Act
            service.likePost(USER_ID, POST_ID);

            // Assert
            verify(likeMapper, never()).insert(any(PostLike.class));
            verify(postMapper, never()).updateById(any(Post.class));
            assertThat(post.getLikeCount()).isEqualTo(5);
        }

        @Test
        @DisplayName("Concurrent duplicate like on insert remains idempotent")
        void duplicateLikeOnInsertIsIdempotent() {
            Post post = buildPublishedPost(POST_ID);
            post.setLikeCount(5);
            when(postMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(post);
            when(likeMapper.exists(any(LambdaQueryWrapper.class))).thenReturn(false);
            when(likeMapper.insert(any(PostLike.class)))
                    .thenThrow(new DuplicateKeyException("uk_post_user"));

            service.likePost(USER_ID, POST_ID);

            verify(postMapper, never()).updateById(any(Post.class));
            assertThat(post.getLikeCount()).isEqualTo(5);
        }

        @Test
        @DisplayName("Non-published post -> throws COMMUNITY_POST_NOT_FOUND")
        void throwsWhenPostNotPublished() {
            // Arrange
            when(postMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

            // Act & Assert
            assertThatThrownBy(() -> service.likePost(USER_ID, POST_ID))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code").isEqualTo(ErrorCode.COMMUNITY_POST_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("unlikePost")
    class UnlikePostTests {

        @Test
        @DisplayName("Removes like, decrements count")
        void removesLikeAndDecrements() {
            // Arrange
            Post post = buildPublishedPost(POST_ID);
            post.setLikeCount(5);
            when(postMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(post);

            PostLike existingLike = new PostLike();
            existingLike.setId(LIKE_ID);
            existingLike.setPostId(POST_ID);
            existingLike.setUserId(USER_ID);
            when(likeMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(existingLike);
            when(likeMapper.deleteById((java.io.Serializable) LIKE_ID)).thenReturn(1);

            // Act
            service.unlikePost(USER_ID, POST_ID);

            // Assert
            verify(likeMapper).deleteById((java.io.Serializable) LIKE_ID);
            verify(postMapper).updateById(post);
            assertThat(post.getLikeCount()).isEqualTo(4);
        }

        @Test
        @DisplayName("Not liked: no error (idempotent)")
        void notLikedIsIdempotent() {
            // Arrange
            Post post = buildPublishedPost(POST_ID);
            post.setLikeCount(5);
            when(postMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(post);
            when(likeMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

            // Act
            service.unlikePost(USER_ID, POST_ID);

            // Assert
            verify(likeMapper, never()).deleteById(any(java.io.Serializable.class));
            verify(postMapper, never()).updateById(any(Post.class));
            assertThat(post.getLikeCount()).isEqualTo(5);
        }

        @Test
        @DisplayName("Count never goes below 0")
        void countNeverGoesBelowZero() {
            // Arrange
            Post post = buildPublishedPost(POST_ID);
            post.setLikeCount(0);
            when(postMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(post);

            PostLike existingLike = new PostLike();
            existingLike.setId(LIKE_ID);
            existingLike.setPostId(POST_ID);
            existingLike.setUserId(USER_ID);
            when(likeMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(existingLike);
            when(likeMapper.deleteById((java.io.Serializable) LIKE_ID)).thenReturn(1);

            // Act
            service.unlikePost(USER_ID, POST_ID);

            // Assert
            verify(postMapper).updateById(post);
            assertThat(post.getLikeCount()).isEqualTo(0);
        }
    }

    // ==================== Favorite Tests ====================

    @Nested
    @DisplayName("favoritePost")
    class FavoritePostTests {

        @Test
        @DisplayName("First favorite: inserts, increments count")
        void firstFavoriteInsertsAndIncrements() {
            // Arrange
            Post post = buildPublishedPost(POST_ID);
            post.setFavoriteCount(3);
            when(postMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(post);
            when(favoriteMapper.exists(any(LambdaQueryWrapper.class))).thenReturn(false);
            when(favoriteMapper.insert(any(PostFavorite.class))).thenReturn(1);

            // Act
            service.favoritePost(USER_ID, POST_ID);

            // Assert
            verify(favoriteMapper).insert(any(PostFavorite.class));
            verify(postMapper).updateById(post);
            assertThat(post.getFavoriteCount()).isEqualTo(4);
        }

        @Test
        @DisplayName("Duplicate favorite: no error, count unchanged")
        void duplicateFavoriteIsIdempotent() {
            // Arrange
            Post post = buildPublishedPost(POST_ID);
            post.setFavoriteCount(3);
            when(postMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(post);
            when(favoriteMapper.exists(any(LambdaQueryWrapper.class))).thenReturn(true);

            // Act
            service.favoritePost(USER_ID, POST_ID);

            // Assert
            verify(favoriteMapper, never()).insert(any(PostFavorite.class));
            verify(postMapper, never()).updateById(any(Post.class));
            assertThat(post.getFavoriteCount()).isEqualTo(3);
        }

        @Test
        @DisplayName("Concurrent duplicate favorite on insert remains idempotent")
        void duplicateFavoriteOnInsertIsIdempotent() {
            Post post = buildPublishedPost(POST_ID);
            post.setFavoriteCount(3);
            when(postMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(post);
            when(favoriteMapper.exists(any(LambdaQueryWrapper.class))).thenReturn(false);
            when(favoriteMapper.insert(any(PostFavorite.class)))
                    .thenThrow(new DuplicateKeyException("uk_post_user"));

            service.favoritePost(USER_ID, POST_ID);

            verify(postMapper, never()).updateById(any(Post.class));
            assertThat(post.getFavoriteCount()).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("unfavoritePost")
    class UnfavoritePostTests {

        @Test
        @DisplayName("Removes favorite, decrements count")
        void removesFavoriteAndDecrements() {
            // Arrange
            Post post = buildPublishedPost(POST_ID);
            post.setFavoriteCount(3);
            when(postMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(post);

            PostFavorite existing = new PostFavorite();
            existing.setId(FAVORITE_ID);
            existing.setPostId(POST_ID);
            existing.setUserId(USER_ID);
            when(favoriteMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(existing);
            when(favoriteMapper.deleteById((java.io.Serializable) FAVORITE_ID)).thenReturn(1);

            // Act
            service.unfavoritePost(USER_ID, POST_ID);

            // Assert
            verify(favoriteMapper).deleteById((java.io.Serializable) FAVORITE_ID);
            verify(postMapper).updateById(post);
            assertThat(post.getFavoriteCount()).isEqualTo(2);
        }

        @Test
        @DisplayName("Not favorited: no error (idempotent)")
        void notFavoritedIsIdempotent() {
            // Arrange
            Post post = buildPublishedPost(POST_ID);
            post.setFavoriteCount(3);
            when(postMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(post);
            when(favoriteMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

            // Act
            service.unfavoritePost(USER_ID, POST_ID);

            // Assert
            verify(favoriteMapper, never()).deleteById(any(java.io.Serializable.class));
            verify(postMapper, never()).updateById(any(Post.class));
            assertThat(post.getFavoriteCount()).isEqualTo(3);
        }
    }

    // ==================== Report Tests ====================

    @Nested
    @DisplayName("reportPost")
    class ReportPostTests {

        @Test
        @DisplayName("Creates report with PENDING status")
        void createsPendingReport() {
            // Arrange
            Post post = buildPublishedPost(POST_ID);
            when(postMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(post);
            when(reportMapper.exists(any(LambdaQueryWrapper.class))).thenReturn(false);
            when(reportMapper.insert(any(PostReport.class))).thenReturn(1);

            ReportPostRequest request = new ReportPostRequest("SPAM", "垃圾内容");

            // Act
            service.reportPost(USER_ID, POST_ID, request);

            // Assert
            verify(reportMapper).insert(any(PostReport.class));
        }

        @Test
        @DisplayName("Duplicate report -> throws COMMUNITY_DUPLICATE_REPORT")
        void throwsOnDuplicateReport() {
            // Arrange
            Post post = buildPublishedPost(POST_ID);
            when(postMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(post);
            when(reportMapper.exists(any(LambdaQueryWrapper.class))).thenReturn(true);

            ReportPostRequest request = new ReportPostRequest("SPAM", "垃圾内容");

            // Act & Assert
            assertThatThrownBy(() -> service.reportPost(USER_ID, POST_ID, request))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code").isEqualTo(ErrorCode.COMMUNITY_DUPLICATE_REPORT);
            verify(reportMapper, never()).insert(any(PostReport.class));
        }

        @Test
        @DisplayName("Concurrent duplicate report on insert maps to COMMUNITY_DUPLICATE_REPORT")
        void duplicateReportOnInsertMapsToConflict() {
            Post post = buildPublishedPost(POST_ID);
            when(postMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(post);
            when(reportMapper.exists(any(LambdaQueryWrapper.class))).thenReturn(false);
            when(reportMapper.insert(any(PostReport.class)))
                    .thenThrow(new DuplicateKeyException("uk_post_report_user"));

            ReportPostRequest request = new ReportPostRequest("SPAM", "垃圾内容");

            assertThatThrownBy(() -> service.reportPost(USER_ID, POST_ID, request))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code").isEqualTo(ErrorCode.COMMUNITY_DUPLICATE_REPORT);
        }

        @Test
        @DisplayName("Non-existent post -> throws COMMUNITY_POST_NOT_FOUND")
        void throwsWhenPostNotFound() {
            // Arrange
            when(postMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

            ReportPostRequest request = new ReportPostRequest("SPAM", "垃圾内容");

            // Act & Assert
            assertThatThrownBy(() -> service.reportPost(USER_ID, 9999L, request))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code").isEqualTo(ErrorCode.COMMUNITY_POST_NOT_FOUND);
        }
    }

    // ==================== Helper Methods ====================

    private Post buildPublishedPost(Long id) {
        Post post = new Post();
        post.setId(id);
        post.setUserId(2001L);
        post.setTitle("测试帖子");
        post.setContent("测试内容");
        post.setStatus("PUBLISHED");
        post.setDeleted(0);
        post.setViewCount(0);
        post.setLikeCount(0);
        post.setCommentCount(0);
        post.setFavoriteCount(0);
        return post;
    }
}
