package com.petcare.ai.service;

import com.petcare.ai.dto.AiConversationCreateRequest;
import com.petcare.ai.dto.AiConversationResponse;
import com.petcare.ai.dto.AiMessageCreateRequest;
import com.petcare.ai.dto.AiMessageResponse;
import com.petcare.common.exception.BusinessException;
import com.petcare.common.exception.ErrorCode;
import com.petcare.common.pagination.PageResponse;

/**
 * Application service for AI conversation operations.
 * Orchestrates conversation creation, message handling, and AI response generation.
 */
public interface AiConversationApplicationService {

    /**
     * Creates a new AI conversation for the current user.
     * Only CUSTOMER_SERVICE and PET_CHAT types are allowed.
     *
     * @param currentUserId the user ID from security context, not from request body
     * @param request       conversation creation request
     * @return created conversation response
     */
    AiConversationResponse createConversation(Long currentUserId, AiConversationCreateRequest request);

    /**
     * Lists conversations belonging to the current user, paginated.
     */
    PageResponse<AiConversationResponse> getMyConversations(Long currentUserId, int page, int size);

    /**
     * Gets conversation detail with messages.
     * Verifies that the conversation belongs to the current user.
     */
    AiConversationResponse getConversation(Long currentUserId, Long conversationId);

    /**
     * Sends a message in a conversation and gets an AI response.
     * Verifies ownership and routes to the correct AI service based on conversation type.
     *
     * @param currentUserId  the user ID from security context
     * @param conversationId the conversation ID
     * @param request        message creation request
     * @return the assistant's response message
     */
    AiMessageResponse sendMessage(Long currentUserId, Long conversationId, AiMessageCreateRequest request);

    /**
     * Lists messages in a conversation.
     * Verifies that the conversation belongs to the current user.
     */
    PageResponse<AiMessageResponse> getMessages(Long currentUserId, Long conversationId, int page, int size);
}
