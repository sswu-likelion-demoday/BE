package com.sujeongring.domain.quest.service;

import com.sujeongring.domain.chat.entity.ChatRoom;
import com.sujeongring.domain.chat.repository.ChatRoomRepository;
import com.sujeongring.domain.chat.service.ChatQuestProcessingService;
import com.sujeongring.domain.quest.dto.response.AiQuizAnswerResponse;
import com.sujeongring.domain.quest.dto.response.AiQuizResponse;
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
import com.sujeongring.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AiQuizAnswerService {

    private final RelationshipQuestRepository relationshipQuestRepository;
    private final AiQuizRepository aiQuizRepository;
    private final QuestProgressService questProgressService;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatQuestProcessingService chatQuestProcessingService;
    private final JsonMapper objectMapper;

    @Transactional
    public AiQuizAnswerResponse submitAnswer(
            Long relationshipId,
            Long relationshipQuestId,
            Long userId,
            Integer selectedOptionId
    ) {
        RelationshipQuest relationshipQuest = relationshipQuestRepository.findByIdForUpdateWithDetails(relationshipQuestId)
                .orElseThrow(() -> new QuestException(QuestErrorCode.RELATIONSHIP_QUEST_NOT_FOUND));

        Relationship relationship = relationshipQuest.getRelationship();

        if (!relationship.getId().equals(relationshipId)) {
            throw new QuestException(QuestErrorCode.RELATIONSHIP_QUEST_NOT_FOUND);
        }

        validateParticipant(relationship, userId);

        if (relationship.getStatus() == RelationshipStatus.ENDED) {
            throw new RelationshipException(RelationshipErrorCode.RELATIONSHIP_ENDED);
        }

        if (relationshipQuest.getStatus() != RelationshipQuestStatus.ACTIVE) {
            throw new QuestException(QuestErrorCode.QUEST_NOT_ACTIVE);
        }

        if (relationshipQuest.getQuest().getQuestType() != QuestType.AI_QUIZ) {
            throw new QuestException(QuestErrorCode.QUEST_TYPE_MISMATCH);
        }

        AiQuiz myQuiz = aiQuizRepository.findByRelationshipQuestIdAndAnswerUserId(
                        relationshipQuestId,
                        userId
                )
                .orElseThrow(() -> new QuestException(QuestErrorCode.AI_QUIZ_NOT_FOUND));

        if (myQuiz.isAnswered()) {
            throw new QuestException(QuestErrorCode.QUIZ_ALREADY_SUBMITTED);
        }

        List<AiQuizResponse.Option> options = parseOptions(myQuiz.getOptions());
        if (options.stream().noneMatch(option -> option.optionId() == selectedOptionId)) {
            throw new QuestException(QuestErrorCode.QUIZ_OPTION_INVALID, "유효하지 않은 퀴즈 선택지입니다.");
        }

        boolean correct = selectedOptionId == myQuiz.getCorrectOption();
        myQuiz.answer(selectedOptionId);

        boolean isUserA = relationship.getUserA().getId().equals(userId);
        User partnerUser = isUserA ? relationship.getUserB() : relationship.getUserA();

        AiQuiz partnerQuiz = aiQuizRepository.findByRelationshipQuestIdAndAnswerUserId(
                relationshipQuestId,
                partnerUser.getId()
        ).orElse(null);

        boolean partnerSubmitted = partnerQuiz != null && partnerQuiz.isAnswered();
        boolean questCompleted = false;
        QuestCompletionResult completionResult = null;

        if (partnerSubmitted) {
            completionResult = questProgressService.completeQuest(relationshipQuestId);
            questCompleted = true;
        }

        AiQuizResponse.Option correctAnswer = findCorrectAnswer(options, myQuiz.getCorrectOption());
        int reward = correct ? 5 : 0;

        AiQuizAnswerResponse.CompletionInfo completionInfo = null;
        if (questCompleted && completionResult != null) {
            final QuestCompletionResult finalizedCompletionResult = completionResult;
            final Long completionRelationshipId = relationship.getId();

            completionInfo = new AiQuizAnswerResponse.CompletionInfo(
                    finalizedCompletionResult.stageCompleted(),
                    finalizedCompletionResult.completedStage(),
                    finalizedCompletionResult.newStage(),
                    resolveUnlockLabel(finalizedCompletionResult)
            );

            if (TransactionSynchronizationManager.isSynchronizationActive()) {
                TransactionSynchronizationManager.registerSynchronization(
                        new TransactionSynchronization() {
                            @Override
                            public void afterCommit() {
                                ChatRoom chatRoom = chatRoomRepository.findByRelationshipId(completionRelationshipId)
                                        .orElse(null);

                                if (chatRoom != null) {
                                    chatQuestProcessingService.processQuestCompletionAsync(
                                            chatRoom.getId(),
                                            finalizedCompletionResult
                                    );
                                }
                            }
                        }
                );
            }
        }

        return new AiQuizAnswerResponse(
                correct,
                correctAnswer,
                reward,
                partnerSubmitted
                        ? com.sujeongring.domain.quest.enums.ParticipationStatus.SUBMITTED.name()
                        : com.sujeongring.domain.quest.enums.ParticipationStatus.NOT_SUBMITTED.name(),
                questCompleted,
                completionInfo
        );
    }

    private void validateParticipant(Relationship relationship, Long userId) {
        boolean isParticipant = relationship.getUserA().getId().equals(userId)
                || relationship.getUserB().getId().equals(userId);

        if (!isParticipant) {
            throw new RelationshipException(RelationshipErrorCode.RELATIONSHIP_ACCESS_DENIED);
        }
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

    private AiQuizResponse.Option findCorrectAnswer(
            List<AiQuizResponse.Option> options,
            int correctOptionId
    ) {
        return options.stream()
                .filter(option -> option.optionId() == correctOptionId)
                .findFirst()
                .orElseGet(() -> new AiQuizResponse.Option(correctOptionId, "정답"));
    }

    private String resolveUnlockLabel(QuestCompletionResult completionResult) {
        if (!completionResult.stageCompleted()) {
            return null;
        }

        Integer newStage = completionResult.newStage();
        if (newStage == null) {
            return null;
        }

        return switch (newStage) {
            case 2 -> "관심사";
            case 3 -> "취미 · MBTI";
            case 4 -> "이름 · 학번 · 학과 · 프로필 사진";
            default -> null;
        };
    }
}
