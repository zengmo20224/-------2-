package com.petcare.community.domain;

import com.petcare.community.entity.Post;

/**
 * Pure policy for determining post visibility.
 * A post is visible to regular users only if it is PUBLISHED and not logically deleted.
 * Authors can see their own non-deleted posts regardless of status.
 */
public final class PostVisibilityPolicy {

    private PostVisibilityPolicy() {
        // prevent instantiation
    }

    /**
     * Checks if a post is visible to a given user.
     *
     * @param post         the post to check
     * @param currentUserId the ID of the current user (null if not authenticated)
     * @return true if the post is visible
     */
    public static boolean isVisibleToUser(Post post, Long currentUserId) {
        if (post == null) {
            return false;
        }
        if (post.getDeleted() != null && post.getDeleted() == 1) {
            return false;
        }
        // Published posts visible to all
        if ("PUBLISHED".equals(post.getStatus())) {
            return true;
        }
        // Author can see their own non-deleted posts
        if (currentUserId != null && currentUserId.equals(post.getUserId())) {
            return true;
        }
        return false;
    }

    /**
     * Checks if a post is visible in public listing (only PUBLISHED, non-deleted).
     */
    public static boolean isPubliclyVisible(Post post) {
        return post != null
                && (post.getDeleted() == null || post.getDeleted() == 0)
                && "PUBLISHED".equals(post.getStatus());
    }
}
