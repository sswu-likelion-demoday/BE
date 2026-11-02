
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

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
@RequestMapping("/match-requests")
public class MatchRequestController {

    private final MatchRequestService matchRequestService;

    @PostMapping
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
    public ApiResponse<MatchRequestListResponse> getMatchRequests(
            @CurrentUserId Long userId
    ) {
        return ApiResponse.success(
                "채팅 요청 내역을 조회하였습니다.",
                matchRequestService.getMatchRequests(userId)
        );
    }


    @GetMapping("/{requestId}")
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
