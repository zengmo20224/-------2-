package com.petcare.ai.service;

import com.petcare.ai.domain.CustomerServiceContext;
import com.petcare.ai.domain.CustomerServiceContextBuilder;
import com.petcare.ai.domain.CustomerServiceGroundingPolicy;
import com.petcare.ai.dto.*;
import com.petcare.ai.entity.AiConversation;
import com.petcare.ai.entity.AiMessage;
import com.petcare.ai.entity.AiUsageLog;
import com.petcare.ai.mapper.AiConversationMapper;
import com.petcare.ai.mapper.AiMessageMapper;
import com.petcare.ai.mapper.AiUsageLogMapper;
import com.petcare.ai.provider.AiProviderUnavailableException;
import com.petcare.ai.provider.MockAiProviderClient;
import com.petcare.ai.service.impl.AiConversationApplicationServiceImpl;
import com.petcare.common.exception.BusinessException;
import com.petcare.common.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AI conversation application service.
 * Uses MockAiProviderClient — no real API calls.
 */
class AiConversationApplicationServiceTest {

    private AiConversationMapper conversationMapper;
    private AiMessageMapper messageMapper;
    private AiUsageLogMapper usageLogMapper;
    private MockAiProviderClient mockProvider;
    private CustomerServiceContextBuilder contextBuilder;
    private AiConversationApplicationServiceImpl service;

    @BeforeEach
    void setUp() {
        conversationMapper = mock(AiConversationMapper.class);
        messageMapper = mock(AiMessageMapper.class);
        usageLogMapper = mock(AiUsageLogMapper.class);
        mockProvider = new MockAiProviderClient();
        contextBuilder = mock(CustomerServiceContextBuilder.class);

        service = new AiConversationApplicationServiceImpl(
                conversationMapper, messageMapper, usageLogMapper,
                mockProvider, contextBuilder
        );
    }

    @Nested
    @DisplayName("Create conversation")
    class CreateConversation {

        @Test
        @DisplayName("Creates CUSTOMER_SERVICE conversation")
        void createsCustomerServiceConversation() {
            doAnswer(inv -> {
                AiConversation c = inv.getArgument(0);
                c.setId(1L);
                return 1;
            }).when(conversationMapper).insert((AiConversation) any());

            AiConversationCreateRequest request = new AiConversationCreateRequest("CUSTOMER_SERVICE", "咨询洗护");
            AiConversationResponse response = service.createConversation(100L, request);

            assertEquals("CUSTOMER_SERVICE", response.conversationType());
            assertEquals("咨询洗护", response.title());
            verify(conversationMapper).insert((AiConversation) any());
        }

        @Test
        @DisplayName("Creates PET_CHAT conversation")
        void createsPetChatConversation() {
            doAnswer(inv -> {
                AiConversation c = inv.getArgument(0);
                c.setId(2L);
                return 1;
            }).when(conversationMapper).insert((AiConversation) any());

            AiConversationCreateRequest request = new AiConversationCreateRequest("PET_CHAT", "聊聊我家猫");
            AiConversationResponse response = service.createConversation(100L, request);

            assertEquals("PET_CHAT", response.conversationType());
        }

        @Test
        @DisplayName("Rejects null user ID")
        void rejectsNullUserId() {
            AiConversationCreateRequest request = new AiConversationCreateRequest("CUSTOMER_SERVICE", "test");
            BusinessException ex = assertThrows(BusinessException.class,
                    () -> service.createConversation(null, request));
            assertEquals(ErrorCode.UNAUTHORIZED, ex.getCode());
        }

        @Test
        @DisplayName("Rejects ADMIN_ANALYSIS type for users")
        void rejectsAdminAnalysis() {
            AiConversationCreateRequest request = new AiConversationCreateRequest("ADMIN_ANALYSIS", "分析");
            BusinessException ex = assertThrows(BusinessException.class,
                    () -> service.createConversation(100L, request));
            assertEquals(ErrorCode.AI_CONVERSATION_TYPE_INVALID, ex.getCode());
        }
    }

    @Nested
    @DisplayName("Send message - Pet Chat")
    class SendMessagePetChat {

        @Test
        @DisplayName("High-risk symptom returns fixed response without calling Provider")
        void highRiskSymptom_noProviderCall() {
            setupConversation(1L, 100L, "PET_CHAT");
            doAnswer(inv -> {
                AiMessage m = inv.getArgument(0);
                m.setId(10L);
                return 1;
            }).when(messageMapper).insert((AiMessage) any());

            mockProvider.withSuccess("This should not be used");

            AiMessageCreateRequest request = new AiMessageCreateRequest("我的猫呕吐了，还在抽搐");
            AiMessageResponse response = service.sendMessage(100L, 1L, request);

            assertTrue(response.content().contains("宠物医院") || response.content().contains("兽医"));
            assertEquals(0, mockProvider.getCallCount());
        }

