package com.sujeongring.domain.quest.service;

import com.sujeongring.domain.quest.dto.response.MeetingVerificationResponse;
import com.sujeongring.domain.quest.entity.RelationshipQuest;
import com.sujeongring.domain.quest.enums.QuestType;
import com.sujeongring.domain.quest.enums.RelationshipQuestStatus;
import com.sujeongring.domain.quest.exception.QuestErrorCode;
import com.sujeongring.domain.quest.exception.QuestException;
import com.sujeongring.domain.quest.repository.MeetingVerificationRepository;
import com.sujeongring.domain.quest.repository.RelationshipQuestRepository;
import com.sujeongring.domain.relationship.entity.Relationship;
import com.sujeongring.domain.relationship.enums.RelationshipStatus;
import com.sujeongring.domain.relationship.exception.RelationshipErrorCode;
import com.sujeongring.domain.relationship.exception.RelationshipException;
import com.sujeongring.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class MeetingVerificationService {

    private final RelationshipQuestRepository relationshipQuestRepository;
    private final MeetingVerificationRepository meetingVerificationRepository;
    private final MeetingVerificationCompletionService meetingVerificationCompletionService;
    private final MeetingVerificationAiService meetingVerificationAiVerifier;

    public MeetingVerificationResponse verify(
            Long relationshipId,
            Long relationshipQuestId,
            Long userId,
            MultipartFile image
    ) {
        if (image == null || image.isEmpty()) {
            throw new QuestException(
                    QuestErrorCode.MEETING_VERIFICATION_FAILED,
                    "인증 사진이 비어 있습니다. 다시 인증해주세요."
            );
        }

        RelationshipQuest relationshipQuest = relationshipQuestRepository
                .findDetailById(relationshipQuestId)
                .orElseThrow(() -> new QuestException(QuestErrorCode.RELATIONSHIP_QUEST_NOT_FOUND));

        Relationship relationship = relationshipQuest.getRelationship();

        if (!relationship.getId().equals(relationshipId)) {
            throw new QuestException(QuestErrorCode.RELATIONSHIP_QUEST_NOT_FOUND);
        }

        if (relationship.getUserA().getId().equals(userId) || relationship.getUserB().getId().equals(userId)) {
            // no-op: final persistence validation happens in completion service
        } else {
            throw new RelationshipException(RelationshipErrorCode.RELATIONSHIP_ACCESS_DENIED);
        }

        if (relationship.getStatus() != RelationshipStatus.ACTIVE) {
            throw new RelationshipException(
                    RelationshipErrorCode.RELATIONSHIP_ENDED
            );
        }

        if (relationshipQuest.getStatus() != RelationshipQuestStatus.ACTIVE) {
            throw new QuestException(QuestErrorCode.QUEST_NOT_ACTIVE);
        }

        if (relationshipQuest.getQuest().getQuestType() != QuestType.MEETING_VERIFICATION) {
            throw new QuestException(QuestErrorCode.QUEST_TYPE_MISMATCH);
        }

        if (meetingVerificationRepository
                .findByRelationshipQuestIdAndUserId(relationshipQuestId, userId)
                .isPresent()) {
            throw new QuestException(
                    QuestErrorCode.MEETING_VERIFICATION_ALREADY_SUBMITTED,
                    "이미 만남 인증을 완료하였습니다."
            );
        }

        boolean aiVerified = meetingVerificationAiVerifier.verify(image);
        if (!aiVerified) {
            throw new QuestException(
                    QuestErrorCode.MEETING_VERIFICATION_FAILED,
                    "인증 사진을 확인하기 어려워요. 다시 인증해주세요."
            );
        }

        MeetingVerificationCompletionResult completionResult =
                meetingVerificationCompletionService.completeVerification(
                        relationshipId,
                        relationshipQuestId,
                        userId
                );

        return new MeetingVerificationResponse(
                true,
                completionResult.partnerVerified() ? "VERIFIED" : "NOT_VERIFIED",
                completionResult.questCompleted(),
                completionResult.relationshipCompleted(),
                completionResult.relationshipCompleted()
                        ? new MeetingVerificationResponse.CompletionInfo(
                                4,
                                "외부 연락 수단"
                        )
                        : null
        );
    }
}
