package com.sujeongring.domain.relationship.controller;

import com.sujeongring.domain.relationship.dto.response.RelationshipMapResponse;
import com.sujeongring.domain.relationship.service.RelationshipMapService;
import com.sujeongring.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequiredArgsConstructor
@RequestMapping("/relationship-map")
@Tag(name = "수정원 조회", description = "수정원 조회 API")
public class RelationshipMapController {

    private final RelationshipMapService relationshipMapService;

    @GetMapping
    public ApiResponse<RelationshipMapResponse> getRelationshipMap(
            Principal principal
    ) {

        Long userId =
                Long.valueOf(principal.getName());

        return ApiResponse.success(
                "수정맵을 조회하였습니다.",
                relationshipMapService.getRelationshipMap(userId)
        );
    }
}
