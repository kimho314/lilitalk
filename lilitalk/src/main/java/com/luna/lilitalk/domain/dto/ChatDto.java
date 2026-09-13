package com.luna.lilitalk.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ChatDto {

    public record ChatRoomDto(Long id) {

    }

    public record CreateChatRoomRequest(
        @NotBlank(message = "채팅방 이름은 필수입니다") // {"name": ""}
        @Size(min = 1, max = 100, message = "채팅방 이름은 1-100자 사이여야 합니다")
        String name
    ) {

    }
}