        @Test
        @DisplayName("Normal message gets AI response via Provider")
        void normalMessage_callsProvider() {
            setupConversation(1L, 100L, "PET_CHAT");
            doAnswer(inv -> {
                AiMessage m = inv.getArgument(0);
                m.setId(10L);
                return 1;
            }).when(messageMapper).insert((AiMessage) any());

            mockProvider.withSuccess("你的猫咪看起来很开心！");

            AiMessageCreateRequest request = new AiMessageCreateRequest("我的猫今天很活泼");
            AiMessageResponse response = service.sendMessage(100L, 1L, request);

            assertEquals("你的猫咪看起来很开心！", response.content());
            assertEquals(1, mockProvider.getCallCount());
        }

        @Test
        @DisplayName("Medical safety violation in output returns safe fallback")
        void medicalViolation_returnsFallback() {
            setupConversation(1L, 100L, "PET_CHAT");
            doAnswer(inv -> {
                AiMessage m = inv.getArgument(0);
                m.setId(10L);
                return 1;
            }).when(messageMapper).insert((AiMessage) any());

            mockProvider.withSuccess("你的猫诊断为了感冒，可以吃点消炎药");

            AiMessageCreateRequest request = new AiMessageCreateRequest("猫咪打喷嚏");
            AiMessageResponse response = service.sendMessage(100L, 1L, request);

            assertTrue(response.content().contains("兽医") || response.content().contains("诊断"));
        }

        @Test
        @DisplayName("Provider unavailable throws exception")
        void providerUnavailable_throwsException() {
            setupConversation(1L, 100L, "PET_CHAT");
            doAnswer(inv -> {
                AiMessage m = inv.getArgument(0);
                m.setId(10L);
                return 1;
            }).when(messageMapper).insert((AiMessage) any());

            mockProvider.withUnavailable();

            AiMessageCreateRequest request = new AiMessageCreateRequest("你好");
            assertThrows(AiProviderUnavailableException.class,
                    () -> service.sendMessage(100L, 1L, request));

            // Verify failed usage was logged
            verify(usageLogMapper).insert((AiUsageLog) any());
        }
    }

    @Nested
    @DisplayName("Send message - Customer Service")
    class SendMessageCustomerService {

        @Test
        @DisplayName("No context with grounding question returns fallback")
        void noContext_groundingQuestion_returnsFallback() {
            setupConversation(2L, 100L, "CUSTOMER_SERVICE");
            doAnswer(inv -> {
                AiMessage m = inv.getArgument(0);
                m.setId(20L);
                return 1;
            }).when(messageMapper).insert((AiMessage) any());
            when(contextBuilder.build()).thenReturn(CustomerServiceContext.empty());

            AiMessageCreateRequest request = new AiMessageCreateRequest("洗澡多少钱？");
            AiMessageResponse response = service.sendMessage(100L, 2L, request);

            assertEquals(CustomerServiceGroundingPolicy.getNoContextFallback(), response.content());
            assertEquals(0, mockProvider.getCallCount());
        }

        @Test
        @DisplayName("With context, calls Provider and returns response")
        void withContext_callsProvider() {
            setupConversation(2L, 100L, "CUSTOMER_SERVICE");
            doAnswer(inv -> {
                AiMessage m = inv.getArgument(0);
                m.setId(20L);
                return 1;
            }).when(messageMapper).insert((AiMessage) any());
            CustomerServiceContext context = new CustomerServiceContext(
                    "宠物乐园", null, "9:00-18:00", null, null, null,
                    List.of(), List.of(), List.of(), true
            );
            when(contextBuilder.build()).thenReturn(context);
            mockProvider.withSuccess("我们门店营业时间是9:00-18:00。");

            AiMessageCreateRequest request = new AiMessageCreateRequest("你们几点开门");
            AiMessageResponse response = service.sendMessage(100L, 2L, request);

            assertEquals("我们门店营业时间是9:00-18:00。", response.content());
        }
    }

    @Nested
    @DisplayName("Conversation ownership")
    class ConversationOwnership {

        @Test
        @DisplayName("User cannot access another user's conversation")
        void cannotAccessOtherUsersConversation() {
            AiConversation conv = new AiConversation();
            conv.setId(1L);
            conv.setUserId(999L);
            when(conversationMapper.selectById(1L)).thenReturn(conv);

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> service.getConversation(100L, 1L));
            assertEquals(ErrorCode.AI_CONVERSATION_FORBIDDEN, ex.getCode());
        }

        @Test
        @DisplayName("Nonexistent conversation returns not found")
        void nonexistentConversation_returnsNotFound() {
            when(conversationMapper.selectById(999L)).thenReturn(null);

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> service.getConversation(100L, 999L));
            assertEquals(ErrorCode.AI_CONVERSATION_NOT_FOUND, ex.getCode());
        }
    }

    private void setupConversation(Long id, Long userId, String type) {
        AiConversation conv = new AiConversation();
        conv.setId(id);
        conv.setUserId(userId);
        conv.setConversationType(type);
        when(conversationMapper.selectById(id)).thenReturn(conv);
    }
}
