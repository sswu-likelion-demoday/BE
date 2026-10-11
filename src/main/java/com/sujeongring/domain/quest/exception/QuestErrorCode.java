package com.sujeongring.domain.quest.exception;

import com.sujeongring.global.error.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum QuestErrorCode implements ErrorCode {

    QUEST_ALREADY_INITIALIZED(
            HttpStatus.CONFLICT,
            "QUEST_ALREADY_INITIALIZED",
            "이미 퀘스트가 초기화된 관계입니다."
    ),

    QUEST_POOL_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "QUEST_POOL_NOT_FOUND",
            "퀘스트 풀을 찾을 수 없습니다."
    ),

    QUEST_CANDIDATE_NOT_ENOUGH(
            HttpStatus.BAD_REQUEST,
            "QUEST_CANDIDATE_NOT_ENOUGH",
            "퀘스트 후보가 부족합니다."
    ),

    QUEST_FIXED_QUEST_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "QUEST_FIXED_QUEST_NOT_FOUND",
            "필수 고정 퀘스트를 찾을 수 없습니다."
    ),

    QUEST_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "QUEST_NOT_FOUND",
            "퀘스트를 찾을 수 없습니다."
    );

    private final HttpStatus status;
    private final String code;
    private final String message;
}
