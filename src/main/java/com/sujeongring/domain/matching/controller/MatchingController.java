
package com.sujeongring.domain.matching.controller;

import com.sujeongring.domain.matching.dto.response.AutoMatchingResponse;
import com.sujeongring.domain.matching.entity.MatchRecommendation;
import com.sujeongring.domain.matching.service.MatchingService;
import com.sujeongring.global.auth.CurrentUserId;
import com.sujeongring.global.common.ApiResponse;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/matchings")
public class MatchingController {

    private final MatchingService matchingService;

    @PostMapping("/normal")
    public ApiResponse<AutoMatchingResponse> matchNormal(
            @CurrentUserId Long userId
    ) {

        // 1. 랜덤 매칭 실행
        MatchRecommendation recommendation =
                matchingService.matchNormal(userId);

        // 2. Entity를 응답 DTO로 변환
        AutoMatchingResponse response =
                AutoMatchingResponse.from(recommendation);

        // 3. 공통 응답 반환
        return ApiResponse.success(
                "자동 매칭이 완료되었습니다.",
                response
        );
    }
}
