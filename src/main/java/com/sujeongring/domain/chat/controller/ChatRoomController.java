package com.sujeongring.domain.chat.controller;

import com.sujeongring.domain.chat.dto.response.ChatMessageListResponse;
import com.sujeongring.domain.chat.dto.response.ChatRoomDetailResponse;
import com.sujeongring.domain.chat.dto.response.ChatRoomListResponse;
import com.sujeongring.domain.chat.service.ChatMessageService;
import com.sujeongring.domain.chat.service.ChatRoomService;
import com.sujeongring.global.common.ApiResponse;
import com.sujeongring.global.auth.CurrentUserId;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/chat-rooms")
public class ChatRoomController {

    private final ChatRoomService chatRoomService;
    private final ChatMessageService chatMessageService;

    @GetMapping
    public ApiResponse<ChatRoomListResponse> getChatRooms(
            @CurrentUserId Long userId
    ) {
        ChatRoomListResponse response =
                chatRoomService.getChatRooms(userId);

        return ApiResponse.success(
                "채팅방 목록을 조회하였습니다.",
                response
        );
    }

    @GetMapping("/{chatRoomId}")
    public ApiResponse<ChatRoomDetailResponse> getChatRoom(
            @PathVariable Long chatRoomId,
            @CurrentUserId Long userId
    ) {
        ChatRoomDetailResponse response =
                chatRoomService.getChatRoom(
                        chatRoomId,
                        userId
                );

        return ApiResponse.success(
                "채팅방을 조회하였습니다.",
                response
        );
    }

    @GetMapping("/{chatRoomId}/messages")
    public ApiResponse<ChatMessageListResponse> getMessages(
            @PathVariable Long chatRoomId,
            @RequestParam(required = false) Long cursor,
            @RequestParam(defaultValue = "30") int size,
            @CurrentUserId Long userId
    ) {
        ChatMessageListResponse response =
                chatMessageService.getMessages(
                        chatRoomId,
                        userId,
                        cursor,
                        size
                );

        return ApiResponse.success(
                "메시지 내역을 조회하였습니다.",
                response
        );
    }
}
