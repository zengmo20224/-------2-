package com.petcare.community.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.petcare.common.exception.BusinessException;
import com.petcare.common.exception.ErrorCode;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for {@link CommunityAdminService}.
 * Uses Mockito mocks — no Spring context.
 *
 * Uses doReturn().when() stubbing to avoid ambiguous method resolution
 * caused by MyBatis-Plus BaseMapper overloaded updateById methods.
 */
@ExtendWith(MockitoExtension.class)
class CommunityAdminServiceTest {

    @Mock
    private PostMapper postMapper;

    @Mock
    private PostCommentMapper commentMapper;

    @Mock
    private PostReportMapper reportMapper;

    @Mock
    private ContentModerationService moderationService;

    @InjectMocks
    private CommunityAdminService adminService;

    private static final Long ADMIN_ID = 1L;
    private static final Long POST_ID = 100L;
    private static final Long COMMENT_ID = 200L;
    private static final Long REPORT_ID = 300L;
    private static final Long USER_ID = 10L;
    private static final String REMARK = "审核通过";

    private Post defaultPost;
    private PostComment defaultComment;
    private PostReport defaultReport;

    @BeforeEach
    void setUp() {
        defaultPost = new Post();
        defaultPost.setId(POST_ID);
        defaultPost.setUserId(USER_ID);
        defaultPost.setTitle("测试帖子");
        defaultPost.setContent("内容");
        defaultPost.setStatus("PENDING_REVIEW");
        defaultPost.setViewCount(0);
        defaultPost.setLikeCount(0);
        defaultPost.setCommentCount(5);
        defaultPost.setFavoriteCount(0);
        defaultPost.setDeleted(0);

        defaultComment = new PostComment();
        defaultComment.setId(COMMENT_ID);
        defaultComment.setPostId(POST_ID);
        defaultComment.setUserId(USER_ID);
        defaultComment.setContent("测试评论");
        defaultComment.setStatus("PENDING_REVIEW");
        defaultComment.setLikeCount(0);
        defaultComment.setDeleted(0);

        defaultReport = new PostReport();
        defaultReport.setId(REPORT_ID);
        defaultReport.setPostId(POST_ID);
        defaultReport.setReporterId(USER_ID);
        defaultReport.setReasonType("SPAM");
        defaultReport.setReason("垃圾内容");
        defaultReport.setStatus("PENDING");
    }

    // ==================== Post Management ====================

    @Nested
    @DisplayName("approvePost")
    class ApprovePostTests {

        @Test
        @DisplayName("PENDING_REVIEW -> PUBLISHED, sets publishTime, calls moderationService")
        void approvePost_success() {
            // Arrange
            doReturn(defaultPost).when(postMapper).selectOne(any());
            doReturn(1).when(postMapper).updateById(any(Post.class));

            // Act
            PostResponse result = adminService.approvePost(ADMIN_ID, POST_ID, REMARK);

            // Assert
            assertThat(result.status()).isEqualTo("PUBLISHED");

            ArgumentCaptor<Post> captor = ArgumentCaptor.forClass(Post.class);
            verify(postMapper).updateById(captor.capture());
            Post updated = captor.getValue();
            assertThat(updated.getStatus()).isEqualTo("PUBLISHED");
            assertThat(updated.getPublishTime()).isNotNull();

            verify(moderationService).approveRecord(
                    eq(CommunityContentType.POST), eq(POST_ID), eq(ADMIN_ID), eq(REMARK));
        }

        @Test
        @DisplayName("non-PENDING_REVIEW post throws COMMUNITY_REVIEW_STATUS_INVALID")
        void approvePost_invalidStatus() {
            // Arrange
            defaultPost.setStatus("PUBLISHED");
            doReturn(defaultPost).when(postMapper).selectOne(any());

            // Act & Assert
            assertThatThrownBy(() -> adminService.approvePost(ADMIN_ID, POST_ID, REMARK))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code").isEqualTo(ErrorCode.COMMUNITY_REVIEW_STATUS_INVALID);

            verifyNoInteractions(moderationService);
        }
    }

    @Nested
    @DisplayName("rejectPost")
    class RejectPostTests {

