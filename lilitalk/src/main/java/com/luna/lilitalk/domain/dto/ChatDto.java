package com.luna.lilitalk.domain.dto;

import com.luna.lilitalk.domain.model.ChatRoomType;
import com.luna.lilitalk.domain.model.MemberRole;
import com.luna.lilitalk.domain.model.MessageType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;
import org.jspecify.annotations.Nullable;

public class ChatDto {

    public record ChatRoomDto(
        Long id,
        String name,
        @Nullable String description,
        ChatRoomType type,
        @Nullable String imageUrl,
        Boolean isActive,
        Integer maxMembers,
        Integer memberCount,
        UserDto.UserDataDto createdBy,
        LocalDateTime createdAt,
        @Nullable MessageDto lastMessage
    ) {

    }

    public record CreateChatRoomRequest(
        @NotBlank(message = "채팅방 이름은 필수입니다") // {"name": ""}
        @Size(min = 1, max = 100, message = "채팅방 이름은 1-100자 사이여야 합니다")
        String name,

        @Nullable String description,

        @NotNull(message = "채팅방 타입은 필수입니다") // {"type" :null}
        ChatRoomType type,

        String imageUrl,

        Integer maxMembers
    ) {

    }

    public record MessageDto(
        Long id,
        Long chatRoomId,
        UserDto.UserDataDto sender,
        MessageType type,
        @Nullable String content,
        Boolean isEdited,
        Boolean isDeleted,
        LocalDateTime createdAt,
        @Nullable LocalDateTime editedAt,
        Long sequenceNumber
    ) {

        public MessageDto(
            Long id,
            Long chatRoomId,
            UserDto.UserDataDto sender,
            MessageType type,
            @Nullable String content,
            Boolean isEdited,
            Boolean isDeleted,
            LocalDateTime createdAt,
            @Nullable LocalDateTime editedAt
        ) {
            this(id,
                chatRoomId,
                sender,
                type,
                content,
                isEdited,
                isDeleted,
                createdAt,
                editedAt,
                0L);
        }
    }

    public record SendMessageRequest(
        @NotNull(message = "채팅방 ID는 필수입니다")
        Long chatRoomId,

        @NotNull(message = "메시지 타입은 필수입니다")
        MessageType type,

        String content
    ) {

    }

    public record MessagePageRequest(
        Long chatRoomId,
        @Nullable Long cursor, // 마지막 메시지 ID (없으면 최신부터)
        Integer limit,
        MessageDirection direction
    ) {

        public MessagePageRequest(Long chatRoomId) {
            this(chatRoomId, null, 50, MessageDirection.BEFORE);
        }
    }

    public record MessagePageResponse(
        List<MessageDto> messages,
        @Nullable Long nextCursor, // 다음 페이지를 위한 커서
        @Nullable Long prevCursor, // 이전 페이지를 위한 커서
        Boolean hasNext,
        Boolean hasPrev
    ) {

    }

    public enum MessageDirection {
        BEFORE, // 커서 이전 메시지들 (과거)
        AFTER   // 커서 이후 메시지들 (최신)
    }

    public record ChatRoomMemberDto(
        Long id,
        UserDto.UserDataDto user,
        MemberRole role,
        Boolean isActive,
        @Nullable Long lastReadMessageId,
        LocalDateTime joinedAt,
        @Nullable LocalDateTime leftAt
    ) {

    }
}
