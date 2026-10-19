package com.sujeongring.domain.quest.service;

import com.sujeongring.domain.chat.entity.ChatRoom;
import com.sujeongring.domain.chat.repository.ChatRoomRepository;
import com.sujeongring.domain.chat.service.ChatQuestProcessingService;
import com.sujeongring.domain.quest.entity.MeetingVerification;
import com.sujeongring.domain.quest.entity.RelationshipQuest;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
@RequiredArgsConstructor
public class MeetingVerificationCompletionService {

    private final RelationshipQuestRepository relationshipQuestRepository;
    private final MeetingVerificationRepository meetingVerificationRepository;
    private final QuestProgressService questProgressService;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatQuestProcessingService chatQuestProcessingService;

    @Transactional
    public MeetingVerificationCompletionResult completeVerification(
            Long relationshipId,
            Long relationshipQuestId,
            Long userId
    ) {
        RelationshipQuest lockedQuest = relationshipQuestRepository
                .findByIdForUpdateWithDetails(relationshipQuestId)
                .orElseThrow(() -> new QuestException(QuestErrorCode.RELATIONSHIP_QUEST_NOT_FOUND));

        Relationship relationship = lockedQuest.getRelationship();
        if (!relationship.getId().equals(relationshipId)) {
            throw new QuestException(QuestErrorCode.RELATIONSHIP_QUEST_NOT_FOUND);
        }

        validateParticipant(relationship, userId);

        if (relationship.getStatus() != RelationshipStatus.ACTIVE) {
            throw new RelationshipException(RelationshipErrorCode.RELATIONSHIP_ENDED);
        }

        if (lockedQuest.getStatus() != RelationshipQuestStatus.ACTIVE) {
            throw new QuestException(QuestErrorCode.QUEST_NOT_ACTIVE);
        }

        User currentUser = getCurrentUser(relationship, userId);
        if (meetingVerificationRepository
                .findByRelationshipQuestIdAndUserId(lockedQuest.getId(), currentUser.getId())
                .isPresent()) {
            throw new QuestException(
                    QuestErrorCode.MEETING_VERIFICATION_ALREADY_SUBMITTED,
                    "이미 만남 인증을 완료하였습니다."
            );
        }

        meetingVerificationRepository.save(
                new MeetingVerification(lockedQuest, currentUser)
        );

        User partnerUser = getPartnerUser(relationship, currentUser);
        boolean partnerVerified = meetingVerificationRepository
                .findByRelationshipQuestIdAndUserId(lockedQuest.getId(), partnerUser.getId())
                .isPresent();

        if (!partnerVerified) {
            return new MeetingVerificationCompletionResult(false, false, false);
        }

        QuestCompletionResult completionResult = questProgressService.completeQuest(lockedQuest.getId());

        Long chatRoomId = chatRoomRepository.findByRelationshipId(relationship.getId())
                .map(ChatRoom::getId)
                .orElse(null);

        if (chatRoomId != null && TransactionSynchronizationManager.isSynchronizationActive()) {
            QuestCompletionResult capturedResult = completionResult;
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    chatQuestProcessingService.processQuestCompletionAsync(
                            chatRoomId,
                            capturedResult
                    );
                }
            });
        }

        return new MeetingVerificationCompletionResult(
                true,
                true,
                completionResult.relationshipCompleted()
        );
    }

    private void validateParticipant(Relationship relationship, Long userId) {
        boolean participant = relationship.getUserA().getId().equals(userId)
                || relationship.getUserB().getId().equals(userId);

        if (!participant) {
            throw new RelationshipException(RelationshipErrorCode.RELATIONSHIP_ACCESS_DENIED);
        }
    }

    private User getCurrentUser(Relationship relationship, Long userId) {
        if (relationship.getUserA().getId().equals(userId)) {
            return relationship.getUserA();
        }
        return relationship.getUserB();
    }

    private User getPartnerUser(Relationship relationship, User currentUser) {
        if (relationship.getUserA().getId().equals(currentUser.getId())) {
            return relationship.getUserB();
        }
        return relationship.getUserA();
    }
}