        @Test
        @DisplayName("PENDING_REVIEW -> REJECTED, sets rejectReason, calls moderationService")
        void rejectPost_success() {
            // Arrange
            doReturn(defaultPost).when(postMapper).selectOne(any());
            doReturn(1).when(postMapper).updateById(any(Post.class));

            // Act
            PostResponse result = adminService.rejectPost(ADMIN_ID, POST_ID, "内容违规");

            // Assert
            assertThat(result.status()).isEqualTo("REJECTED");

            ArgumentCaptor<Post> captor = ArgumentCaptor.forClass(Post.class);
            verify(postMapper).updateById(captor.capture());
            Post updated = captor.getValue();
            assertThat(updated.getStatus()).isEqualTo("REJECTED");
            assertThat(updated.getRejectReason()).isEqualTo("内容违规");

            verify(moderationService).rejectRecord(
                    eq(CommunityContentType.POST), eq(POST_ID), eq(ADMIN_ID), eq("内容违规"));
        }
    }

    @Nested
    @DisplayName("hidePost")
    class HidePostTests {

        @Test
        @DisplayName("PUBLISHED -> HIDDEN")
        void hidePost_success() {
            // Arrange
            defaultPost.setStatus("PUBLISHED");
            doReturn(defaultPost).when(postMapper).selectOne(any());
            doReturn(1).when(postMapper).updateById(any(Post.class));

            // Act
            PostResponse result = adminService.hidePost(ADMIN_ID, POST_ID);

            // Assert
            assertThat(result.status()).isEqualTo("HIDDEN");

            ArgumentCaptor<Post> captor = ArgumentCaptor.forClass(Post.class);
            verify(postMapper).updateById(captor.capture());
            assertThat(captor.getValue().getStatus()).isEqualTo("HIDDEN");
        }

        @Test
        @DisplayName("non-PUBLISHED post throws COMMUNITY_REVIEW_STATUS_INVALID")
        void hidePost_invalidStatus() {
            // Arrange
            defaultPost.setStatus("REJECTED");
            doReturn(defaultPost).when(postMapper).selectOne(any());

            // Act & Assert
            assertThatThrownBy(() -> adminService.hidePost(ADMIN_ID, POST_ID))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code").isEqualTo(ErrorCode.COMMUNITY_REVIEW_STATUS_INVALID);
        }
    }

    @Nested
    @DisplayName("deletePost")
    class DeletePostTests {

        @Test
        @DisplayName("logical delete sets deleted=1")
        void deletePost_success() {
            // Arrange
            doReturn(defaultPost).when(postMapper).selectOne(any());
            doReturn(1).when(postMapper).updateById(any(Post.class));

            // Act
            adminService.deletePost(ADMIN_ID, POST_ID);

            // Assert
            ArgumentCaptor<Post> captor = ArgumentCaptor.forClass(Post.class);
            verify(postMapper).updateById(captor.capture());
            assertThat(captor.getValue().getDeleted()).isEqualTo(1);
        }

        @Test
        @DisplayName("non-existent post throws COMMUNITY_POST_NOT_FOUND")
        void deletePost_notFound() {
            // Arrange
            doReturn(null).when(postMapper).selectOne(any());

            // Act & Assert
            assertThatThrownBy(() -> adminService.deletePost(ADMIN_ID, POST_ID))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code").isEqualTo(ErrorCode.COMMUNITY_POST_NOT_FOUND);
        }
    }

    // ==================== Comment Management ====================

    @Nested
    @DisplayName("approveComment")
    class ApproveCommentTests {

        @Test
        @DisplayName("PENDING_REVIEW -> PUBLISHED, increments post comment_count")
        void approveComment_success() {
            // Arrange
            doReturn(defaultComment).when(commentMapper).selectOne(any());
            doReturn(defaultPost).when(postMapper).selectById(POST_ID);
            doReturn(1).when(commentMapper).updateById(any(PostComment.class));
            doReturn(1).when(postMapper).updateById(any(Post.class));

            // Act
            CommentResponse result = adminService.approveComment(ADMIN_ID, COMMENT_ID, REMARK);

            // Assert
            assertThat(result.status()).isEqualTo("PUBLISHED");

            // Verify post comment_count incremented
            ArgumentCaptor<Post> postCaptor = ArgumentCaptor.forClass(Post.class);
            verify(postMapper).updateById(postCaptor.capture());
            assertThat(postCaptor.getValue().getCommentCount()).isEqualTo(6); // was 5

            verify(moderationService).approveRecord(
                    eq(CommunityContentType.COMMENT), eq(COMMENT_ID), eq(ADMIN_ID), eq(REMARK));
        }
    }

    @Nested
    @DisplayName("rejectComment")
    class RejectCommentTests {

