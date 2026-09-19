package com.luna.lilitalk.persistence.service;

import com.luna.lilitalk.domain.dto.ChatDto;
import com.luna.lilitalk.domain.dto.ChatDto.ChatRoomDto;
import com.luna.lilitalk.domain.dto.ChatDto.ChatRoomMemberDto;
import com.luna.lilitalk.domain.dto.ChatDto.CreateChatRoomRequest;
import com.luna.lilitalk.domain.dto.ChatDto.MessageDto;
import com.luna.lilitalk.domain.dto.ChatDto.MessagePageRequest;
import com.luna.lilitalk.domain.dto.ChatDto.MessagePageResponse;
import com.luna.lilitalk.domain.dto.ChatDto.SendMessageRequest;
import com.luna.lilitalk.domain.dto.UserDto;
import com.luna.lilitalk.domain.dto.WebSocketDto.ChatMessage;
import com.luna.lilitalk.domain.model.ChatRoom;
import com.luna.lilitalk.domain.model.ChatRoomMember;
import com.luna.lilitalk.domain.model.MemberRole;
import com.luna.lilitalk.domain.model.Message;
import com.luna.lilitalk.domain.model.MessageType;
import com.luna.lilitalk.domain.model.User;
import com.luna.lilitalk.domain.service.ChatService;
import com.luna.lilitalk.persistence.redis.RedisMessageBroker;
import com.luna.lilitalk.persistence.repository.ChatRoomMemberRepository;
import com.luna.lilitalk.persistence.repository.ChatRoomRepository;
import com.luna.lilitalk.persistence.repository.MessageRepository;
import com.luna.lilitalk.persistence.repository.UserRepository;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ChatServiceImpl implements ChatService {

    private static final Logger log = LoggerFactory.getLogger(ChatServiceImpl.class);
    private final ChatRoomRepository chatRoomRepository;
    private final MessageRepository messageRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final UserRepository userRepository;
    private final RedisMessageBroker redisMessageBroker;
    private final MessageSequenceService messageSequenceService;
    private final WebSocketSessionManager webSocketSessionManager;

    public ChatServiceImpl(
        ChatRoomRepository chatRoomRepository,
        MessageRepository messageRepository,
        ChatRoomMemberRepository chatRoomMemberRepository,
        UserRepository userRepository,
        RedisMessageBroker redisMessageBroker,
        MessageSequenceService messageSequenceService,
        WebSocketSessionManager webSocketSessionManager
    ) {
        this.chatRoomRepository = chatRoomRepository;
        this.messageRepository = messageRepository;
        this.chatRoomMemberRepository = chatRoomMemberRepository;
        this.userRepository = userRepository;
        this.redisMessageBroker = redisMessageBroker;
        this.messageSequenceService = messageSequenceService;
        this.webSocketSessionManager = webSocketSessionManager;
    }

    @Cacheable(value = {"chatRooms"}, key = "#chatRoom.id")
    public ChatRoomDto chatRoomToDto(ChatRoom chatRoom) {
        var memberCount = chatRoomMemberRepository.countActiveMembersInRoom(chatRoom.getId())
            .intValue();
        var lastMessage = messageRepository.findLatestMessage(chatRoom.getId())
            .map(it -> messageToDto(it))
            .orElse(null);

        return new ChatRoomDto(
            chatRoom.getId(),
            chatRoom.getName(),
            chatRoom.getDescription(),
            chatRoom.getType(),
            chatRoom.getImageUrl(),
            chatRoom.getActive(),
            chatRoom.getMaxMembers(),
            memberCount,
            userToDto(chatRoom.getCreatedBy()),
            chatRoom.getCreatedAt(),
            lastMessage
        );
    }

    private MessageDto messageToDto(Message message) {
        return new MessageDto(
            message.getId(),
            message.getChatRoom().getId(),
            userToDto(message.getSender()),
            message.getType(),
            message.getContent(),
            message.getIsEdited(),
            message.getIsDeleted(),
            message.getCreatedAt(),
            message.getEditedAt(),
            message.getSequenceNumber()
        );
    }

    private ChatRoomMemberDto memberToDto(ChatRoomMember member) {
        return new ChatRoomMemberDto(
            member.getId(),
            userToDto(member.getUser()),
            member.getRole(),
            member.getIsActive(),
            member.getLastReadMessageId(),
            member.getJoinedAt(),
            member.getLeftAt()
        );
    }

    @Cacheable(value = {"users"}, key = "#user.id")
    public UserDto.UserDataDto userToDto(User user) {
        return new UserDto.UserDataDto(
            user.getId(),
            user.getUsername(),
            user.getDisplayName(),
            user.getProfileImageUrl(),
            user.getStatus(),
            user.getIsActive(),
            user.getLastSeenAt(),
            user.getCreatedAt()
        );
    }

    @CacheEvict(value = {"chatRooms"}, allEntries = true)
    @Transactional
    @Override
    public ChatRoomDto createChatRoom(CreateChatRoomRequest request, Long createdBy) {
        var creator = userRepository.findById(createdBy)
            .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: $createdBy"));

        var chatRoom = new ChatRoom(
            request.name(),
            request.description(),
            request.type(),
            request.imageUrl(),
            request.maxMembers(),
            creator
        );

        var savedRoom = chatRoomRepository.save(chatRoom);

        var ownerMember = new ChatRoomMember(
            savedRoom,
            creator,
            MemberRole.OWNER
        );
        chatRoomMemberRepository.save(ownerMember);

        // 생성자 세션 갱신
        if (webSocketSessionManager.isUserOnlineLocally(creator.getId())) {
            webSocketSessionManager.joinRoom(creator.getId(), savedRoom.getId());
        }

        return chatRoomToDto(savedRoom);
    }

    @Cacheable(value = {"chatRooms"}, key = "#roomId")
    @Transactional(readOnly = true)
    @Override
    public ChatRoomDto getChatRoom(Long roomId) {
        var chatRoom = chatRoomRepository.findById(roomId)
            .orElseThrow(() -> new IllegalArgumentException("채팅방을 찾을 수 없습니다: $roomId"));
        return chatRoomToDto(chatRoom);
    }

    @Transactional(readOnly = true)
    @Override
    public Page<ChatRoomDto> getChatRooms(Long userId, Pageable pageable) {
        Page<ChatRoom> chatRooms = chatRoomRepository.findUserChatRooms(userId, pageable);

        return chatRooms.stream()
            .map(this::chatRoomToDto)
            .collect(Collectors.collectingAndThen(
                Collectors.toList(),
                list -> new PageImpl<>(list, pageable, chatRooms.getTotalElements())
            ));
    }

    @Transactional(readOnly = true)
    @Override
    public List<ChatRoomDto> searchChatRooms(String query, Long userId) {
        List<ChatRoom> chatRooms;
        if (query.isBlank()) {
            chatRooms = chatRoomRepository.findByIsActiveTrueOrderByCreatedAtDesc();
        } else {
            chatRooms = chatRoomRepository.findByNameContainingIgnoreCaseAndIsActiveTrueOrderByCreatedAtDesc(
                query
            );
        }

        return chatRooms.stream()
            .map(it -> chatRoomToDto(it))
            .toList();
    }

    @Caching(evict = {
        @CacheEvict(value = {"chatRoomMembers"}, key = "#roomId"),
        @CacheEvict(value = {"chatRooms"}, key = "#roomId")
    })
    @Transactional
    @Override
    public void joinChatRoom(Long roomId, Long userId) {
        // 채팅방 확인
        var chatRoom = chatRoomRepository.findById(roomId)
            .orElseThrow(() -> new IllegalArgumentException("채팅방을 찾을 수 없습니다: $roomId"));

        // 사용자 확인
        var user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: $userId"));

        // 이미 참여중인지 확인
        if (chatRoomMemberRepository.existsByChatRoomIdAndUserIdAndIsActiveTrue(roomId, userId)) {
            throw new IllegalStateException("이미 참여한 채팅방입니다");
        }

        var member = new ChatRoomMember(
            chatRoom,
            user,
            MemberRole.MEMBER
        );
        chatRoomMemberRepository.save(member);

        if (webSocketSessionManager.isUserOnlineLocally(userId)) {
            webSocketSessionManager.joinRoom(userId, roomId);
        }
    }

    @Caching(evict = {
        @CacheEvict(value = {"chatRoomMembers"}, key = "#roomId"),
        @CacheEvict(value = {"chatRooms"}, key = "#roomId")
    })
    @Transactional
    @Override
    public void leaveChatRoom(Long roomId, Long userId) {
        chatRoomMemberRepository.leaveChatRoom(roomId, userId);
    }

    @Cacheable(value = {"chatRoomMembers"}, key = "#roomId")
    @Transactional(readOnly = true)
    @Override
    public List<ChatRoomMemberDto> getChatRoomMembers(Long roomId) {
        return chatRoomMemberRepository.findByChatRoomIdAndIsActiveTrue(roomId).stream()
            .map(it -> memberToDto(it))
            .toList();
    }

    @Override
    public MessageDto sendMessage(SendMessageRequest request, Long senderId) {
        var chatRoom = chatRoomRepository.findById(request.chatRoomId())
            .orElseThrow(() -> new IllegalArgumentException(
                "채팅방을 찾을 수 없습니다: %d".formatted(request.chatRoomId())));

        var sender = userRepository.findById(senderId)
            .orElseThrow(
                () -> new IllegalArgumentException("사용자를 찾을 수 없습니다: %d".formatted(senderId))
            );

        chatRoomMemberRepository.findByChatRoomIdAndUserIdAndIsActiveTrue(request.chatRoomId(),
                senderId)
            .orElseThrow(() -> new IllegalArgumentException("채팅방에 참여하지 않은 사용자입니다."));

        var sequenceNumber = messageSequenceService.getNextSequence(request.chatRoomId());

        var message = new Message(
            request.content(),
            request.type() == null ? null : MessageType.TEXT,
            chatRoom,
            sender,
            sequenceNumber
        );
        var savedMessage = messageRepository.save(message);

        var chatMessage = new ChatMessage(
            savedMessage.getId(),
            savedMessage.getContent() == null ? "" : savedMessage.getContent(),
            savedMessage.getType(),
            savedMessage.getSender().getId(),
            savedMessage.getSender().getDisplayName(),
            savedMessage.getSequenceNumber(),
            savedMessage.getChatRoom().getId(),
            savedMessage.getCreatedAt()
        );

        // 1. 로컬 세션에 즉시 전송 (실시간 응답성 보장)
        webSocketSessionManager.sendMessageToLocalRoom(request.chatRoomId(), chatMessage);

        // 2. 다른 서버 인스턴스에 브로드캐스트 (자신을 제외)
        try {
            redisMessageBroker.broadcastToRoom(
                request.chatRoomId(),
                chatMessage,
                redisMessageBroker.getServerId()
            );
        } catch (Exception e) {
            log.error("Failed to broadcast message via Redis: %s".formatted(e.getMessage()), e);
        }

        return messageToDto(savedMessage);
    }

    @Transactional(readOnly = true)
    @Override
    public Page<MessageDto> getMessages(Long roomId, Long userId, Pageable pageable) {
        if (!chatRoomMemberRepository.existsByChatRoomIdAndUserIdAndIsActiveTrue(roomId, userId)) {
            throw new IllegalArgumentException("채팅방 멤버가 아닙니다");
        }

        Page<Message> messages = messageRepository.findByChatRoomId(roomId, pageable);
        return messages.stream()
            .map(it -> messageToDto(it))
            .collect(Collectors.collectingAndThen(
                Collectors.toList(),
                list -> new PageImpl<>(list, pageable, messages.getTotalElements())
            ));
    }

    @Override
    public MessagePageResponse getMessagesByCursor(MessagePageRequest request, Long userId) {
        if (!chatRoomMemberRepository.existsByChatRoomIdAndUserIdAndIsActiveTrue(
            request.chatRoomId(), userId)) {
            throw new IllegalArgumentException("채팅방 멤버가 아닙니다");
        }

        var pageable = PageRequest.of(0, request.limit());
        var cursor = request.cursor();

        List<Message> messages;
        if (cursor == null) {
            messages = messageRepository.findLatestMessages(request.chatRoomId(), pageable);
        } else if (request.direction() == ChatDto.MessageDirection.BEFORE) {
            messages = messageRepository.findMessagesBefore(request.chatRoomId(), cursor, pageable);
        } else {
            messages = messageRepository.findMessagesAfter(request.chatRoomId(), cursor, pageable)
                .reversed();
        }

        var messageDtos = messages.stream().map(it -> messageToDto(it)).toList();

        // 다음/이전 커서 계산
        var nextCursor = !messageDtos.isEmpty() ? messageDtos.getLast().id() : null;
        var prevCursor = !messageDtos.isEmpty() ? messageDtos.getFirst().id() : null;

        // 추가 데이터 존재 여부 확인
        var hasNext = messages.size() == request.limit();
        var hasPrev = cursor != null;

        return new MessagePageResponse(
            messageDtos,
            nextCursor,
            prevCursor,
            hasNext,
            hasPrev
        );
    }
}
