package com.sujeongring.domain.user.controller;

import com.sujeongring.domain.user.dto.response.SajuElementResponse;
import com.sujeongring.domain.user.service.SajuElementService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/matching/saju")
@Tag(name = "사주", description = "사용자 사주 정보 API")
public class SajuElementController {

    private final SajuElementService sajuElementService;

    @Operation(
            summary = "부족한 기운 조회",
            description = "사용자의 오행 분포와 부족한 기운을 조회합니다."
    )
    @GetMapping("/elements")
    public ResponseEntity<Map<String, Object>> getMyElements(
            @AuthenticationPrincipal Long userId
    ) {

        SajuElementResponse response =
                sajuElementService.getMyElements(userId);

        return ResponseEntity.ok(
                Map.of(
                        "success", true,
                        "data", response
                )
        );
    }
}