package com.sujeongring.domain.quest.service;

import com.openai.client.OpenAIClient;
import com.openai.models.responses.ResponseCreateParams;
import com.openai.models.responses.StructuredResponseCreateParams;
import com.sujeongring.domain.chat.entity.ChatMessage;
import com.sujeongring.domain.quest.entity.RelationshipQuest;
import com.sujeongring.domain.relationship.entity.Relationship;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiQuestEvaluator {

    private final OpenAIClient openAIClient;

    @Value("${openai.model}")
    private String model;

    public AiQuestEvaluationResult evaluate(
            RelationshipQuest relationshipQuest,
            Relationship relationship,
            List<ChatMessage> messages
    ) {
        String conversation =
                buildConversation(
                        relationship,
                        messages
                );

        String prompt =
                buildPrompt(
                        relationshipQuest,
                        conversation
                );

        StructuredResponseCreateParams<AiQuestEvaluationResult> params =
                ResponseCreateParams.builder()
                        .model(model)
                        .input(prompt)
                        .text(AiQuestEvaluationResult.class)
                        .build();

        log.info("[AI QUEST] OpenAI 요청 prompt=\n{}", prompt);

        AiQuestEvaluationResult result =
                openAIClient
                .responses()
                .create(params)
                .output()
                .stream()
                .flatMap(item -> item.message().stream())
                .flatMap(message -> message.content().stream())
                .flatMap(content -> content.outputText().stream())
                .findFirst()
                .orElseThrow(() ->
                        new IllegalStateException(
                                "AI 퀘스트 판정 결과를 받지 못했습니다."
                        )
                );

        log.info(
                "[AI QUEST] OpenAI 응답 userAParticipated={}, userBParticipated={}, reciprocalConversation={}, reason={}",
                result.userAParticipated,
                result.userBParticipated,
                result.reciprocalConversation,
                result.reason
        );

        return result;
    }

    private String buildConversation(
            Relationship relationship,
            List<ChatMessage> messages
    ) {
        StringBuilder builder =
                new StringBuilder();

        for (ChatMessage message : messages) {

            String speaker;

            if (message.getSender()
                    .getId()
                    .equals(
                            relationship
                                    .getUserA()
                                    .getId()
                    )) {
                speaker = "사용자 A";
            } else {
                speaker = "사용자 B";
            }

            builder
                    .append(speaker)
                    .append(": ")
                    .append(message.getContent())
                    .append("\n");
        }

        return builder.toString();
    }

    private String buildPrompt(
            RelationshipQuest relationshipQuest,
            String conversation
    ) {
        return """
                당신은 친구 관계 형성 서비스의 퀘스트 완료 여부를 판단하는 평가자입니다.

                두 사용자가 아래 퀘스트를 실제 대화를 통해 충분히 수행했는지 판단하세요.

                [퀘스트 제목]
                %s

                [퀘스트 설명]
                %s

                [콘텐츠 범위]
                여기서 콘텐츠는 영화, 드라마, 음악뿐 아니라
                유튜브 영상, 숏폼, 먹방, 동물 영상, 크리에이터,
                웹툰, 게임 등 사용자가 즐겨 보는 모든 미디어 콘텐츠를 포함합니다.
                반드시 특정 작품명이나 제목을 말할 필요는 없습니다.
                "먹방을 자주 본다", "동물 영상이 자주 뜬다"처럼
                선호하는 콘텐츠 유형이나 경험을 말하는 것도 유효한 참여입니다.

                [판정 기준]
                - 단순 감탄, 맞장구, 퀘스트 자체에 대한 언급만으로는 참여로 판단하지 마세요.
                - 두 사용자의 대화가 퀘스트 주제와 실질적으로 관련되어 있어야 합니다.
                - 두 사용자 모두 퀘스트 주제에 대해 최소 한 번 이상 자신의 취향, 경험, 생각을 말해야 합니다.
                - 구체적인 작품명을 말하지 않아도 콘텐츠 유형, 장르, 크리에이터, 시청 경험 등을 말하면 의미 있는 정보입니다.
                - 한 사용자가 정보를 말하고 다른 사용자가 단순히 "오", "맞아" 정도로만 반응했다면 충분하지 않습니다.
                - 두 사람이 서로 반응하며 콘텐츠 관련 대화를 한 차례 이상 이어갔다면 상호 대화가 이루어진 것으로 봅니다.
                - 대화 전체가 완벽하게 퀘스트 주제에만 집중될 필요는 없습니다.
                - 일부 잡담이 섞여 있어도 실제로 퀘스트 목적을 달성했다면 긍정적으로 판단하세요.

                [반드시 반환할 필드]
                - userAParticipated: 사용자 A가 자신의 콘텐츠 취향이나 경험을 최소 한 번 이상 제공했는지
                - userBParticipated: 사용자 B가 자신의 콘텐츠 취향이나 경험을 최소 한 번 이상 제공했는지
                - reciprocalConversation: 두 사람이 서로 반응하며 콘텐츠 관련 대화를 이어갔는지
                - reason: 판단 이유를 짧게 설명

                [대화]
                %s

                위 대화를 바탕으로 JSON 객체를 반환하세요.
                """.formatted(
                relationshipQuest
                        .getQuest()
                        .getTitle(),
                relationshipQuest
                        .getQuest()
                        .getDescription(),
                conversation
        );
    }
}
