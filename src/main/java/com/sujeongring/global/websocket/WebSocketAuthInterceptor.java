package com.sujeongring.global.websocket;

import com.sujeongring.domain.auth.exception.AuthErrorCode;
import com.sujeongring.global.error.exception.BaseException;
import com.sujeongring.global.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
@RequiredArgsConstructor
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public Message<?> preSend(
            Message<?> message,
            MessageChannel channel
    ) {
        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(
                        message,
                        StompHeaderAccessor.class
                );

        if (accessor == null) {
            return message;
        }

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            authenticate(accessor);
        }

        return message;
    }

    private void authenticate(StompHeaderAccessor accessor) {

        String authorization =
                accessor.getFirstNativeHeader(HttpHeaders.AUTHORIZATION);

        if (authorization == null
                || !authorization.startsWith(BEARER_PREFIX)) {
            throw new BaseException(AuthErrorCode.UNAUTHORIZED);
        }

        String token =
                authorization.substring(BEARER_PREFIX.length());

        if (!jwtTokenProvider.validateToken(token)) {
            throw new BaseException(AuthErrorCode.UNAUTHORIZED);
        }

        Long userId = jwtTokenProvider.getUserId(token);

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        userId,
                        null,
                        Collections.emptyList()
                );

        accessor.setUser(authentication);
    }
}