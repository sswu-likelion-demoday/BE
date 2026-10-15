package com.sujeongring.domain.quest.controller;

import com.sujeongring.domain.quest.dto.request.AiQuizAnswerRequest;
import com.sujeongring.domain.quest.dto.response.AiQuizAnswerResponse;
import com.sujeongring.domain.quest.dto.response.AiQuizResponse;
import com.sujeongring.domain.quest.service.AiQuizAnswerService;
import com.sujeongring.domain.quest.service.AiQuizGenerationService;
import com.sujeongring.global.auth.CurrentUserId;
import com.sujeongring.global.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/relationships")
public class AiQuizController {

    private final AiQuizGenerationService aiQuizGenerationService;
    private final AiQuizAnswerService aiQuizAnswerService;

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

    @PostMapping("/{relationshipId}/quests/{relationshipQuestId}/quiz/answer")
    public ApiResponse<AiQuizAnswerResponse> submitAnswer(
            @PathVariable Long relationshipId,
            @PathVariable Long relationshipQuestId,
            @CurrentUserId Long userId,
            @Valid @RequestBody AiQuizAnswerRequest request
    ) {
        AiQuizAnswerResponse response = aiQuizAnswerService.submitAnswer(
                relationshipId,
                relationshipQuestId,
                userId,
                request.selectedOptionId()
        );

        return ApiResponse.success(
                "퀴즈 답변을 제출하였습니다.",
                response
        );
    }
}
