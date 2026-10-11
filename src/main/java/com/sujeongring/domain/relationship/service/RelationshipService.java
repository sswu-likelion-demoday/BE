package com.sujeongring.domain.relationship.service;

import com.sujeongring.domain.quest.entity.Quest;
import com.sujeongring.domain.quest.entity.RelationshipQuest;
import com.sujeongring.domain.quest.enums.QuestCompletionType;
import com.sujeongring.domain.quest.enums.QuestType;
import com.sujeongring.domain.quest.enums.RelationshipQuestStatus;
import com.sujeongring.domain.quest.repository.RelationshipQuestRepository;
import com.sujeongring.domain.relationship.dto.response.QuestResponseType;
import com.sujeongring.domain.relationship.dto.response.RelationshipProgressResponse;
import com.sujeongring.domain.relationship.entity.Relationship;
import com.sujeongring.domain.relationship.enums.RelationshipStatus;
import com.sujeongring.domain.relationship.exception.RelationshipErrorCode;
import com.sujeongring.domain.relationship.exception.RelationshipException;
import com.sujeongring.domain.relationship.policy.RelationshipUnlockPolicy;
import com.sujeongring.domain.relationship.repository.RelationshipRepository;
import com.sujeongring.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RelationshipService {

    private static final int TOTAL_STAGES = 4;

    private final RelationshipRepository relationshipRepository;
    private final RelationshipQuestRepository relationshipQuestRepository;
    private final RelationshipUnlockPolicy relationshipUnlockPolicy;

    public RelationshipProgressResponse getRelationshipProgress(
            Long relationshipId,
            Long userId
    ) {

        Relationship relationship =
                relationshipRepository.findById(relationshipId)
                        .orElseThrow(() ->
                                new RelationshipException(
                                        RelationshipErrorCode.RELATIONSHIP_NOT_FOUND
                                )
                        );

        validateParticipant(
                relationship,
                userId
        );

        User partner = getPartner(
                relationship,
                userId
        );

        boolean profileImageUnlocked =
                relationshipUnlockPolicy
                        .isProfileImageUnlocked(relationship);

        List<RelationshipQuest> relationshipQuests =
                relationshipQuestRepository
                        .findAllByRelationshipIdWithQuest(
                                relationshipId
                        );

        Map<Integer, List<RelationshipQuest>> questsByStage =
                relationshipQuests.stream()
                        .collect(
                                Collectors.groupingBy(
                                        rq -> rq.getQuest().getStage()
                                )
                        );

        List<RelationshipProgressResponse.StageItem> stages =
                java.util.stream.IntStream
                        .rangeClosed(1, TOTAL_STAGES)
                        .mapToObj(stage ->
                                buildStage(
                                        relationship,
                                        stage,
                                        questsByStage.getOrDefault(
                                                stage,
                                                List.of()
                                        )
                                )
                        )
                        .toList();

        return new RelationshipProgressResponse(

                relationship.getId(),
                relationship.getStatus(),

                new RelationshipProgressResponse.Partner(
                        partner.getId(),
                        partner.getNickname(),
                        profileImageUnlocked
                                ? partner.getProfileImageUrl()
                                : null,
                        profileImageUnlocked
                ),

                new RelationshipProgressResponse.CurrentStage(
                        relationship.getCurrentStage(),
                        getStageName(
                                relationship.getCurrentStage()
                        )
                ),

                TOTAL_STAGES,

                stages
        );
    }

    private RelationshipProgressResponse.StageItem buildStage(
            Relationship relationship,
            int stage,
            List<RelationshipQuest> quests
    ) {

        RelationshipProgressResponse.StageStatus stageStatus =
                getStageStatus(
                        relationship,
                        stage
                );

        RelationshipProgressResponse.UnlockStatus unlockStatus =
                stageStatus
                        == RelationshipProgressResponse.StageStatus.COMPLETED
                        ? RelationshipProgressResponse.UnlockStatus.UNLOCKED
                        : RelationshipProgressResponse.UnlockStatus.LOCKED;

        // 아직 도달하지 않은 단계 - 퀘스트 상세 공개 X, 진행도도 공개 X, 단계명 + 해금 정보만 표시
        if (stageStatus
                == RelationshipProgressResponse.StageStatus.LOCKED) {

            return new RelationshipProgressResponse.StageItem(
                    stage,
                    getStageName(stage),
                    stageStatus,
                    null,
                    List.of(),
                    new RelationshipProgressResponse.Unlock(
                            unlockStatus,
                            getUnlockLabel(stage)
                    )
            );
        }

        int completedCount = (int) quests.stream()
                .filter(quest ->
                        quest.getStatus()
                                == RelationshipQuestStatus.COMPLETED
                )
                .count();

        List<RelationshipQuest> visibleQuests;

        // 완료 단계 -> 해당 단계의 퀘스트 표시
        if (stageStatus
                == RelationshipProgressResponse.StageStatus.COMPLETED) {

            visibleQuests = quests;
        }

        // 현재 진행 단계 -> completed + 현재 active까지 퀘스트 표시, 앞으로 남은 locked 퀘스트는 아직 비공개
        else {

            visibleQuests = quests.stream()
                    .filter(quest ->
                            quest.getStatus()
                                    != RelationshipQuestStatus.LOCKED
                    )
                    .toList();
        }

        List<RelationshipProgressResponse.QuestItem> questItems =
                visibleQuests.stream()
                        .map(this::toQuestItem)
                        .toList();

        return new RelationshipProgressResponse.StageItem(
                stage,
                getStageName(stage),
                stageStatus,

                new RelationshipProgressResponse.QuestProgress(
                        completedCount,
                        quests.size()
                ),

                questItems,

                new RelationshipProgressResponse.Unlock(
                        unlockStatus,
                        getUnlockLabel(stage)
                )
        );
    }

    private RelationshipProgressResponse.QuestItem toQuestItem(
            RelationshipQuest relationshipQuest
    ) {

        Quest quest =
                relationshipQuest.getQuest();

        boolean participationQuest =
                quest.getQuestType() == QuestType.AI_QUIZ
                        || quest.getQuestType()
                        == QuestType.MEETING_VERIFICATION;

        // AI 퀴즈 / 만남 인증 참여 상태는 추후 구현 후 실제 값으로 교체
        String myParticipationStatus =
                participationQuest
                        ? null
                        : null;

        String partnerParticipationStatus =
                participationQuest
                        ? null
                        : null;

        return new RelationshipProgressResponse.QuestItem(
                relationshipQuest.getId(),
                quest.getTitle(),
                getQuestResponseType(quest),
                relationshipQuest.getStatus(),
                null,
                null
        );
    }

    private QuestResponseType getQuestResponseType(
            Quest quest
    ) {

        if (quest.getQuestType() == QuestType.AI_QUIZ) {
            return QuestResponseType.AI_QUIZ;
        }

        if (quest.getQuestType()
                == QuestType.MEETING_VERIFICATION) {
            return QuestResponseType.MEETING_VERIFICATION;
        }

        // CHAT_ANALYSIS는 실제 완료 방식에 따라 구분해서 넘겨줌
        if (quest.getCompletionType()
                == QuestCompletionType.MESSAGE_COUNT) {
            return QuestResponseType.MESSAGE_COUNT;
        }

        return QuestResponseType.AI_ANALYSIS;
    }

    private RelationshipProgressResponse.StageStatus getStageStatus(
            Relationship relationship,
            int stage
    ) {

        if (relationship.getStatus()
                == RelationshipStatus.COMPLETED) {

            return RelationshipProgressResponse.StageStatus.COMPLETED;
        }

        int currentStage =
                relationship.getCurrentStage();

        if (stage < currentStage) {
            return RelationshipProgressResponse.StageStatus.COMPLETED;
        }

        if (stage == currentStage) {
            return RelationshipProgressResponse.StageStatus.ACTIVE;
        }

        return RelationshipProgressResponse.StageStatus.LOCKED;
    }

    private void validateParticipant(
            Relationship relationship,
            Long userId
    ) {

        boolean participant =
                relationship.getUserA().getId().equals(userId)
                        || relationship.getUserB()
                        .getId()
                        .equals(userId);

        if (!participant) {
            throw new RelationshipException(
                    RelationshipErrorCode.RELATIONSHIP_ACCESS_DENIED
            );
        }
    }

    private User getPartner(
            Relationship relationship,
            Long userId
    ) {

        if (relationship.getUserA()
                .getId()
                .equals(userId)) {

            return relationship.getUserB();
        }

        return relationship.getUserA();
    }

    private String getStageName(
            int stage
    ) {

        return switch (stage) {
            case 1 -> "첫 인사";
            case 2 -> "알아가는 중";
            case 3 -> "가까워지는 중";
            case 4 -> "인연";
            default -> throw new IllegalArgumentException(
                    "지원하지 않는 관계 단계입니다: " + stage
            );
        };
    }

    private String getUnlockLabel(
            int stage
    ) {

        return switch (stage) {
            case 1 -> "관심사";
            case 2 -> "취미 · MBTI";
            case 3 -> "이름 · 학번 · 학과 · 프로필 사진";
            case 4 -> "외부 연락 수단";
            default -> throw new IllegalArgumentException(
                    "지원하지 않는 관계 단계입니다: " + stage
            );
        };
    }
}
