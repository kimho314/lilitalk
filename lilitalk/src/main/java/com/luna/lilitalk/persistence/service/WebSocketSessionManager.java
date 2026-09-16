package com.luna.lilitalk.persistence.service;

import com.luna.lilitalk.domain.dto.WebSocketDto.ChatMessage;
import com.luna.lilitalk.persistence.redis.RedisMessageBroker;
import com.luna.lilitalk.persistence.repository.ChatRoomMemberRepository;
import jakarta.annotation.PostConstruct;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import tools.jackson.databind.ObjectMapper;

@Service
public class WebSocketSessionManager {

    private static final Logger log = LoggerFactory.getLogger(WebSocketSessionManager.class);
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;
    private final RedisMessageBroker redisMessageBroker;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final ConcurrentHashMap<Long, Set<WebSocketSession>> userSession = new ConcurrentHashMap<>();

    private final String serverRoomsKeyPrefix = "chat:server:rooms";

    public WebSocketSessionManager(
        RedisTemplate<String, String> redisTemplate,
        ObjectMapper objectMapper,
        RedisMessageBroker redisMessageBroker,
        ChatRoomMemberRepository chatRoomMemberRepository
    ) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.redisMessageBroker = redisMessageBroker;
        this.chatRoomMemberRepository = chatRoomMemberRepository;
    }

    @PostConstruct
    void initialize() {
        redisMessageBroker.setSetLocalMessageHandler((roomId, sessions) ->
            sendMessageToLocalRoom(roomId, sessions, null));

    }

    public void addSession(Long userId, WebSocketSession session) {
        log.info("Adding session $userId to server");
        userSession.computeIfAbsent(userId, k -> new HashSet<>()).add(session);
    }

    public void removeSession(Long userId, WebSocketSession session) {
        Set<WebSocketSession> webSocketSessions = userSession.get(userId);
        if (!webSocketSessions.isEmpty()) {
            webSocketSessions.remove(session);
        }
        if (userSession.get(userId).isEmpty()) {
            userSession.remove(userId);

            long totalConnectedUsers = userSession.values().stream()
                .flatMap(s -> s.stream()
                    .filter(WebSocketSession::isOpen)
                ).count();

            if (totalConnectedUsers == 0) {
                String serverId = redisMessageBroker.getServerId();
                String serverRoomKey = "%s%s".formatted(serverRoomsKeyPrefix, serverId);

                Set<String> subscribedRooms = redisTemplate.opsForSet().members(serverRoomKey);
                if (subscribedRooms == null) {
                    subscribedRooms = new HashSet<>();
                }

                subscribedRooms.forEach(roomIdStr -> {
                    try {
                        Long roomId = Long.parseLong(roomIdStr);
                        redisTemplate.opsForSet().remove(serverRoomKey, roomId);
                    } catch (NumberFormatException e) {
                        log.error("Failed to parse roomId: {}", roomIdStr, e);
                    }
                });
                redisTemplate.delete(serverRoomKey);
                log.info("Removed {} {}", totalConnectedUsers, subscribedRooms);
            }
        }
    }

    public void joinRoom(Long userId, Long roomId) {
        String serverId = redisMessageBroker.getServerId();
        String serverRoomKey = "%s%s".formatted(serverRoomsKeyPrefix, serverId);

        boolean wasAlreadySubscribed =
            redisTemplate.opsForSet().isMember(serverRoomKey, roomId.toString()) == true;

        if (!wasAlreadySubscribed) {
            redisMessageBroker.subscribeToRoom(roomId);
        }

        redisTemplate.opsForSet().add(serverRoomKey, roomId.toString());

        log.info("Joined $roomId for $userId $serverId to server $serverRoomKey");
    }

    public void sendMessageToLocalRoom(Long roomId, ChatMessage message,
        @Nullable Long excludeUserId) {
        String json = objectMapper.writeValueAsString(message);

        // 채팅방을 확인을 하면서, 관련된 방에 메시지를 전송
        userSession.forEach((userId, session) -> {
            if (!Objects.equals(userId, excludeUserId)) {
                Boolean isMember = chatRoomMemberRepository.existsByChatRoomIdAndUserIdAndIsActiveTrue(
                    roomId,
                    userId
                );
                if (isMember) {
                    Set<WebSocketSession> closedSessions = new HashSet<>();
                    session.forEach(s -> {
                        if (s.isOpen()) {
                            try {
                                s.sendMessage(new TextMessage(json));
                                log.info("Sending message to local room {}", roomId);
                            } catch (Exception e) {
                                log.error(e.getMessage(), e);
                                closedSessions.add(s);
                            }
                        }
                    });
                    if (!closedSessions.isEmpty()) {
                        session.removeAll(closedSessions);
                    }
                } else {
                    log.info("not member of {} for {}", roomId, userId);
                }
            }
        });
    }

    public Boolean isUserOnlineLocally(Long userId) {
        Set<WebSocketSession> sessions = userSession.get(userId);
        if (sessions == null) {
            return false;
        }
        List<WebSocketSession> openSessions = sessions.stream()
            .filter(WebSocketSession::isOpen)
            .toList();

        if (openSessions.size() != sessions.size()) {
            var closedSessions = sessions.stream()
                .filter(it -> !it.isOpen())
                .toList();
            closedSessions.forEach(sessions::remove);

            if (sessions.isEmpty()) {
                userSession.remove(userId);
            }
        }

        return !openSessions.isEmpty();
    }
}
