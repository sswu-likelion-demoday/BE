package com.sujeongring.domain.user.controller;

import com.sujeongring.domain.user.dto.response.BlockListResponse;
import com.sujeongring.domain.user.service.BlockService;
import com.sujeongring.global.auth.CurrentUserId;
import com.sujeongring.global.common.ApiResponse;
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
public class BlockController {

    private final BlockService blockService;

    @PostMapping("/{targetUserId}/block")
    public ApiResponse<Void> blockUser(
            @CurrentUserId Long userId,
            @PathVariable Long targetUserId
    ) {
        blockService.blockUser(userId, targetUserId);
        return ApiResponse.success("사용자를 차단했습니다.", null);
    }

    @GetMapping("/blocks")
    public ApiResponse<BlockListResponse> getBlockedUsers(
            @CurrentUserId Long userId
    ) {
        BlockListResponse response = blockService.getBlockedUsers(userId);
        return ApiResponse.success("차단 목록을 조회하였습니다.", response);
    }

    @DeleteMapping("/{targetUserId}/block")
    public ApiResponse<Void> unblockUser(
            @CurrentUserId Long userId,
            @PathVariable Long targetUserId
    ) {
        blockService.unblockUser(userId, targetUserId);
        return ApiResponse.success("차단이 해제되었습니다.", null);
    }
}
