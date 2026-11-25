package com.sujeongring.domain.quest.controller;

import com.sujeongring.domain.quest.dto.request.AiQuizAnswerRequest;
import com.sujeongring.domain.quest.dto.response.AiQuizAnswerResponse;
import com.sujeongring.domain.quest.dto.response.AiQuizResponse;
import com.sujeongring.domain.quest.service.AiQuizAnswerService;
import com.sujeongring.domain.quest.service.AiQuizGenerationService;
import com.sujeongring.global.auth.CurrentUserId;
import com.sujeongring.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/relationships")
@Tag(name = "AI 퀴즈", description = "관계 퀘스트 AI 퀴즈 조회/답변 제출 API")
public class AiQuizController {

    private final AiQuizGenerationService aiQuizGenerationService;
    private final AiQuizAnswerService aiQuizAnswerService;

    @GetMapping("/{relationshipId}/quests/{relationshipQuestId}/quiz")
    @Operation(summary = "AI 퀴즈 조회", description = "현재 활성 AI 퀴즈를 조회하거나 없으면 생성합니다.")
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
    @Operation(summary = "AI 퀴즈 답변 제출", description = "선택한 답변을 제출하고 정답/보상을 반환합니다.")
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
