package com.luna.lilitalk.websocket.handler;

import com.luna.lilitalk.domain.dto.ChatDto.SendMessageRequest;
import com.luna.lilitalk.domain.dto.WebSocketDto.ErrorMessage;
import com.luna.lilitalk.domain.model.MessageType;
import com.luna.lilitalk.domain.service.ChatService;
import com.luna.lilitalk.persistence.service.WebSocketSessionManager;
import java.io.EOFException;
import java.io.IOException;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Component
public class ChatWebSocketHandler implements WebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(ChatWebSocketHandler.class);
    private final WebSocketSessionManager sessionManager;
    private final ChatService chatService;
    private final ObjectMapper objectMapper;

    public ChatWebSocketHandler(
        WebSocketSessionManager sessionManager,
        ChatService chatService,
        ObjectMapper objectMapper
    ) {
        this.sessionManager = sessionManager;
        this.chatService = chatService;
        this.objectMapper = objectMapper;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        var userId = getUserIdFromSession(session);

        if (userId != null) {
            sessionManager.addSession(userId, session);
            log.info("Session $userId established for $userId");

            try {
                loadUserChatRooms(userId);
            } catch (Exception e) {
                log.error("Error while loading user chat rooms", e);
            }
        }
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message)
        throws Exception {
        var userId = getUserIdFromSession(session);
        if (userId == null) {
            return;
        }

        try {
            if (message instanceof TextMessage textMessage) {
                handleTextMessage(session, userId, textMessage.getPayload());
            } else {
                log.warn("Unsupported message type {}", message.getClass().getName());
            }
        } catch (Exception e) {
            log.warn("exception while processing message", e);
            sendErrorMessage(session, "메시지 처리 에러", null);
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception)
        throws Exception {
        var userId = getUserIdFromSession(session);

        // EOFException -> 클라이언트 연결 해제, 정상적인 상황 (로그레벨을 따로 두기 위해)
        if (exception instanceof EOFException) {
            log.debug("WebSocket connection closed by client for user: {}", userId);
        } else {
            log.error("WebSocket transport error for user: {}", userId, exception);
        }

        if (userId != null) {
            sessionManager.removeSession(userId, session);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus)
        throws Exception {
        var userId = getUserIdFromSession(session);
        if (userId != null) {
            sessionManager.removeSession(userId, session);
            log.info("Session removed for $userId");
        }
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }

    private @Nullable Long getUserIdFromSession(WebSocketSession session) {
        Object userId = session.getAttributes().get("userId");
        return userId instanceof Long id ? id : null;
    }

    private void loadUserChatRooms(Long userId) {
        try {
            var chatRooms = chatService.getChatRooms(userId, PageRequest.of(0, 100));

            chatRooms.getContent().forEach(room ->
                sessionManager.joinRoom(userId, room.id())
            );

            log.info("Loaded ${chatRooms.content.size} chat rooms for user: $userId");

        } catch (Exception e) {
            log.error("Failed to load chat rooms for user: $userId", e);
        }
    }


    private void sendErrorMessage(WebSocketSession session, String errorMessage,
        @Nullable String errorCode) {
        try {
            var error = new ErrorMessage(
                null,
                errorMessage,
                errorCode
            );
            var json = objectMapper.writeValueAsString(error);
            session.sendMessage(new TextMessage(json));
        } catch (IOException e) {
            log.error("Failed to send error message", e);
        }
    }

    private @Nullable String extractMessageType(String payload) {
        try {
            JsonNode type = objectMapper.readTree(payload).get("type");
            return type == null ? null : type.asString();
        } catch (Exception e) {
            return null;
        }
    }

    private void handleTextMessage(WebSocketSession session, Long userId, String payload) {
        try {
            var messageType = extractMessageType(payload);

            if ("SEND_MESSAGE".equals(messageType)) {
                var jsonNode = objectMapper.readTree(payload);

                var chatRoomIdNode = jsonNode.get("chatRoomId");
                if (chatRoomIdNode == null || chatRoomIdNode.isNull()) {
                    throw new IllegalArgumentException("chatRoomId is required");
                }
                long chatRoomId = chatRoomIdNode.asLong();

                var messageTypeNode = jsonNode.get("messageType");
                if (messageTypeNode == null || messageTypeNode.isNull()) {
                    throw new IllegalArgumentException("messageType is required");
                }
                String messageTypeText = messageTypeNode.asString();

                var contentNode = jsonNode.get("content");
                String content =
                    contentNode != null && !contentNode.isNull() ? contentNode.asString() : null;

                var sendMessageRequest = new SendMessageRequest(
                    chatRoomId,
                    MessageType.valueOf(messageTypeText),
                    content
                );

                chatService.sendMessage(sendMessageRequest, userId);
            } else {
                log.warn("Unknown message type: {}", messageType);
                sendErrorMessage(session, "알 수 없는 메시지 타입입니다: " + messageType,
                    "UNKNOWN_MESSAGE_TYPE");
            }
        } catch (Exception e) {
            log.error("Error parsing WebSocket message from user {}: {}", userId, e.getMessage(),
                e);
            sendErrorMessage(session, "메시지 형식이 올바르지 않습니다.", "INVALID_MESSAGE_FORMAT");
        }
    }
}
