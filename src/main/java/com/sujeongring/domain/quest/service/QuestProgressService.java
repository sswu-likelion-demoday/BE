// 퀘스트 완료 후 채팅 후처리 공통 로직
package com.sujeongring.domain.quest.service;

import com.sujeongring.domain.quest.entity.RelationshipQuest;
import com.sujeongring.domain.quest.enums.RelationshipQuestStatus;
import com.sujeongring.domain.quest.exception.QuestErrorCode;
import com.sujeongring.domain.quest.exception.QuestException;
import com.sujeongring.domain.quest.repository.RelationshipQuestRepository;
import com.sujeongring.domain.relationship.entity.Relationship;
import com.sujeongring.domain.relationship.enums.RelationshipStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class QuestProgressService {

    private final RelationshipQuestRepository relationshipQuestRepository;

    @Transactional
    public void updateQuestProgress(
            Long relationshipQuestId,
            int progress
    ) {
        RelationshipQuest relationshipQuest =
                relationshipQuestRepository
                        .findByIdForUpdateWithDetails(
                                relationshipQuestId
                        )
                        .orElseThrow(() ->
                                new QuestException(
                                        QuestErrorCode.QUEST_NOT_FOUND
                                )
                        );

        relationshipQuest.updateProgress(progress);
    }

    @Transactional
    public AiQuestEvaluationState updateAiEvaluation(
            Long relationshipQuestId,
            boolean userAParticipated,
            boolean userBParticipated,
            boolean reciprocalConversation
    ) {
        RelationshipQuest relationshipQuest =
                relationshipQuestRepository
                        .findByIdForUpdateWithDetails(
                                relationshipQuestId
                        )
                        .orElseThrow(() ->
                                new QuestException(
                                        QuestErrorCode.QUEST_NOT_FOUND
                                )
                        );

        relationshipQuest.updateAiEvaluation(
                userAParticipated,
                userBParticipated,
                reciprocalConversation
        );

        return new AiQuestEvaluationState(
                relationshipQuest.isUserAParticipated(),
                relationshipQuest.isUserBParticipated(),
                relationshipQuest.isReciprocalConversation()
        );
    }

    @Transactional
    public QuestCompletionResult completeQuest(
            Long relationshipQuestId
    ) {

        RelationshipQuest relationshipQuest =
                relationshipQuestRepository
                        .findByIdForUpdateWithDetails(
                                relationshipQuestId
                        )
                        .orElseThrow(() ->
                                new QuestException(
                                        QuestErrorCode.QUEST_NOT_FOUND
                                )
                        );

        Relationship relationship =
                relationshipQuest.getRelationship();

        validateQuestCompletion(
                relationship,
                relationshipQuest
        );

        int completedStage =
                relationshipQuest.getQuest().getStage();

        // 현재 퀘스트 완료
        relationshipQuest.complete();

        List<RelationshipQuest> stageQuests =
                relationshipQuestRepository
                        .findByRelationshipIdAndStage(
                                relationship.getId(),
                                completedStage
                        );

        // 같은 단계의 다음 LOCKED 퀘스트 찾기
        RelationshipQuest nextQuest =
                stageQuests.stream()
                        .filter(quest ->
                                quest.getStatus()
                                        == RelationshipQuestStatus.LOCKED
                        )
                        .findFirst()
                        .orElse(null);

        // 같은 단계에 다음 퀘스트가 남아 있음
        if (nextQuest != null) {

            nextQuest.activate();

            return QuestCompletionResult.nextQuestOpened(
                    relationship.getId(),
                    completedStage,
                    relationshipQuest.getId(),
                    nextQuest.getId()
            );
        }

        // 현재 단계의 퀘스트를 전부 완료함
        if (completedStage == 4) {

            relationship.complete();

            return QuestCompletionResult.relationshipCompleted(
                    relationship.getId(),
                    completedStage,
                    relationshipQuest.getId()
            );
        }

        // 다음 관계 단계로 이동
        relationship.advanceStage();

        int newStage = relationship.getCurrentStage();

        List<RelationshipQuest> nextStageQuests =
                relationshipQuestRepository
                        .findByRelationshipIdAndStage(
                                relationship.getId(),
                                newStage
                        );

        RelationshipQuest firstQuest =
                nextStageQuests.stream()
                        .findFirst()
                        .orElseThrow(() ->
                                new QuestException(
                                        QuestErrorCode.QUEST_POOL_NOT_FOUND
                                )
                        );

        firstQuest.activate();

        return QuestCompletionResult.stageCompleted(
                relationship.getId(),
                completedStage,
                newStage,
                relationshipQuest.getId(),
                firstQuest.getId()
        );
    }

    private void validateQuestCompletion(
            Relationship relationship,
            RelationshipQuest relationshipQuest
    ) {

        if (relationship.getStatus()
                != RelationshipStatus.ACTIVE) {

            throw new QuestException(
                    QuestErrorCode.QUEST_NOT_ACTIVE
            );
        }

        if (relationshipQuest.getStatus()
                != RelationshipQuestStatus.ACTIVE) {

            throw new QuestException(
                    QuestErrorCode.QUEST_NOT_ACTIVE
            );
        }

        if (relationshipQuest.getQuest().getStage()
                != relationship.getCurrentStage()) {

            throw new QuestException(
                    QuestErrorCode.QUEST_STAGE_MISMATCH
            );
        }
    }
}
