package com.luna.lilitalk.websocket.interceptor;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
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
            String userId = null;
            if (request instanceof ServletServerHttpRequest servletRequest) {
                userId = servletRequest.getServletRequest().getParameter("userId");
            }

            if (userId == null) {
                var uri = request.getURI();
                var query = uri.getQuery();
                if (query != null) {
                    var param = parseQuery(query);
                    userId = param.get("userId");
                }
            }

            if (userId == null || userId.isBlank()) {
                log.warn("userId is missing or empty in WebSocket handshake: {}", request.getURI());
                return false;
            }

            attributes.put("userId", userId.trim());
            return true;
        } catch (Exception e) {
            log.error("WebSocket HandshakeInterceptor exception", e);
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
            log.info("WebSocket HandshakeInterceptor completed successfully for URI: {}",
                request.getURI());
        }
    }

    private Map<String, String> parseQuery(String query) {
        return Arrays.stream(query.split("&"))
            .map(param -> param.split("=", 2))
            .filter(parts -> parts.length == 2)
            .collect(Collectors.toMap(
                parts -> URLDecoder.decode(parts[0], StandardCharsets.UTF_8),
                parts -> URLDecoder.decode(parts[1], StandardCharsets.UTF_8),
                (existing, replacement) -> existing
            ));
    }
}
