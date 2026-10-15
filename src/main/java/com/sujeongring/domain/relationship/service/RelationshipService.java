package com.sujeongring.domain.relationship.service;

import com.sujeongring.domain.quest.entity.Quest;
import com.sujeongring.domain.quest.entity.RelationshipQuest;
import com.sujeongring.domain.quest.enums.QuestCompletionType;
import com.sujeongring.domain.quest.enums.QuestType;
import com.sujeongring.domain.quest.enums.RelationshipQuestStatus;
import com.sujeongring.domain.quest.repository.RelationshipQuestRepository;
import com.sujeongring.domain.relationship.dto.response.PartnerProfileResponse;
import com.sujeongring.domain.relationship.dto.response.QuestResponseType;
import com.sujeongring.domain.relationship.dto.response.RelationshipProgressResponse;
import com.sujeongring.domain.relationship.entity.Relationship;
import com.sujeongring.domain.relationship.enums.RelationshipStatus;
import com.sujeongring.domain.relationship.exception.RelationshipErrorCode;
import com.sujeongring.domain.relationship.exception.RelationshipException;
import com.sujeongring.domain.relationship.policy.RelationshipUnlockPolicy;
import com.sujeongring.domain.relationship.repository.RelationshipRepository;
import com.sujeongring.domain.user.entity.User;
import com.sujeongring.domain.user.entity.UserHobby;
import com.sujeongring.domain.user.entity.UserInterest;
import com.sujeongring.domain.user.repository.onboarding.UserHobbyRepository;
import com.sujeongring.domain.user.repository.onboarding.UserInterestRepository;
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
    private final UserInterestRepository userInterestRepository;
    private final UserHobbyRepository userHobbyRepository;
    private final RelationshipUnlockPolicy relationshipUnlockPolicy;

    public RelationshipProgressResponse getRelationshipProgress(
            Long relationshipId,
            Long userId
    ) {

        Relationship relationship =
                getAccessibleRelationship(
                        relationshipId,
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
                                        ),
                                        userId
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

    public PartnerProfileResponse getPartnerProfile(
            Long relationshipId,
            Long userId
    ) {

        Relationship relationship =
                getAccessibleRelationship(
                        relationshipId,
                        userId
                );

        User partner =
                getPartner(
                        relationship,
                        userId
                );

        boolean interestsUnlocked =
                relationshipUnlockPolicy
                        .isInterestsUnlocked(relationship);

        boolean hobbiesUnlocked =
                relationshipUnlockPolicy
                        .isHobbiesUnlocked(relationship);

        boolean mbtiUnlocked =
                relationshipUnlockPolicy
                        .isMbtiUnlocked(relationship);

        boolean basicProfileUnlocked =
                relationshipUnlockPolicy
                        .isBasicProfileUnlocked(relationship);

        boolean profileImageUnlocked =
                relationshipUnlockPolicy
                        .isProfileImageUnlocked(relationship);

        boolean contactUnlocked =
                relationshipUnlockPolicy
                        .isContactUnlocked(relationship);

        List<String> interests =
                interestsUnlocked
                        ? userInterestRepository
                          .findAllByUser_IdOrderByIdAsc(partner.getId())
                          .stream()
                          .map(UserInterest::getInterest)
                          .map(interest -> interest.getLabel())
                          .toList()
                        : null;

        List<String> hobbies =
                hobbiesUnlocked
                        ? userHobbyRepository
                          .findAllByUser_IdOrderByIdAsc(partner.getId())
                          .stream()
                          .map(UserHobby::getHobby)
                          .map(hobby -> hobby.getLabel())
                          .toList()
                        : null;

        return new PartnerProfileResponse(
                partner.getId(),
                partner.getNickname(),

                relationship.getStatus(),

                new PartnerProfileResponse.CurrentStage(
                        relationship.getCurrentStage(),
                        getStageName(
                                relationship.getCurrentStage()
                        )
                ),

                new PartnerProfileResponse.ValueField<>(
                        profileImageUnlocked,
                        profileImageUnlocked
                                ? partner.getProfileImageUrl()
                                : null
                ),

                new PartnerProfileResponse.ValuesField(
                        interestsUnlocked,
                        interests
                ),

                new PartnerProfileResponse.ValuesField(
                        hobbiesUnlocked,
                        hobbies
                ),

                new PartnerProfileResponse.ValueField<>(
                        mbtiUnlocked,
                        mbtiUnlocked && partner.getMbti() != null
                                ? partner.getMbti().name()
                                : null
                ),

                new PartnerProfileResponse.ValueField<>(
                        basicProfileUnlocked,
                        basicProfileUnlocked
                                ? partner.getName()
                                : null
                ),

                new PartnerProfileResponse.ValueField<>(
                        basicProfileUnlocked,
                        basicProfileUnlocked
                                ? partner.getStudentNumber()
                                : null
                ),

                new PartnerProfileResponse.ValueField<>(
                        basicProfileUnlocked,
                        basicProfileUnlocked
                                ? partner.getDepartmentName()
                                : null
                ),

                new PartnerProfileResponse.ValueField<>(
                        contactUnlocked,
                        contactUnlocked
                                ? partner.getContact()
                                : null
                )
        );
    }

    private RelationshipProgressResponse.StageItem buildStage(
            Relationship relationship,
            int stage,
            List<RelationshipQuest> quests,
            Long currentUserId
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

        if (stageStatus
                == RelationshipProgressResponse.StageStatus.COMPLETED) {
            visibleQuests = quests;
        } else {
            visibleQuests = quests.stream()
                    .filter(quest ->
                            quest.getStatus()
                                    != RelationshipQuestStatus.LOCKED
                    )
                    .toList();
        }

        List<RelationshipProgressResponse.QuestItem> questItems =
                visibleQuests.stream()
                        .map(quest -> toQuestItem(quest, currentUserId))
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
            RelationshipQuest relationshipQuest,
            Long currentUserId
    ) {

        Quest quest =
                relationshipQuest.getQuest();

        boolean participationQuest =
                quest.getQuestType() == QuestType.AI_QUIZ
                        || quest.getQuestType()
                        == QuestType.MEETING_VERIFICATION;

        String myParticipationStatus = null;
        String partnerParticipationStatus = null;

        if (participationQuest) {
            boolean isUserA =
                    relationshipQuest.getRelationship().getUserA().getId().equals(currentUserId);

            myParticipationStatus =
                    (isUserA
                            ? relationshipQuest.isUserAParticipated()
                            : relationshipQuest.isUserBParticipated())
                            ? "SUBMITTED"
                            : "NOT_SUBMITTED";

            partnerParticipationStatus =
                    (isUserA
                            ? relationshipQuest.isUserBParticipated()
                            : relationshipQuest.isUserAParticipated())
                            ? "SUBMITTED"
                            : "NOT_SUBMITTED";
        }

        return new RelationshipProgressResponse.QuestItem(
                relationshipQuest.getId(),
                quest.getTitle(),
                getQuestResponseType(quest),
                relationshipQuest.getStatus(),
                myParticipationStatus,
                partnerParticipationStatus
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

    private Relationship getAccessibleRelationship(
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

        if (relationship.getStatus() == RelationshipStatus.ENDED) {
            throw new RelationshipException(
                    RelationshipErrorCode.RELATIONSHIP_ENDED
            );
        }

        return relationship;
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
