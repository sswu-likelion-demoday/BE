package com.sujeongring.domain.quest.service;

import com.sujeongring.domain.quest.dto.response.AiQuizResponse;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;
import com.sujeongring.domain.quest.entity.AiQuiz;
import com.sujeongring.domain.quest.entity.RelationshipQuest;
import com.sujeongring.domain.quest.enums.QuestType;
import com.sujeongring.domain.quest.enums.RelationshipQuestStatus;
import com.sujeongring.domain.quest.exception.QuestErrorCode;
import com.sujeongring.domain.quest.exception.QuestException;
import com.sujeongring.domain.quest.repository.AiQuizRepository;
import com.sujeongring.domain.quest.repository.RelationshipQuestRepository;
import com.sujeongring.domain.relationship.entity.Relationship;
import com.sujeongring.domain.relationship.enums.RelationshipStatus;
import com.sujeongring.domain.relationship.exception.RelationshipErrorCode;
import com.sujeongring.domain.relationship.exception.RelationshipException;
import com.sujeongring.domain.relationship.repository.RelationshipRepository;
import com.sujeongring.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AiQuizGenerationService {

    private final RelationshipRepository relationshipRepository;
    private final RelationshipQuestRepository relationshipQuestRepository;
    private final AiQuizRepository aiQuizRepository;
    private final JsonMapper objectMapper;

    @Transactional
    public AiQuizResponse getQuiz(
            Long relationshipId,
            Long relationshipQuestId,
            Long userId
    ) {
        Relationship relationship = relationshipRepository.findById(relationshipId)
                .orElseThrow(() -> new RelationshipException(RelationshipErrorCode.RELATIONSHIP_NOT_FOUND));

        validateParticipant(relationship, userId);

        if (relationship.getStatus() == RelationshipStatus.ENDED) {
            throw new RelationshipException(RelationshipErrorCode.RELATIONSHIP_ENDED);
        }

        RelationshipQuest relationshipQuest = relationshipQuestRepository.findById(relationshipQuestId)
                .orElseThrow(() -> new QuestException(QuestErrorCode.RELATIONSHIP_QUEST_NOT_FOUND));

        if (!relationshipQuest.getRelationship().getId().equals(relationshipId)) {
            throw new QuestException(QuestErrorCode.RELATIONSHIP_QUEST_NOT_FOUND);
        }

        if (relationshipQuest.getStatus() != RelationshipQuestStatus.ACTIVE) {
            throw new QuestException(QuestErrorCode.QUEST_NOT_ACTIVE);
        }

        if (relationshipQuest.getQuest().getQuestType() != QuestType.AI_QUIZ) {
            throw new QuestException(QuestErrorCode.QUEST_TYPE_MISMATCH);
        }

        User currentUser = getCurrentUser(relationship, userId);
        User targetUser = getTargetUser(relationship, currentUser);

        AiQuiz aiQuiz = aiQuizRepository.findByRelationshipQuestIdAndAnswerUserId(
                        relationshipQuestId,
                        userId
                )
                .orElseGet(() -> createQuiz(relationshipQuest, targetUser, currentUser));

        if (aiQuiz.isAnswered()) {
            throw new QuestException(QuestErrorCode.QUIZ_ALREADY_SUBMITTED);
        }

        return toResponse(relationshipQuest, aiQuiz);
    }

    private void validateParticipant(Relationship relationship, Long userId) {
        boolean isParticipant = relationship.getUserA().getId().equals(userId)
                || relationship.getUserB().getId().equals(userId);

        if (!isParticipant) {
            throw new RelationshipException(RelationshipErrorCode.RELATIONSHIP_ACCESS_DENIED);
        }
    }

    private User getCurrentUser(Relationship relationship, Long userId) {
        if (relationship.getUserA().getId().equals(userId)) {
            return relationship.getUserA();
        }
        return relationship.getUserB();
    }

    private User getTargetUser(Relationship relationship, User currentUser) {
        if (relationship.getUserA().getId().equals(currentUser.getId())) {
            return relationship.getUserB();
        }
        return relationship.getUserA();
    }

    private AiQuizResponse toResponse(
            RelationshipQuest relationshipQuest,
            AiQuiz aiQuiz
    ) {
        List<AiQuizResponse.Option> options = parseOptions(aiQuiz.getOptions());

        return new AiQuizResponse(
                relationshipQuest.getId(),
                new AiQuizResponse.StageInfo(
                        relationshipQuest.getQuest().getStage(),
                        switch (relationshipQuest.getQuest().getStage()) {
                            case 1 -> "첫 인사";
                            case 2 -> "알아가는 중";
                            case 3 -> "가까워지는 중";
                            case 4 -> "인연";
                            default -> "알 수 없음";
                        }
                ),
                relationshipQuest.getQuest().getTitle(),
                aiQuiz.getQuestion(),
                options
        );
    }

    private List<AiQuizResponse.Option> parseOptions(String optionsJson) {
        try {
            List<AiQuizResponse.Option> options = objectMapper.readValue(
                    optionsJson,
                    new TypeReference<>() {}
            );
            return options == null ? List.of() : options;
        } catch (JacksonException e) {
            throw new QuestException(
                    QuestErrorCode.AI_QUIZ_NOT_FOUND,
                    "퀴즈 선택지를 읽어오지 못했습니다."
            );
        }
    }

    private AiQuiz createQuiz(
            RelationshipQuest relationshipQuest,
            User targetUser,
            User answerUser
    ) {
        String question = targetUser.getNickname() + "님이 가장 좋아하는 음식은?";
        List<AiQuizResponse.Option> options = List.of(
                new AiQuizResponse.Option(1, "떡볶이"),
                new AiQuizResponse.Option(2, "파스타"),
                new AiQuizResponse.Option(3, "초밥"),
                new AiQuizResponse.Option(4, "마라탕")
        );

        String optionsJson;
        try {
            optionsJson = objectMapper.writeValueAsString(options);
        } catch (JacksonException e) {
            throw new QuestException(
                    QuestErrorCode.AI_EVALUATION_FAILED,
                    "AI 퀴즈 생성 중 오류가 발생했습니다."
            );
        }

        AiQuiz aiQuiz = new AiQuiz(
                relationshipQuest,
                targetUser,
                answerUser,
                question,
                optionsJson,
                2
        );

        return aiQuizRepository.save(aiQuiz);
    }
}
