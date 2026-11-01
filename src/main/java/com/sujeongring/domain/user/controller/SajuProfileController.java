
package com.sujeongring.domain.user.controller;

import com.sujeongring.domain.user.dto.request.SajuSettingsRequest;
import com.sujeongring.domain.user.dto.response.SajuSettingsResponse;
import com.sujeongring.domain.user.service.SajuProfileService;

import com.sujeongring.global.auth.CurrentUserId;
import com.sujeongring.global.common.ApiResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users/me/saju-settings")
public class SajuProfileController {

    private final SajuProfileService sajuProfileService;

    // 사주 매칭 설정 조회
    @GetMapping
    public ApiResponse<SajuSettingsResponse> getSettings(
            @CurrentUserId Long userId
    ) {
        SajuSettingsResponse response =
                sajuProfileService.getSettings(userId);

        return ApiResponse.success(
                "사주 매칭 설정을 조회하였습니다.",
                response
        );
    }

    // 사주 매칭 설정 저장 및 수정
    @PutMapping
    public ApiResponse<SajuSettingsResponse> updateSettings(
            @CurrentUserId Long userId,
            @Valid @RequestBody SajuSettingsRequest request
    ) {
        SajuSettingsResponse response =
                sajuProfileService.updateSettings(userId, request);

        return ApiResponse.success(
                "사주 매칭 설정을 저장하였습니다.",
                response
        );
    }
}
