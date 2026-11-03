package com.sujeongring.domain.user.controller;

import com.sujeongring.domain.user.dto.response.BlockListResponse;
import com.sujeongring.domain.user.service.BlockService;
import com.sujeongring.global.auth.CurrentUserId;
import com.sujeongring.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
@Tag(name = "차단", description = "사용자 차단/차단 해제/차단 목록 조회 API")
public class BlockController {

    private final BlockService blockService;

    @PostMapping("/{targetUserId}/block")
    @Operation(summary = "사용자 차단", description = "상대 사용자를 차단하고 관련 요청/관계 정책을 적용합니다.")
    public ApiResponse<Void> blockUser(
            @CurrentUserId Long userId,
            @PathVariable Long targetUserId
    ) {
        blockService.blockUser(userId, targetUserId);
        return ApiResponse.success("사용자를 차단했습니다.", null);
    }

    @GetMapping("/blocks")
    @Operation(summary = "차단 목록 조회", description = "현재 사용자의 차단 목록을 조회합니다.")
    public ApiResponse<BlockListResponse> getBlockedUsers(
            @CurrentUserId Long userId
    ) {
        BlockListResponse response = blockService.getBlockedUsers(userId);
        return ApiResponse.success("차단 목록을 조회하였습니다.", response);
    }

    @DeleteMapping("/{targetUserId}/block")
    @Operation(summary = "차단 해제", description = "상대 사용자에 대한 차단을 해제합니다.")
    public ApiResponse<Void> unblockUser(
            @CurrentUserId Long userId,
            @PathVariable Long targetUserId
    ) {
        blockService.unblockUser(userId, targetUserId);
        return ApiResponse.success("차단이 해제되었습니다.", null);
    }
}
