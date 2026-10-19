package com.sujeongring.domain.quest.service;

import com.openai.client.OpenAIClient;
import com.openai.models.chat.completions.ChatCompletionContentPart;
import com.openai.models.chat.completions.ChatCompletionContentPartImage;
import com.openai.models.chat.completions.ChatCompletionContentPartText;
import com.openai.models.chat.completions.ChatCompletionCreateParams;
import com.openai.models.chat.completions.StructuredChatCompletionCreateParams;
import com.sujeongring.domain.quest.dto.response.AiMeetingVerificationResult;
import com.sujeongring.domain.quest.exception.QuestErrorCode;
import com.sujeongring.domain.quest.exception.QuestException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.Base64;
import java.util.List;

@Component
@RequiredArgsConstructor
public class MeetingVerificationAiService {

    private final OpenAIClient openAIClient;

    @Value("${openai.model:gpt-4o-mini}")
    private String model;

    public boolean verify(MultipartFile image) {
        try {
            String contentType = image.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                throw new QuestException(
                        QuestErrorCode.MEETING_VERIFICATION_FAILED,
                        "인증 사진을 확인하기 어려워요. 다시 인증해주세요."
                );
            }

            String imageDataUrl = buildImageDataUrl(image, contentType);
            String prompt = buildPrompt();

            ChatCompletionContentPart textPart =
                    ChatCompletionContentPart.ofText(
                            ChatCompletionContentPartText.builder()
                                    .text(prompt)
                                    .build()
                    );

            ChatCompletionContentPart imagePart =
                    ChatCompletionContentPart.ofImageUrl(
                            ChatCompletionContentPartImage.builder()
                                    .imageUrl(
                                            ChatCompletionContentPartImage.ImageUrl.builder()
                                                    .url(imageDataUrl)
                                                    .build()
                                    )
                                    .build()
                    );

            StructuredChatCompletionCreateParams<AiMeetingVerificationResult> params =
                    ChatCompletionCreateParams.builder()
                            .model(model)
                            .addUserMessageOfArrayOfContentParts(List.of(textPart, imagePart))
                            .responseFormat(AiMeetingVerificationResult.class)
                            .build();

            AiMeetingVerificationResult result = openAIClient
                    .chat()
                    .completions()
                    .create(params)
                    .choices()
                    .stream()
                    .flatMap(choice -> choice.message().content().stream())
                    .findFirst()
                    .orElseThrow(() -> new QuestException(
                            QuestErrorCode.MEETING_VERIFICATION_FAILED,
                            "인증 사진을 확인하기 어려워요. 다시 인증해주세요."
                    ));

            return result.verified();
        } catch (QuestException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new QuestException(
                    QuestErrorCode.AI_EVALUATION_FAILED,
                    "AI 판정 중 오류가 발생했습니다. 다시 시도해주세요."
            );
        }
    }

    private String buildImageDataUrl(MultipartFile image, String contentType) throws Exception {
        String base64Image = Base64.getEncoder().encodeToString(image.getBytes());
        return "data:" + contentType + ";base64," + base64Image;
    }

    private String buildPrompt() {
        return """
                당신은 오프라인 만남 인증을 위한 사진 판별 AI입니다.

                업로드된 이미지가 실제 장소에서 촬영된 사진인지 판단하세요.

                [판정 기준]
                - 실제 공간이나 장소에서 촬영된 사진으로 확인되면 verified=true로 판단하세요.
                - 실내 또는 실외 장소 모두 가능합니다.
                - 화면 캡처, 문서, 그림, 일러스트, 생성된 이미지처럼
                  실제 장소 사진으로 보기 어려운 경우 verified=false로 판단하세요.
                - 이미지가 지나치게 흐리거나 어두워 장소 사진인지 판단하기 어려운 경우
                  verified=false로 판단하세요.
                - 사진에 특정 장소의 이름이나 위치 정보가 반드시 보여야 하는 것은 아닙니다.
                - 확실하지 않은 경우 verified=false로 판단하세요.

                결과는 verified 값으로만 판단하세요.
                """;
    }
}
