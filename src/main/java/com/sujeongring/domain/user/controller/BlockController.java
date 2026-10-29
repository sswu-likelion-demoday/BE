package com.sujeongring.domain.user.controller;

import com.sujeongring.domain.user.service.BlockService;
import com.sujeongring.global.auth.CurrentUserId;
import com.sujeongring.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
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
}
