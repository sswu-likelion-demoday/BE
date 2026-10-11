package com.sujeongring.domain.relationship.controller;

import com.sujeongring.domain.relationship.dto.response.RelationshipProgressResponse;
import com.sujeongring.domain.relationship.service.RelationshipService;
import com.sujeongring.global.auth.CurrentUserId;
import com.sujeongring.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/relationships")
public class RelationshipController {

    private final RelationshipService relationshipService;

    @GetMapping("/{relationshipId}")
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
}