        @Test
        @DisplayName("PENDING_REVIEW -> REJECTED")
        void rejectComment_success() {
            // Arrange
            doReturn(defaultComment).when(commentMapper).selectOne(any());
            doReturn(1).when(commentMapper).updateById(any(PostComment.class));

            // Act
            CommentResponse result = adminService.rejectComment(ADMIN_ID, COMMENT_ID, "评论违规");

            // Assert
            assertThat(result.status()).isEqualTo("REJECTED");

            verify(moderationService).rejectRecord(
                    eq(CommunityContentType.COMMENT), eq(COMMENT_ID), eq(ADMIN_ID), eq("评论违规"));
        }
    }

    @Nested
    @DisplayName("hideComment")
    class HideCommentTests {

        @Test
        @DisplayName("PUBLISHED -> HIDDEN, decrements post comment_count (min 0)")
        void hideComment_success() {
            // Arrange
            defaultComment.setStatus("PUBLISHED");
            doReturn(defaultComment).when(commentMapper).selectOne(any());
            doReturn(defaultPost).when(postMapper).selectById(POST_ID);
            doReturn(1).when(commentMapper).updateById(any(PostComment.class));
            doReturn(1).when(postMapper).updateById(any(Post.class));

            // Act
            CommentResponse result = adminService.hideComment(ADMIN_ID, COMMENT_ID);

            // Assert
            assertThat(result.status()).isEqualTo("HIDDEN");

            // Verify post comment_count decremented
            ArgumentCaptor<Post> postCaptor = ArgumentCaptor.forClass(Post.class);
            verify(postMapper).updateById(postCaptor.capture());
            assertThat(postCaptor.getValue().getCommentCount()).isEqualTo(4); // was 5
        }

