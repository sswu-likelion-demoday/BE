package com.sujeongring.domain.chat.controller;

import com.sujeongring.domain.chat.dto.response.ChatRoomListResponse;
import com.sujeongring.domain.chat.service.ChatRoomService;
import com.sujeongring.global.common.ApiResponse;
import com.sujeongring.global.auth.CurrentUserId;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/chat-rooms")
public class ChatRoomController {

    private final ChatRoomService chatRoomService;

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
}
