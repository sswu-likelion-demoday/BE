package com.sujeongring.global.websocket;

import com.sujeongring.domain.auth.exception.AuthErrorCode;
import com.sujeongring.domain.chat.exception.ChatErrorCode;
import com.sujeongring.domain.chat.service.ChatRoomService;
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
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
@RequiredArgsConstructor
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private static final String BEARER_PREFIX = "Bearer ";
    private final ChatRoomService chatRoomService;

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

        if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            authorizeSubscribe(accessor);
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

    private void authorizeSubscribe(StompHeaderAccessor accessor) {

        String destination = accessor.getDestination();

        if (destination == null) {
            return;
        }

        // 채팅방 구독일 때만 검사
        if (!destination.startsWith("/sub/chat/")) {
            return;
        }

        Authentication authentication =
                (Authentication) accessor.getUser();

        if (authentication == null) {
            throw new BaseException(AuthErrorCode.UNAUTHORIZED);
        }

        Long userId = (Long) authentication.getPrincipal();

        String chatRoomIdValue =
                destination.substring("/sub/chat/".length());

        Long chatRoomId;

        try {
            chatRoomId = Long.valueOf(chatRoomIdValue);
        } catch (NumberFormatException e) {
            throw new BaseException(ChatErrorCode.CHAT_ROOM_NOT_FOUND);
        }

        chatRoomService.getAccessibleChatRoom(
                chatRoomId,
                userId
        );
    }
}