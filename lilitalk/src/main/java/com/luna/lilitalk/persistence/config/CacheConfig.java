package com.luna.lilitalk.persistence.config;

import java.time.Duration;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import tools.jackson.databind.ObjectMapper;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        var objectMapper = new ObjectMapper();

        var configuration = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(30))
            .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(
                StringRedisSerializer.UTF_8))
            .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(
                new GenericJacksonJsonRedisSerializer(objectMapper)))
            .disableCachingNullValues();

        return RedisCacheManager.builder(connectionFactory)
            .cacheDefaults(configuration)
            .withCacheConfiguration("users",
                configuration.entryTtl(Duration.ofHours(1))) // 사용자 정보에 대해서는 1시간
            .withCacheConfiguration("chatRooms",
                configuration.entryTtl(Duration.ofMinutes(15))) // 채팅방 정보는 15분
            .withCacheConfiguration("chatRoomMembers",
                configuration.entryTtl(Duration.ofMinutes(10))) // 멤버 정보는 10분
            .withCacheConfiguration("messages",
                configuration.entryTtl(Duration.ofMinutes(5))) // 메시지는 5분
            .build();
    }
}
