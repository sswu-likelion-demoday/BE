package com.sujeongring.domain.quest.service;

import com.sujeongring.domain.chat.entity.ChatMessage;
import com.sujeongring.domain.chat.enums.MessageType;
import com.sujeongring.domain.chat.repository.ChatMessageRepository;
import com.sujeongring.domain.chat.repository.ChatRoomRepository;
import com.sujeongring.domain.quest.dto.response.AiQuizGenerationResult;
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
import com.sujeongring.domain.user.entity.UserHobby;
import com.sujeongring.domain.user.entity.UserInterest;
import com.sujeongring.domain.user.enums.HobbyType;
import com.sujeongring.domain.user.enums.InterestType;
import com.sujeongring.domain.user.enums.MbtiType;
import com.sujeongring.domain.user.repository.onboarding.UserHobbyRepository;
import com.sujeongring.domain.user.repository.onboarding.UserInterestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AiQuizGenerationService {

    private final RelationshipRepository relationshipRepository;
    private final RelationshipQuestRepository relationshipQuestRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final AiQuizRepository aiQuizRepository;
    private final AiQuizGenerator aiQuizGenerator;
    private final AiQuizPersistenceService aiQuizPersistenceService;
    private final UserInterestRepository userInterestRepository;
    private final UserHobbyRepository userHobbyRepository;
    private final JsonMapper objectMapper;

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

    protected AiQuiz createQuiz(
            RelationshipQuest relationshipQuest,
            User targetUser,
            User answerUser
    ) {
        List<ChatMessage> messages = loadStageMessages(relationshipQuest);

        AiQuizGenerationResult generationResult = generateWithFallback(
                targetUser,
                messages,
                relationshipQuest
        );

        List<AiQuizResponse.Option> options = generationResult.options().stream()
                .map(option -> new AiQuizResponse.Option(option.optionId(), option.content()))
                .toList();

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
                generationResult.question(),
                optionsJson,
                generationResult.correctOption()
        );

        return aiQuizPersistenceService.saveGeneratedQuiz(aiQuiz);
    }

    private AiQuizGenerationResult generateWithFallback(
            User targetUser,
            List<ChatMessage> messages,
            RelationshipQuest relationshipQuest
    ) {
        try {
            return aiQuizGenerator.generate(targetUser, messages);
        } catch (QuestException exception) {
            return buildFallbackQuiz(targetUser, relationshipQuest.getQuest().getStage());
        }
    }

    private List<ChatMessage> loadStageMessages(
            RelationshipQuest relationshipQuest
    ) {
        Relationship relationship = relationshipQuest.getRelationship();

        Long chatRoomId = chatRoomRepository.findByRelationshipId(relationship.getId())
                .orElseThrow(() -> new QuestException(
                        QuestErrorCode.AI_QUIZ_NOT_FOUND,
                        "채팅방을 찾을 수 없어 AI 퀴즈를 만들 수 없습니다."
                ))
                .getId();

        List<RelationshipQuest> stageQuests = relationshipQuestRepository.findByRelationshipIdAndStage(
                relationship.getId(),
                relationshipQuest.getQuest().getStage()
        );

        LocalDateTime stageOpenedAt = stageQuests.stream()
                .filter(quest -> quest.getOpenedAt() != null)
                .min(java.util.Comparator.comparing(RelationshipQuest::getOpenedAt))
                .map(RelationshipQuest::getOpenedAt)
                .orElse(relationshipQuest.getOpenedAt());

        LocalDateTime quizOpenedAt = relationshipQuest.getOpenedAt();
        if (quizOpenedAt == null) {
            quizOpenedAt = LocalDateTime.now();
        }

        return chatMessageRepository.findStageMessages(
                chatRoomId,
                MessageType.TEXT,
                stageOpenedAt,
                quizOpenedAt
        );
    }

    private AiQuizGenerationResult buildFallbackQuiz(User targetUser, int stage) {
        if (stage == 2) {
            return buildInterestFallback(targetUser);
        }

        if (stage == 3) {
            return buildHobbyOrMbtiFallback(targetUser);
        }

        return buildInterestFallback(targetUser);
    }

    private AiQuizGenerationResult buildInterestFallback(User targetUser) {
        List<String> interests = userInterestRepository.findAllByUser_IdOrderByIdAsc(targetUser.getId())
                .stream()
                .map(UserInterest::getInterest)
                .map(Enum::name)
                .distinct()
                .toList();

        if (interests.isEmpty()) {
            throw new QuestException(
                    QuestErrorCode.AI_QUIZ_NOT_FOUND,
                    "퀴즈를 생성할 수 있는 관심사 정보가 부족합니다."
            );
        }

        Set<String> interestSet = Set.copyOf(interests);
        String correct = interests.getFirst();
        List<String> distractors = new ArrayList<>(Arrays.asList(
                InterestType.IT_TECH.name(),
                InterestType.FINANCE.name(),
                InterestType.SPORTS.name(),
                InterestType.TRAVEL.name(),
                InterestType.GAME.name(),
                InterestType.BOOK_WRITING.name()
        ));
        distractors.removeAll(interestSet);
        if (distractors.size() < 3) {
            for (InterestType type : InterestType.values()) {
                String value = type.name();
                if (!interestSet.contains(value) && !distractors.contains(value)) {
                    distractors.add(value);
                    if (distractors.size() >= 3) {
                        break;
                    }
                }
            }
        }

        if (distractors.size() < 3) {
            throw new QuestException(
                    QuestErrorCode.AI_QUIZ_NOT_FOUND,
                    "퀴즈를 생성할 수 있는 관심사 정보가 부족합니다."
            );
        }

        List<String> options = new ArrayList<>(List.of(correct));
        options.addAll(distractors.subList(0, 3));
        java.util.Collections.shuffle(options);
        int correctIndex = options.indexOf(correct) + 1;

        return new AiQuizGenerationResult(
                targetUser.getNickname() + "님의 관심사에 해당하는 항목은 무엇인가요?",
                toDisplayOptionList(options),
                correctIndex
        );
    }

    private AiQuizGenerationResult buildHobbyOrMbtiFallback(User targetUser) {
        List<String> hobbies = userHobbyRepository.findAllByUser_IdOrderByIdAsc(targetUser.getId())
                .stream()
                .map(UserHobby::getHobby)
                .map(Enum::name)
                .distinct()
                .toList();

        if (!hobbies.isEmpty()) {
            Set<String> hobbySet = Set.copyOf(hobbies);
            String correct = hobbies.getFirst();
            List<String> distractors = new ArrayList<>(Arrays.asList(
                    HobbyType.READING.name(),
                    HobbyType.TRAVEL.name(),
                    HobbyType.GAME.name(),
                    HobbyType.RUNNING.name(),
                    HobbyType.CAFE.name()
            ));
            distractors.removeAll(hobbySet);
            if (distractors.size() < 3) {
                for (HobbyType type : HobbyType.values()) {
                    String value = type.name();
                    if (!hobbySet.contains(value) && !distractors.contains(value)) {
                        distractors.add(value);
                        if (distractors.size() >= 3) {
                            break;
                        }
                    }
                }
            }

            if (distractors.size() < 3) {
                throw new QuestException(
                        QuestErrorCode.AI_QUIZ_NOT_FOUND,
                        "퀴즈를 생성할 수 있는 취미 정보가 부족합니다."
                );
            }

            List<String> options = new ArrayList<>(List.of(correct));
            options.addAll(distractors.subList(0, 3));
            java.util.Collections.shuffle(options);
            int correctIndex = options.indexOf(correct) + 1;

            return new AiQuizGenerationResult(
                    targetUser.getNickname() + "님의 취미에 해당하는 것은 무엇일까요?",
                    toDisplayOptionList(options),
                    correctIndex
            );
        }

        MbtiType mbti = targetUser.getMbti();
        if (mbti != null) {
            List<String> mbtiOptions = new ArrayList<>(Arrays.asList(
                    MbtiType.INTJ.name(),
                    MbtiType.ENFP.name(),
                    MbtiType.ISFJ.name(),
                    MbtiType.ESTP.name(),
                    MbtiType.INFJ.name(),
                    MbtiType.ENTP.name()
            ));
            String correct = mbti.name();
            mbtiOptions.remove(correct);
            List<String> options = new ArrayList<>();
            options.add(correct);
            options.addAll(mbtiOptions.subList(0, 3));
            java.util.Collections.shuffle(options);

            return new AiQuizGenerationResult(
                    targetUser.getNickname() + "님의 MBTI는 무엇일까요?",
                    toDisplayOptionList(options),
                    options.indexOf(correct) + 1
            );
        }

        throw new QuestException(
                QuestErrorCode.AI_QUIZ_NOT_FOUND,
                "퀴즈를 생성할 수 있는 정보가 부족합니다."
        );
    }

    private List<AiQuizGenerationResult.Option> toDisplayOptionList(List<String> values) {
        List<AiQuizGenerationResult.Option> options = new ArrayList<>();
        for (int i = 0; i < values.size(); i++) {
            options.add(new AiQuizGenerationResult.Option(i + 1, toDisplayText(values.get(i))));
        }
        return options;
    }

    private String toDisplayText(String value) {
        if (value == null || value.isBlank()) {
            return value;
        }

        try {
            return InterestType.valueOf(value).getLabel();
        } catch (IllegalArgumentException ignored) {
            try {
                return HobbyType.valueOf(value).getLabel();
            } catch (IllegalArgumentException ignoredAgain) {
                try {
                    return MbtiType.valueOf(value).name();
                } catch (IllegalArgumentException ignoredFinal) {
                    return value;
                }
            }
        }
    }
}
