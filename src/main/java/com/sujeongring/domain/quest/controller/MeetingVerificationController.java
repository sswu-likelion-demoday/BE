package com.sujeongring.domain.quest.controller;

import com.sujeongring.domain.quest.dto.response.MeetingVerificationResponse;
import com.sujeongring.domain.quest.service.MeetingVerificationService;
import com.sujeongring.global.auth.CurrentUserId;
import com.sujeongring.global.common.ApiResponse;
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
public class MeetingVerificationController {

    private final MeetingVerificationService meetingVerificationService;

    @PostMapping(
            value = "/{relationshipId}/quests/{relationshipQuestId}/meeting-verification",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
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
