
package com.sujeongring.domain.matching.controller;

import com.sujeongring.domain.matching.dto.request.CreateMatchRequestRequest;
import com.sujeongring.domain.matching.dto.response.matchrequest.AcceptMatchRequestResponse;
import com.sujeongring.domain.matching.dto.response.matchrequest.CreateMatchRequestResponse;
import com.sujeongring.domain.matching.dto.response.matchrequest.MatchRequestDetailResponse;
import com.sujeongring.domain.matching.dto.response.matchrequest.MatchRequestListResponse;
import com.sujeongring.domain.matching.entity.MatchRequest;
import com.sujeongring.domain.matching.enums.MatchRequestStatus;
import com.sujeongring.domain.matching.exception.MatchRequestErrorCode;
import com.sujeongring.domain.matching.service.MatchRequestService;
import com.sujeongring.global.auth.CurrentUserId;
import com.sujeongring.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
@RequestMapping("/match-requests")
@Tag(name = "채팅 요청", description = "매칭 후 채팅 요청 생성/조회/수락/거절 API")
public class MatchRequestController {

    private final MatchRequestService matchRequestService;

    @PostMapping
    @Operation(summary = "채팅 요청 생성", description = "추천 결과를 기반으로 상대에게 채팅 요청을 보냅니다.")
    public ApiResponse<CreateMatchRequestResponse> createMatchRequest(
            @CurrentUserId Long userId,
            @Valid @RequestBody CreateMatchRequestRequest request
    ) {
        CreateMatchRequestResponse response =
                matchRequestService.create(
                        userId,
                        request.recommendationId()
                );

        return ApiResponse.success(
                "채팅 요청을 보냈습니다.",
                response
        );
    }

    @GetMapping
    @Operation(summary = "채팅 요청 목록 조회", description = "보낸/받은 채팅 요청 목록을 조회합니다.")
    public ApiResponse<MatchRequestListResponse> getMatchRequests(
            @CurrentUserId Long userId
    ) {
        return ApiResponse.success(
                "채팅 요청 내역을 조회하였습니다.",
                matchRequestService.getMatchRequests(userId)
        );
    }


    @GetMapping("/{requestId}")
    @Operation(summary = "채팅 요청 상세 조회", description = "채팅 요청 상세 및 상대 프로필 정보를 조회합니다.")
    public ApiResponse<MatchRequestDetailResponse> getMatchRequestDetail(
            @CurrentUserId Long userId,
            @PathVariable Long requestId
    ) {
        return ApiResponse.success(
                "요청 상대 프로필을 조회하였습니다.",
                matchRequestService.getMatchRequestDetail(userId, requestId)
        );
    }


    @PostMapping("/{requestId}/accept")
    @Operation(summary = "채팅 요청 수락", description = "채팅 요청을 수락하고 관계/채팅방/퀘스트를 생성합니다.")
    public ApiResponse<AcceptMatchRequestResponse> acceptMatchRequest(
            @CurrentUserId Long userId,
            @PathVariable Long requestId
    ) {
        return ApiResponse.success(
                "채팅 요청을 수락하였습니다.",
                matchRequestService.acceptMatchRequest(userId, requestId)
        );
    }

    @PatchMapping("/{requestId}/reject")
    @Operation(summary = "채팅 요청 거절", description = "채팅 요청을 거절 처리합니다.")
    public ApiResponse<Void> rejectMatchRequest(
            @CurrentUserId Long userId,
            @PathVariable Long requestId
    ) {
        matchRequestService.rejectMatchRequest(userId, requestId);

        return ApiResponse.success(
                "채팅 요청을 거절하였습니다.",
                null
        );
    }
}
