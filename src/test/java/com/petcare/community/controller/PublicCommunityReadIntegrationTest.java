package com.petcare.community.controller;

import com.petcare.community.entity.Post;
import com.petcare.community.entity.PostComment;
import com.petcare.community.entity.Topic;
import com.petcare.community.mapper.PostCommentMapper;
import com.petcare.community.mapper.PostMapper;
import com.petcare.community.mapper.TopicMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Phase 11-05 public community read privacy & filtering contract.
 *
 * <p>Anonymous readers must only see PUBLISHED, non-deleted content and must never
 * receive private identifiers (userId, petId), internal status, risk level or
 * soft-delete markers. Comments under a non-published post must not be reachable.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class PublicCommunityReadIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TopicMapper topicMapper;

    @Autowired
    private PostMapper postMapper;

    @Autowired
    private PostCommentMapper commentMapper;

    private Long activeTopicId;
    private Long inactiveTopicId;
    private Long publishedPostId;
    private Long pendingPostId;
    private Long rejectedPostId;
    private Long publishedCommentId;
    private Long pendingCommentId;

    @BeforeEach
    void setUp() {
        activeTopicId = insertTopic("日常分享_pubT", "ACTIVE", 1);
        inactiveTopicId = insertTopic("禁用话题_pubT", "INACTIVE", 2);

        publishedPostId = insertPost(activeTopicId, "已发布帖子_pubT", "PUBLISHED");
        pendingPostId = insertPost(activeTopicId, "待审核帖子_pubT", "PENDING_REVIEW");
        rejectedPostId = insertPost(activeTopicId, "拒绝帖子_pubT", "REJECTED");

        publishedCommentId = insertComment(publishedPostId, "已发布评论_pubT", "PUBLISHED");
        pendingCommentId = insertComment(publishedPostId, "待审核评论_pubT", "PENDING_REVIEW");
        // A comment sitting under a non-published post
        insertComment(pendingPostId, "待审核帖下的评论_pubT", "PUBLISHED");
    }

    // ==================== Topics ====================

    @Test
    @DisplayName("Anonymous topic list excludes INACTIVE topics")
    void topicListExcludesInactive() throws Exception {
        mockMvc.perform(get("/api/v1/topics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[?(@.name=='日常分享_pubT')]").exists())
                .andExpect(jsonPath("$.data[?(@.name=='禁用话题_pubT')]").doesNotExist());
    }

    // ==================== Posts list ====================

    @Test
    @DisplayName("Anonymous post list shows only PUBLISHED posts")
    void postListShowsOnlyPublished() throws Exception {
        mockMvc.perform(get("/api/v1/posts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[?(@.title=='已发布帖子_pubT')]").exists())
                .andExpect(jsonPath("$.data.items[?(@.title=='待审核帖子_pubT')]").doesNotExist())
                .andExpect(jsonPath("$.data.items[?(@.title=='拒绝帖子_pubT')]").doesNotExist());
    }

    @Test
    @DisplayName("Anonymous post list does not leak private identifiers or status")
    void postListDoesNotLeakPrivateFields() throws Exception {
        mockMvc.perform(get("/api/v1/posts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[0].userId").doesNotExist())
                .andExpect(jsonPath("$.data.items[0].petId").doesNotExist())
                .andExpect(jsonPath("$.data.items[0].status").doesNotExist())
                .andExpect(jsonPath("$.data.items[0].riskLevel").doesNotExist())
                .andExpect(jsonPath("$.data.items[0].deleted").doesNotExist());
    }

    // ==================== Post detail ====================

    @Test
    @DisplayName("Anonymous published post detail returns 200 without private fields")
    void publishedPostDetailAnonymous() throws Exception {
        mockMvc.perform(get("/api/v1/posts/" + publishedPostId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("已发布帖子_pubT"))
                .andExpect(jsonPath("$.data.userId").doesNotExist())
                .andExpect(jsonPath("$.data.petId").doesNotExist())
                .andExpect(jsonPath("$.data.status").doesNotExist())
                .andExpect(jsonPath("$.data.riskLevel").doesNotExist())
                .andExpect(jsonPath("$.data.deleted").doesNotExist());
    }

    @Test
    @DisplayName("Anonymous pending post detail returns 404 (no status leak)")
    void pendingPostDetailAnonymousReturns404() throws Exception {
        mockMvc.perform(get("/api/v1/posts/" + pendingPostId))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Anonymous rejected post detail returns 404 (no status leak)")
    void rejectedPostDetailAnonymousReturns404() throws Exception {
        mockMvc.perform(get("/api/v1/posts/" + rejectedPostId))
                .andExpect(status().isNotFound());
    }

    // ==================== Comments ====================

    @Test
    @DisplayName("Anonymous comment list shows only PUBLISHED comments without private fields")
    void commentListShowsOnlyPublished() throws Exception {
        mockMvc.perform(get("/api/v1/posts/" + publishedPostId + "/comments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[?(@.content=='已发布评论_pubT')]").exists())
                .andExpect(jsonPath("$.data.items[?(@.content=='待审核评论_pubT')]").doesNotExist())
                .andExpect(jsonPath("$.data.items[0].userId").doesNotExist())
                .andExpect(jsonPath("$.data.items[0].status").doesNotExist())
                .andExpect(jsonPath("$.data.items[0].riskLevel").doesNotExist())
                .andExpect(jsonPath("$.data.items[0].deleted").doesNotExist());
    }

    @Test
    @DisplayName("Anonymous comments under a non-published post are unreachable (404)")
    void commentsUnderPendingPostUnreachable() throws Exception {
        mockMvc.perform(get("/api/v1/posts/" + pendingPostId + "/comments"))
                .andExpect(status().isNotFound());
    }

    // ==================== Helpers ====================

    private Long insertTopic(String name, String status, int sort) {
        Topic topic = new Topic();
        topic.setName(name);
        topic.setStatus(status);
        topic.setSort(sort);
        topic.setDeleted(0);
        topicMapper.insert(topic);
        return topic.getId();
    }

    private Long insertPost(Long topicId, String title, String status) {
        Post post = new Post();
        post.setUserId(7000001L);
        post.setPetId(8000001L);
        post.setTopicId(topicId);
        post.setTitle(title);
        post.setContent(title + "_正文");
        post.setStatus(status);
        post.setRiskLevel("PUBLISHED".equals(status) ? 0 : 1);
        post.setViewCount(0);
        post.setLikeCount(0);
        post.setCommentCount(0);
        post.setFavoriteCount(0);
        post.setDeleted(0);
        postMapper.insert(post);
        return post.getId();
    }

    private Long insertComment(Long postId, String content, String status) {
        PostComment comment = new PostComment();
        comment.setPostId(postId);
        comment.setUserId(7000002L);
        comment.setContent(content);
        comment.setStatus(status);
        comment.setRiskLevel("PUBLISHED".equals(status) ? 0 : 1);
        comment.setLikeCount(0);
        comment.setDeleted(0);
        commentMapper.insert(comment);
        return comment.getId();
    }
}
