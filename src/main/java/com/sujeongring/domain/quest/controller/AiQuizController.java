package com.sujeongring.domain.quest.controller;

import com.sujeongring.domain.quest.dto.response.AiQuizResponse;
import com.sujeongring.domain.quest.service.AiQuizGenerationService;
import com.sujeongring.global.auth.CurrentUserId;
import com.sujeongring.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/relationships")
public class AiQuizController {

    private final AiQuizGenerationService aiQuizGenerationService;

    @GetMapping("/{relationshipId}/quests/{relationshipQuestId}/quiz")
    public ApiResponse<AiQuizResponse> getQuiz(
            @PathVariable Long relationshipId,
            @PathVariable Long relationshipQuestId,
            @CurrentUserId Long userId
    ) {
        AiQuizResponse response =
                aiQuizGenerationService.getQuiz(
                        relationshipId,
                        relationshipQuestId,
                        userId
                );

        return ApiResponse.success(
                "상대 퀴즈를 조회하였습니다.",
                response
        );
    }
}
