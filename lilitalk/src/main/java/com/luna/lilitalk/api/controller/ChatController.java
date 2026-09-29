package com.luna.lilitalk.api.controller;

import com.luna.lilitalk.domain.dto.ChatDto;
import com.luna.lilitalk.domain.dto.ChatDto.ChatRoomDto;
import com.luna.lilitalk.domain.dto.ChatDto.ChatRoomMemberDto;
import com.luna.lilitalk.domain.dto.ChatDto.MessageDirection;
import com.luna.lilitalk.domain.dto.ChatDto.MessageDto;
import com.luna.lilitalk.domain.dto.ChatDto.MessagePageRequest;
import com.luna.lilitalk.domain.dto.ChatDto.MessagePageResponse;
import com.luna.lilitalk.domain.service.ChatService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/chat-rooms")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping
    public ResponseEntity<ChatRoomDto> createChatRoom(
        @RequestParam Long createdBy,
        @Valid @RequestBody ChatDto.CreateChatRoomRequest request
    ) {
        var chatRoom = chatService.createChatRoom(request, createdBy);
        return ResponseEntity.ok(chatRoom);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ChatRoomDto> getChatRoom(@PathVariable Long id) {
        var chatRoom = chatService.getChatRoom(id);
        return ResponseEntity.ok(chatRoom);
    }

    @GetMapping
    public ResponseEntity<Page<ChatRoomDto>> getChatRooms(
        @RequestParam Long userId,
        @PageableDefault(size = 20) Pageable pageable
    ) {
        var chatRooms = chatService.getChatRooms(userId, pageable);
        return ResponseEntity.ok(chatRooms);
    }

    @PostMapping("/{id}/members")
    public ResponseEntity<Void> joinChatRoom(
        @PathVariable Long id,
        @RequestBody Map<String, Long> request
    ) {
        Long userId = request.getOrDefault("userId", null);
        if (userId == null) {
            throw new IllegalArgumentException("userId is required");
        }
        chatService.joinChatRoom(id, userId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}/members/me")
    public ResponseEntity<Void> leaveChatRoom(
        @PathVariable Long id,
        @RequestParam Long userId
    ) {
        chatService.leaveChatRoom(id, userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/members")
    public ResponseEntity<List<ChatRoomMemberDto>> getChatRoomMembers(@PathVariable Long id) {
        var members = chatService.getChatRoomMembers(id);
        return ResponseEntity.ok(members);
    }

    // 메시지 조회만 제공 (히스토리 조회용)
    @GetMapping("/{id}/messages")
    public ResponseEntity<Page<MessageDto>> getMessages(
        @PathVariable Long id,
        @RequestParam Long userId,
        @PageableDefault(size = 50) Pageable pageable
    ) {
        var messages = chatService.getMessages(id, userId, pageable);
        return ResponseEntity.ok(messages);
    }

    /**
     * 커서 기반 메시지 페이지네이션 (성능 최적화)
     */
    @GetMapping("/{id}/messages/cursor")
    public ResponseEntity<MessagePageResponse> getMessagesByCursor(
        @PathVariable Long id,
        @RequestParam Long userId,
        @RequestParam(required = false) @Nullable Long cursor,
        @RequestParam(defaultValue = "50") Integer limit,
        @RequestParam(defaultValue = "BEFORE") MessageDirection direction
    ) {
        var request = new MessagePageRequest(
            id,
            cursor,
            limit > 100 ? 100 : limit, // 최대 100개로 제한
            direction
        );
        var response = chatService.getMessagesByCursor(request, userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    public ResponseEntity<List<ChatRoomDto>> searchChatRooms(
        @RequestParam(required = false, defaultValue = "") String q,
        @RequestParam Long userId
    ) {
        var chatRooms = chatService.searchChatRooms(q, userId);
        return ResponseEntity.ok(chatRooms);
    }
}
