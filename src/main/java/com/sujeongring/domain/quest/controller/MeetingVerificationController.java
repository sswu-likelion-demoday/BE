package com.sujeongring.domain.quest.controller;

import com.sujeongring.domain.quest.dto.response.MeetingVerificationResponse;
import com.sujeongring.domain.quest.service.MeetingVerificationService;
import com.sujeongring.global.auth.CurrentUserId;
import com.sujeongring.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/relationships")
@Tag(name = "만남 인증", description = "관계 퀘스트 만남 인증 API")
public class MeetingVerificationController {

    private final MeetingVerificationService meetingVerificationService;

    @PostMapping(
            value = "/{relationshipId}/quests/{relationshipQuestId}/meeting-verification",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @Operation(summary = "만남 인증 제출", description = "인증 이미지를 업로드해 만남 인증 퀘스트를 완료합니다.")
    public ApiResponse<MeetingVerificationResponse> verifyMeeting(
            @PathVariable Long relationshipId,
            @PathVariable Long relationshipQuestId,
            @CurrentUserId Long userId,
            @RequestParam("image") MultipartFile image
    ) {
        MeetingVerificationResponse response = meetingVerificationService.verify(
                relationshipId,
                relationshipQuestId,
                userId,
                image
        );

        return ApiResponse.success(
                "만남 인증을 완료하였습니다.",
                response
        );
    }
}
