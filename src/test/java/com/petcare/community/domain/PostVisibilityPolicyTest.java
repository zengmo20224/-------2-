package com.petcare.community.domain;

import com.petcare.community.entity.Post;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for PostVisibilityPolicy pure domain logic.
 */
class PostVisibilityPolicyTest {

    private Post createPost(String status, Long userId, int deleted) {
        Post post = new Post();
        post.setId(1L);
        post.setUserId(userId);
        post.setStatus(status);
        post.setDeleted(deleted);
        return post;
    }

    @Nested
    @DisplayName("isVisibleToUser")
    class IsVisibleToUser {

        @Test
        @DisplayName("Published, non-deleted post visible to any user")
        void published_visibleToAll() {
            Post post = createPost("PUBLISHED", 100L, 0);
            assertTrue(PostVisibilityPolicy.isVisibleToUser(post, 200L));
        }

        @Test
        @DisplayName("Published post visible to null user (unauthenticated)")
        void published_visibleToNull() {
            Post post = createPost("PUBLISHED", 100L, 0);
            assertTrue(PostVisibilityPolicy.isVisibleToUser(post, null));
        }

        @Test
        @DisplayName("Non-published post visible to author")
        void nonPublished_visibleToAuthor() {
            Post post = createPost("PENDING_REVIEW", 100L, 0);
            assertTrue(PostVisibilityPolicy.isVisibleToUser(post, 100L));
        }

        @Test
        @DisplayName("Non-published post NOT visible to other users")
        void nonPublished_notVisibleToOthers() {
            Post post = createPost("PENDING_REVIEW", 100L, 0);
            assertFalse(PostVisibilityPolicy.isVisibleToUser(post, 200L));
        }

        @Test
        @DisplayName("Non-published post NOT visible to null user")
        void nonPublished_notVisibleToNull() {
            Post post = createPost("REJECTED", 100L, 0);
            assertFalse(PostVisibilityPolicy.isVisibleToUser(post, null));
        }

        @Test
        @DisplayName("Deleted post NOT visible even to author")
        void deleted_notVisibleToAuthor() {
            Post post = createPost("PUBLISHED", 100L, 1);
            assertFalse(PostVisibilityPolicy.isVisibleToUser(post, 100L));
        }

        @Test
        @DisplayName("Null post NOT visible")
        void nullPost_notVisible() {
            assertFalse(PostVisibilityPolicy.isVisibleToUser(null, 100L));
        }
    }

    @Nested
    @DisplayName("isPubliclyVisible")
    class IsPubliclyVisible {

        @Test
        @DisplayName("Published non-deleted post is publicly visible")
        void published_publiclyVisible() {
            Post post = createPost("PUBLISHED", 100L, 0);
            assertTrue(PostVisibilityPolicy.isPubliclyVisible(post));
        }

        @Test
        @DisplayName("Pending review post is NOT publicly visible")
        void pending_notPubliclyVisible() {
            Post post = createPost("PENDING_REVIEW", 100L, 0);
            assertFalse(PostVisibilityPolicy.isPubliclyVisible(post));
        }

        @Test
        @DisplayName("Rejected post is NOT publicly visible")
        void rejected_notPubliclyVisible() {
            Post post = createPost("REJECTED", 100L, 0);
            assertFalse(PostVisibilityPolicy.isPubliclyVisible(post));
        }

        @Test
        @DisplayName("Hidden post is NOT publicly visible")
        void hidden_notPubliclyVisible() {
            Post post = createPost("HIDDEN", 100L, 0);
            assertFalse(PostVisibilityPolicy.isPubliclyVisible(post));
        }

        @Test
        @DisplayName("Deleted published post is NOT publicly visible")
        void deleted_notPubliclyVisible() {
            Post post = createPost("PUBLISHED", 100L, 1);
            assertFalse(PostVisibilityPolicy.isPubliclyVisible(post));
        }

        @Test
        @DisplayName("Null post is NOT publicly visible")
        void null_notPubliclyVisible() {
            assertFalse(PostVisibilityPolicy.isPubliclyVisible(null));
        }
    }
}
