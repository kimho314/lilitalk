package com.luna.lilitalk.domain.dto;

import com.luna.lilitalk.domain.model.MessageType;
import java.time.LocalDateTime;
import org.jspecify.annotations.Nullable;

public class WebSocketDto {

    public record ChatMessage(
        Long id,
        String content,
        MessageType type,
        Long senderId,
        String senderName,
        Long sequenceNumber,
        Long chatRoomId,
        LocalDateTime timestamp
    ) {

        public ChatMessage(
            Long id,
            String content,
            MessageType type,
            Long senderId,
            String senderName,
            Long sequenceNumber,
            Long chatRoomId
        ) {
            this(id,
                content,
                type,
                senderId,
                senderName,
                sequenceNumber,
                chatRoomId,
                LocalDateTime.now());
        }
    }

    public record ErrorMessage(
        String message,
        @Nullable String code,
        @Nullable Long chatRoomId,
        LocalDateTime timestamp
    ) {

        public ErrorMessage(
            @Nullable Long chatRoomId,
            @Nullable String code,
            String message
        ) {
            this(message,
                code,
                chatRoomId,
                LocalDateTime.now());
        }
    }
}