        @Test
        @DisplayName("decrements post comment_count to min 0 when count is already 0")
        void hideComment_countFloorAtZero() {
            // Arrange
            defaultComment.setStatus("PUBLISHED");
            defaultPost.setCommentCount(0);
            doReturn(defaultComment).when(commentMapper).selectOne(any());
            doReturn(defaultPost).when(postMapper).selectById(POST_ID);
            doReturn(1).when(commentMapper).updateById(any(PostComment.class));
            doReturn(1).when(postMapper).updateById(any(Post.class));

            // Act
            adminService.hideComment(ADMIN_ID, COMMENT_ID);

            // Assert
            ArgumentCaptor<Post> postCaptor = ArgumentCaptor.forClass(Post.class);
            verify(postMapper).updateById(postCaptor.capture());
            assertThat(postCaptor.getValue().getCommentCount()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("deleteComment")
    class DeleteCommentTests {

        @Test
        @DisplayName("logical delete, if was PUBLISHED decrements comment_count")
        void deleteComment_wasPublished() {
            // Arrange
            defaultComment.setStatus("PUBLISHED");
            doReturn(defaultComment).when(commentMapper).selectOne(any());
            doReturn(defaultPost).when(postMapper).selectById(POST_ID);
            doReturn(1).when(commentMapper).updateById(any(PostComment.class));
            doReturn(1).when(postMapper).updateById(any(Post.class));

            // Act
            adminService.deleteComment(ADMIN_ID, COMMENT_ID);

            // Assert
            ArgumentCaptor<Post> postCaptor = ArgumentCaptor.forClass(Post.class);
            verify(postMapper).updateById(postCaptor.capture());
            assertThat(postCaptor.getValue().getCommentCount()).isEqualTo(4); // was 5

            ArgumentCaptor<PostComment> commentCaptor = ArgumentCaptor.forClass(PostComment.class);
            verify(commentMapper).updateById(commentCaptor.capture());
            assertThat(commentCaptor.getValue().getDeleted()).isEqualTo(1);
        }

        @Test
        @DisplayName("logical delete, if was not PUBLISHED does NOT decrement comment_count")
        void deleteComment_wasNotPublished() {
            // Arrange
            defaultComment.setStatus("REJECTED");
            doReturn(defaultComment).when(commentMapper).selectOne(any());
            doReturn(1).when(commentMapper).updateById(any(PostComment.class));

            // Act
            adminService.deleteComment(ADMIN_ID, COMMENT_ID);

            // Assert — postMapper.selectById should NOT be called
            verify(postMapper, org.mockito.Mockito.never()).selectById(any());
        }

        @Test
        @DisplayName("non-existent comment throws COMMUNITY_COMMENT_NOT_FOUND")
        void deleteComment_notFound() {
            // Arrange
            doReturn(null).when(commentMapper).selectOne(any());

            // Act & Assert
            assertThatThrownBy(() -> adminService.deleteComment(ADMIN_ID, COMMENT_ID))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code").isEqualTo(ErrorCode.COMMUNITY_COMMENT_NOT_FOUND);
        }
    }

    // ==================== Report Management ====================

    @Nested
    @DisplayName("handleReport")
    class HandleReportTests {

        @Test
        @DisplayName("PENDING -> PROCESSED, sets handler_id and handle_time")
        void handleReport_success() {
            // Arrange
            AdminReportHandleRequest request = new AdminReportHandleRequest("PROCESSED", false, "已处理");
            doReturn(defaultReport).when(reportMapper).selectById(REPORT_ID);
            doReturn(1).when(reportMapper).updateById(any(PostReport.class));

            // Act
            adminService.handleReport(ADMIN_ID, REPORT_ID, request);

            // Assert
            ArgumentCaptor<PostReport> captor = ArgumentCaptor.forClass(PostReport.class);
            verify(reportMapper).updateById(captor.capture());
            PostReport updated = captor.getValue();
            assertThat(updated.getStatus()).isEqualTo("PROCESSED");
            assertThat(updated.getHandlerId()).isEqualTo(ADMIN_ID);
            assertThat(updated.getHandleTime()).isNotNull();
            assertThat(updated.getHandleResult()).isEqualTo("PROCESSED");
        }

        @Test
        @DisplayName("with hidePost=true also hides the published post")
        void handleReport_withHidePost() {
            // Arrange
            AdminReportHandleRequest request = new AdminReportHandleRequest("PROCESSED", true, "隐藏帖子");
            defaultPost.setStatus("PUBLISHED");
            doReturn(defaultReport).when(reportMapper).selectById(REPORT_ID);
            doReturn(1).when(reportMapper).updateById(any(PostReport.class));
            doReturn(defaultPost).when(postMapper).selectOne(any());
            doReturn(1).when(postMapper).updateById(any(Post.class));

            // Act
            adminService.handleReport(ADMIN_ID, REPORT_ID, request);

            // Assert — report updated
            verify(reportMapper).updateById(any(PostReport.class));

            // Assert — post hidden
            ArgumentCaptor<Post> postCaptor = ArgumentCaptor.forClass(Post.class);
            verify(postMapper).updateById(postCaptor.capture());
            assertThat(postCaptor.getValue().getStatus()).isEqualTo("HIDDEN");
        }

        @Test
        @DisplayName("with hidePost=true but non-PUBLISHED post does not update post")
        void handleReport_hidePostButNotPublished() {
            // Arrange
            AdminReportHandleRequest request = new AdminReportHandleRequest("PROCESSED", true, "隐藏帖子");
            defaultPost.setStatus("REJECTED");
            doReturn(defaultReport).when(reportMapper).selectById(REPORT_ID);
            doReturn(1).when(reportMapper).updateById(any(PostReport.class));
            doReturn(defaultPost).when(postMapper).selectOne(any());

            // Act
            adminService.handleReport(ADMIN_ID, REPORT_ID, request);

            // Assert — report updated
            verify(reportMapper).updateById(any(PostReport.class));

            // Assert — post NOT updated (status was not PUBLISHED)
            verify(postMapper, org.mockito.Mockito.never()).updateById(any(Post.class));
        }

        @Test
        @DisplayName("non-PENDING report throws COMMUNITY_REVIEW_STATUS_INVALID")
        void handleReport_alreadyProcessed() {
            // Arrange
            defaultReport.setStatus("PROCESSED");
            AdminReportHandleRequest request = new AdminReportHandleRequest("PROCESSED", false, "备注");
            doReturn(defaultReport).when(reportMapper).selectById(REPORT_ID);

            // Act & Assert
            assertThatThrownBy(() -> adminService.handleReport(ADMIN_ID, REPORT_ID, request))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code").isEqualTo(ErrorCode.COMMUNITY_REVIEW_STATUS_INVALID);

            verify(reportMapper, org.mockito.Mockito.never()).updateById(any(PostReport.class));
        }

        @Test
        @DisplayName("non-existent report throws COMMUNITY_POST_NOT_FOUND")
        void handleReport_notFound() {
            // Arrange
            AdminReportHandleRequest request = new AdminReportHandleRequest("PROCESSED", false, "备注");
            doReturn(null).when(reportMapper).selectById(REPORT_ID);

            // Act & Assert
            assertThatThrownBy(() -> adminService.handleReport(ADMIN_ID, REPORT_ID, request))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code").isEqualTo(ErrorCode.COMMUNITY_POST_NOT_FOUND);
        }
    }
}
