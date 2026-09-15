package com.luna.lilitalk.domain.service;

import com.luna.lilitalk.domain.dto.ChatDto.ChatRoomDto;
import com.luna.lilitalk.domain.dto.ChatDto.ChatRoomMemberDto;
import com.luna.lilitalk.domain.dto.ChatDto.CreateChatRoomRequest;
import com.luna.lilitalk.domain.dto.ChatDto.MessageDto;
import com.luna.lilitalk.domain.dto.ChatDto.MessagePageRequest;
import com.luna.lilitalk.domain.dto.ChatDto.MessagePageResponse;
import com.luna.lilitalk.domain.dto.ChatDto.SendMessageRequest;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ChatService {

    // 채팅방 관리
    ChatRoomDto createChatRoom(CreateChatRoomRequest request, Long createdBy);

    ChatRoomDto getChatRoom(Long roomId);

    Page<ChatRoomDto> getChatRooms(Long userId, Pageable pageable);

    List<ChatRoomDto> searchChatRooms(String query, Long userId);

    // 채팅방 멤버 관리
    void joinChatRoom(Long roomId, Long userId);

    void leaveChatRoom(Long roomId, Long userId);

    List<ChatRoomMemberDto> getChatRoomMembers(Long roomId);

    // 메시지 관리
    MessageDto sendMessage(SendMessageRequest request, Long senderId);

    Page<MessageDto> getMessages(Long roomId, Long userId, Pageable pageable);

    // 커서 기반 메시지 페이지네이션 (성능 최적화)
    MessagePageResponse getMessagesByCursor(MessagePageRequest request, Long userId);
}
