package com.luna.lilitalk.websocket.interceptor;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

@Component
public class WebSocketHandshakeInterceptor implements HandshakeInterceptor {

    private static final Logger log = LoggerFactory.getLogger(WebSocketHandshakeInterceptor.class);

    @Override
    public boolean beforeHandshake(
        ServerHttpRequest request,
        ServerHttpResponse response,
        WebSocketHandler wsHandler,
        Map<String, Object> attributes
    ) throws Exception {
        try {
            // ws://localhost:8080/chat?userId=123
            var uri = request.getURI();
            var query = uri.getQuery();

            if (query != null) {
                var param = parseQuery(query);
                if (param.get("userId") == null) {
                    return false;
                } else {
                    attributes.put("userId", param.get("userId"));
                    return true;
                }
            } else {
                return false;
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return false;
        }
    }

    @Override
    public void afterHandshake(
        ServerHttpRequest request,
        ServerHttpResponse response,
        WebSocketHandler wsHandler,
        @Nullable Exception exception
    ) {
        if (exception != null) {
            log.error("WebSocket HandshakeInterceptor exception", exception);
        } else {
            log.info("WebSocket HandshakeInterceptor");
        }
    }

    private Map<String, String> parseQuery(String query) {
        return Arrays.stream(query.split("&"))
            .map(param -> param.split("=", 2))
            .filter(parts -> parts.length == 2)
            .collect(Collectors.toMap(
                parts -> parts[0],
                parts -> parts[1],
                (existing, replacement) -> existing
            ));
    }
}
