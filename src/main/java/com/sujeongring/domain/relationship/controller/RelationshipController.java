package com.sujeongring.domain.relationship.controller;

import com.sujeongring.domain.relationship.dto.response.PartnerProfileResponse;
import com.sujeongring.domain.relationship.dto.response.RelationshipProgressResponse;
import com.sujeongring.domain.relationship.service.RelationshipService;
import com.sujeongring.global.auth.CurrentUserId;
import com.sujeongring.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/relationships")
@Tag(name = "관계", description = "관계 진행도/상대 프로필 조회 및 관계 종료 API")
public class RelationshipController {

    private final RelationshipService relationshipService;

    @GetMapping("/{relationshipId}")
    @Operation(summary = "관계 진행 조회", description = "단계별 퀘스트 진행 상태와 해금 정보를 조회합니다.")
    public ApiResponse<RelationshipProgressResponse> getRelationshipProgress(
            @PathVariable Long relationshipId,
            @CurrentUserId Long userId
    ) {
        RelationshipProgressResponse response =
                relationshipService.getRelationshipProgress(
                        relationshipId,
                        userId
                );

        return ApiResponse.success(
                "관계 진행 정보를 조회하였습니다.",
                response
        );
    }

    @GetMapping("/{relationshipId}/partner-profile")
    @Operation(summary = "상대 프로필 조회", description = "관계 단계에 따라 해금된 상대 프로필 정보를 조회합니다.")
    public ApiResponse<PartnerProfileResponse> getPartnerProfile(
            @PathVariable Long relationshipId,
            @CurrentUserId Long userId
    ) {
        PartnerProfileResponse response =
                relationshipService.getPartnerProfile(
                        relationshipId,
                        userId
                );

        return ApiResponse.success(
                "상대방 프로필을 조회하였습니다.",
                response
        );
    }

    @PostMapping("/{relationshipId}/end")
    @Operation(summary = "관계 종료", description = "활성 관계를 종료 상태로 전환합니다.")
    public ApiResponse<Void> endRelationship(
            @PathVariable Long relationshipId,
            @CurrentUserId Long userId
    ) {
        relationshipService.endRelationship(relationshipId, userId);

        return ApiResponse.success("관계 종료가 완료되었습니다.", null);
    }
}
