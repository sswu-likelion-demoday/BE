package com.sujeongring.domain.chat.controller;

import com.sujeongring.domain.chat.dto.request.ChatReadRequest;
import com.sujeongring.domain.chat.dto.response.ChatMessageListResponse;
import com.sujeongring.domain.chat.dto.response.ChatRoomDetailResponse;
import com.sujeongring.domain.chat.dto.response.ChatRoomListResponse;
import com.sujeongring.domain.chat.service.ChatMessageService;
import com.sujeongring.domain.chat.service.ChatRoomService;
import com.sujeongring.global.common.ApiResponse;
import com.sujeongring.global.auth.CurrentUserId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/chat-rooms")
@Tag(name = "채팅방", description = "채팅방/메시지 조회 및 읽음 처리 API")
public class ChatRoomController {

    private final ChatRoomService chatRoomService;
    private final ChatMessageService chatMessageService;

    @GetMapping
    @Operation(summary = "채팅방 목록 조회", description = "현재 사용자의 채팅방 목록과 미읽음 정보를 조회합니다.")
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
    @Operation(summary = "채팅방 상세 조회", description = "현재 관계 단계 및 현재 퀘스트 정보를 포함한 채팅방 상세를 조회합니다.")
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
    @Operation(summary = "메시지 목록 조회", description = "커서 기반으로 채팅 메시지 목록을 조회합니다.")
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

    @PatchMapping("/{chatRoomId}/read")
    @Operation(summary = "읽음 처리", description = "채팅방 마지막 읽은 메시지를 갱신합니다.")
    public ApiResponse<Void> markAsRead(
            @PathVariable Long chatRoomId,
            @CurrentUserId Long userId,
            @Valid @RequestBody ChatReadRequest request
    ) {
        chatRoomService.markAsRead(
                chatRoomId,
                userId,
                request.lastReadMessageId()
        );

        return ApiResponse.ok();
    }
}
