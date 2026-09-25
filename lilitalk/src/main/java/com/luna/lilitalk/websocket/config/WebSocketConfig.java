package com.luna.lilitalk.websocket.config;

import com.luna.lilitalk.websocket.handler.ChatWebSocketHandler;
import com.luna.lilitalk.websocket.interceptor.WebSocketHandshakeInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@EnableWebSocket
@Configuration
public class WebSocketConfig implements WebSocketConfigurer {

    private final ChatWebSocketHandler chatWebSocketHandler;
    private final WebSocketHandshakeInterceptor webSocketHandshakeInterceptor;

    public WebSocketConfig(
        ChatWebSocketHandler chatWebSocketHandler,
        WebSocketHandshakeInterceptor webSocketHandshakeInterceptor
    ) {
        this.chatWebSocketHandler = chatWebSocketHandler;
        this.webSocketHandshakeInterceptor = webSocketHandshakeInterceptor;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(chatWebSocketHandler, "/ws/chat")
            .addInterceptors(webSocketHandshakeInterceptor)
            .setAllowedOrigins("*");
    }
}
