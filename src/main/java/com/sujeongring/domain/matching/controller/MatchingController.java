package com.sujeongring.domain.matching.controller;

import com.sujeongring.domain.matching.dto.response.DailyFreeMatchingStatusResponse;
import com.sujeongring.domain.matching.dto.response.MatchingHomeResponse;
import com.sujeongring.domain.matching.dto.response.NormalMatchingResponse;
import com.sujeongring.domain.matching.entity.MatchRecommendation;
import com.sujeongring.domain.matching.service.NormalMatching.DailyFreeMatchingService;
import com.sujeongring.domain.matching.service.NormalMatching.MatchingHomeService;
import com.sujeongring.domain.matching.service.NormalMatching.NormalMatchingService;
import com.sujeongring.global.auth.CurrentUserId;
import com.sujeongring.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.sujeongring.domain.matching.dto.response.SajuMatchingResponse;
import com.sujeongring.domain.matching.service.SajuMatching.SajuMatchingService;
import com.sujeongring.domain.user.dto.request.SajuSettingsRequest;
import com.sujeongring.domain.user.dto.response.SajuSettingsResponse;
import com.sujeongring.domain.user.service.SajuProfileService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequiredArgsConstructor
@RequestMapping("/matching")
@Tag(name = "매칭", description = "일반/사주 매칭 추천 및 매칭 홈/이용 상태 API")
public class MatchingController {

    private final DailyFreeMatchingService dailyFreeMatchingService;
    private final NormalMatchingService normalMatchingService;
    private final MatchingHomeService matchingHomeService;
    private final SajuMatchingService sajuMatchingService;
    private final SajuProfileService sajuProfileService;

    @GetMapping("/home")
    @Operation(summary = "매칭 홈 조회", description = "매칭 홈 요약 정보를 조회합니다.")
    public ApiResponse<MatchingHomeResponse> getMatchingHome(
            @CurrentUserId Long userId
    ) {
        return ApiResponse.success(
                "매칭 홈을 조회하였습니다.",
                matchingHomeService.getHome(userId)
        );
    }

    @GetMapping("/status")
    @Operation(summary = "매칭 이용 상태 조회", description = "주간 무료 매칭 횟수 및 사용 상태를 조회합니다.")
    public ApiResponse<DailyFreeMatchingStatusResponse> getMatchingStatus(
            @CurrentUserId Long userId
    ) {
        DailyFreeMatchingStatusResponse response =
                dailyFreeMatchingService.getStatus(userId);

        return ApiResponse.success(
                "매칭 이용 상태를 조회하였습니다.",
                response
        );
    }

    @PostMapping("/recommendations")
    @Operation(summary = "일반 매칭 추천", description = "온보딩 기반 일반 매칭 후보 1명을 추천합니다.")
    public ApiResponse<NormalMatchingResponse> matchNormal(
            @CurrentUserId Long userId
    ) {
        MatchRecommendation recommendation =
                normalMatchingService.matchNormal(userId);

        return ApiResponse.success(
                "매칭 후보를 추천하였습니다.",
                NormalMatchingResponse.from(recommendation)
        );
    }


    @PostMapping("/saju/recommendations")
    @Operation(summary = "사주 매칭 추천", description = "사주 궁합 기반 매칭 후보 1명을 추천합니다.")
    public ApiResponse<SajuMatchingResponse> matchSaju(
            @CurrentUserId Long userId
    ) {
        MatchRecommendation recommendation =
                sajuMatchingService.matchSaju(userId);

        return ApiResponse.success(
                "사주 매칭 후보를 추천하였습니다.",
                SajuMatchingResponse.from(recommendation)
        );
    }


    // 사주 매칭 화면 - 현재 설정 조회
    @GetMapping("/saju")
    @Operation(summary = "사주 매칭 설정 조회", description = "사주 매칭 추천에 사용할 설정을 조회합니다.")
    public ApiResponse<SajuSettingsResponse> getSajuSettings(
            @CurrentUserId Long userId
    ) {
        SajuSettingsResponse response =
                sajuProfileService.getSettings(userId);

        return ApiResponse.success(
                "사주 매칭 설정을 조회하였습니다.",
                response
        );
    }

    // 사주 매칭 화면 - 설정 저장
    @PutMapping("/saju")
    @Operation(summary = "사주 매칭 설정 저장", description = "사주 매칭 추천에 사용할 설정을 저장/수정합니다.")
    public ApiResponse<SajuSettingsResponse> updateSajuSettings(
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