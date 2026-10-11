package com.sujeongring.domain.quest.service;

import com.sujeongring.domain.quest.entity.Quest;
import com.sujeongring.domain.quest.entity.RelationshipQuest;
import com.sujeongring.domain.quest.exception.QuestErrorCode;
import com.sujeongring.domain.quest.exception.QuestException;
import com.sujeongring.domain.quest.enums.QuestType;
import com.sujeongring.domain.quest.enums.RelationshipQuestStatus;
import com.sujeongring.domain.quest.repository.QuestRepository;
import com.sujeongring.domain.quest.repository.RelationshipQuestRepository;
import com.sujeongring.domain.relationship.entity.Relationship;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class QuestInitializationService {

    private static final int STAGE_1_RANDOM_COUNT = 2;
    private static final int STAGE_2_RANDOM_COUNT = 3;
    private static final int STAGE_3_RANDOM_COUNT = 3;

    private final QuestRepository questRepository;
    private final RelationshipQuestRepository relationshipQuestRepository;

    @Transactional
    public void initializeQuests(Relationship relationship) {

        if (relationshipQuestRepository.existsByRelationshipId(
                relationship.getId()
        )) {
            throw new QuestException(
                    QuestErrorCode.QUEST_ALREADY_INITIALIZED
            );
        }

        List<Quest> selectedQuests = new ArrayList<>();

        selectedQuests.addAll(selectStage1Quests());
        selectedQuests.addAll(selectStage2Quests());
        selectedQuests.addAll(selectStage3Quests());
        selectedQuests.addAll(selectStage4Quests());

        List<RelationshipQuest> relationshipQuests =
                new ArrayList<>();

        for (int i = 0; i < selectedQuests.size(); i++) {

            Quest quest = selectedQuests.get(i);

            RelationshipQuestStatus status =
                    i == 0
                            ? RelationshipQuestStatus.ACTIVE
                            : RelationshipQuestStatus.LOCKED;

            relationshipQuests.add(
                    new RelationshipQuest(
                            relationship,
                            quest,
                            status
                    )
            );
        }

        relationshipQuestRepository.saveAll(
                relationshipQuests
        );
    }

    private List<Quest> selectStage1Quests() {

        List<Quest> pool =
                questRepository
                        .findAllByStageAndActiveTrueOrderByQuestOrderAsc(1);

        Quest greetingQuest = pool.stream()
                .filter(quest -> quest.getQuestOrder() == 1)
                .findFirst()
                .orElseThrow(() ->
                        new QuestException(
                                QuestErrorCode.QUEST_FIXED_QUEST_NOT_FOUND,
                                "1단계 첫 인사 퀘스트가 존재하지 않습니다."
                        )
                );

        List<Quest> candidates = new ArrayList<>(
                pool.stream()
                        .filter(quest ->
                                !quest.getId().equals(
                                        greetingQuest.getId()
                                )
                        )
                        .toList()
        );

        validateCandidateCount(
                candidates,
                STAGE_1_RANDOM_COUNT,
                1
        );

        Collections.shuffle(candidates);

        List<Quest> selected = new ArrayList<>();

        selected.add(greetingQuest);

        selected.addAll(
                candidates.stream()
                        .limit(STAGE_1_RANDOM_COUNT)
                        .toList()
        );

        selected.sort(
                Comparator.comparingInt(
                        Quest::getQuestOrder
                )
        );

        return selected;
    }

    private List<Quest> selectStage2Quests() {
        return selectQuizStage(
                2,
                STAGE_2_RANDOM_COUNT
        );
    }

    private List<Quest> selectStage3Quests() {
        return selectQuizStage(
                3,
                STAGE_3_RANDOM_COUNT
        );
    }

    private List<Quest> selectQuizStage(
            int stage,
            int randomCount
    ) {

        List<Quest> pool =
                questRepository
                        .findAllByStageAndActiveTrueOrderByQuestOrderAsc(
                                stage
                        );

        Quest quizQuest = pool.stream()
                .filter(quest ->
                        quest.getQuestType()
                                == QuestType.AI_QUIZ
                )
                .findFirst()
                .orElseThrow(() ->
                        new QuestException(
                                QuestErrorCode.QUEST_FIXED_QUEST_NOT_FOUND,
                                stage
                                        + "단계 AI 퀴즈가 존재하지 않습니다."
                        )
                );

        List<Quest> candidates = new ArrayList<>(
                pool.stream()
                        .filter(quest ->
                                quest.getQuestType()
                                        != QuestType.AI_QUIZ
                        )
                        .toList()
        );

        validateCandidateCount(
                candidates,
                randomCount,
                stage
        );

        Collections.shuffle(candidates);

        List<Quest> selected = new ArrayList<>(
                candidates.stream()
                        .limit(randomCount)
                        .toList()
        );

        selected.add(quizQuest);

        selected.sort(
                Comparator.comparingInt(
                        Quest::getQuestOrder
                )
        );

        return selected;
    }

    private List<Quest> selectStage4Quests() {

        List<Quest> quests =
                questRepository
                        .findAllByStageAndActiveTrueOrderByQuestOrderAsc(
                                4
                        );

        if (quests.isEmpty()) {
            throw new QuestException(
                    QuestErrorCode.QUEST_POOL_NOT_FOUND,
                    "4단계 퀘스트가 존재하지 않습니다."
            );
        }

        return quests;
    }

    private void validateCandidateCount(
            List<Quest> candidates,
            int requiredCount,
            int stage
    ) {

        if (candidates.size() < requiredCount) {
            throw new QuestException(
                    QuestErrorCode.QUEST_CANDIDATE_NOT_ENOUGH,
                    stage
                            + "단계 퀘스트 후보가 부족합니다."
            );
        }
    }
}
