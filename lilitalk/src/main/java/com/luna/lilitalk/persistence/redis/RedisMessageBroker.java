package com.luna.lilitalk.persistence.redis;

import com.luna.lilitalk.domain.dto.WebSocketDto.ChatMessage;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentHashMap.KeySetView;
import java.util.function.BiConsumer;
import java.util.stream.Stream;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

@Service
public class RedisMessageBroker implements MessageListener {

    private static final Logger log = LoggerFactory.getLogger(RedisMessageBroker.class);
    private final RedisTemplate<String, String> redisTemplate;
    private final RedisMessageListenerContainer messageListenerContainer;
    private final ObjectMapper objectMapper;
    private final String serverId;
    private final ConcurrentHashMap<String, Long> processedMessages;
    private final KeySetView<Long, Boolean> subscribeRooms;
    @Nullable
    private BiConsumer<Long, ChatMessage> localMessageHandler;

    public RedisMessageBroker(
        RedisTemplate<String, String> redisTemplate,
        RedisMessageListenerContainer messageListenerContainer,
        ObjectMapper objectMapper
    ) {
        this.redisTemplate = redisTemplate;
        this.messageListenerContainer = messageListenerContainer;
        this.objectMapper = objectMapper;
        this.serverId =
            System.getenv("HOSTNAME") == null ? "server-%d".formatted(System.currentTimeMillis())
                : System.getenv("HOSTNAME");
        this.processedMessages = new ConcurrentHashMap<>();
        this.subscribeRooms = ConcurrentHashMap.newKeySet();
        this.localMessageHandler = null;
    }

    @PostConstruct
    void initialize() {
        log.info("Initializing RedisMessageListenerContainer");

        Thread thread = new Thread(() -> {
            try {
                Thread.sleep(30000);
            } catch (Exception e) {
                log.error("Error in initializing RedisMessageListenerContainer", e);
            }
        });
        thread.setName("redis-broker-cleanup");
        thread.setDaemon(true);
        thread.start();
    }

    @PreDestroy
    void cleanup() {
        subscribeRooms.forEach(this::unsubscribeFromRoom);
        log.info("Removing RedisMessageListenerContainer");
    }

    public String getServerId() {
        return serverId;
    }

    public void setSetLocalMessageHandler(BiConsumer<Long, ChatMessage> setLocalMessageHandler) {
        this.localMessageHandler = setLocalMessageHandler;
    }


    @Override
    public void onMessage(Message message, byte @Nullable [] pattern) {
        try {
            String json = new String(message.getBody());
            var distributedMessage = objectMapper.readValue(json, DistributedMessage.class);

            if (distributedMessage.excludeSeverId != null
                && distributedMessage.excludeSeverId.equals(serverId)) {
                log.error("excludeSeverId to $serverId");
                return;
            }

            if (processedMessages.containsKey(distributedMessage.id)) {
                log.error("processedMessages $distributedMessage");
                return;
            }

            if (localMessageHandler != null) {
                localMessageHandler.accept(distributedMessage.roomId, distributedMessage.payload);
            }

            processedMessages.put(distributedMessage.id, System.currentTimeMillis());

            if (processedMessages.size() > 10000) {
                Stream<Entry<String, Long>> oldestEntries = processedMessages.entrySet().stream()
                    .sorted(Comparator.comparing(it -> it.getValue()))
                    .limit(processedMessages.size() - 10000);
                oldestEntries.forEach(it -> processedMessages.remove(it.getKey()));
            }

            log.info("processedMessages $distributedMessage.id");

        } catch (Exception e) {
            log.error("Error in on message", e);
        }
    }

    public void subscribeToRoom(Long roomId) {
        if (subscribeRooms.add(roomId)) {
            ChannelTopic topic = new ChannelTopic("chat.room.%d".formatted(roomId));
            messageListenerContainer.addMessageListener(this, topic);
            log.info("Subscribed to $roomId");
        } else {
            log.error("Room $roomId does not exist");
        }
    }

    public void unsubscribeFromRoom(Long roomId) {
        if (subscribeRooms.remove(roomId)) {
            ChannelTopic topic = new ChannelTopic("chat.room.%d".formatted(roomId));
            messageListenerContainer.removeMessageListener(this, topic);
            log.info("Unsubscribed from $roomId");
        } else {
            log.error("Room $roomId does not exist");
        }
    }

    void broadcastToRoom(Long roomId, ChatMessage message, @Nullable String excludeSeverId) {
        try {
            DistributedMessage dMessage = new DistributedMessage(
                "%S-%d-%d".formatted(serverId, System.currentTimeMillis(), System.nanoTime()),
                serverId,
                roomId,
                excludeSeverId,
                LocalDateTime.now(),
                message
            );

            String json = objectMapper.writeValueAsString(dMessage);
            redisTemplate.convertAndSend("chat.room.%d".formatted(roomId), json);

            log.info("Broadcast to $roomId to $json");
        } catch (Exception e) {
            log.error("Error broadcast to $roomId", e);
        }
    }

    record DistributedMessage(
        String id,
        String serverId,
        Long roomId,
        @Nullable String excludeSeverId,
        LocalDateTime timestamp,
        ChatMessage payload
    ) {

    }
}
