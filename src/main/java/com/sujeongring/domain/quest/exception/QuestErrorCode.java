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
            HttpStatus.INTERNAL_SERVER_ERROR,
            "QUEST_POOL_NOT_FOUND",
            "퀘스트 풀을 찾을 수 없습니다."
    ),

    QUEST_CANDIDATE_NOT_ENOUGH(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "QUEST_CANDIDATE_NOT_ENOUGH",
            "퀘스트 후보가 부족합니다."
    ),

    QUEST_FIXED_QUEST_NOT_FOUND(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "QUEST_FIXED_QUEST_NOT_FOUND",
            "필수 고정 퀘스트를 찾을 수 없습니다."
    ),

    QUEST_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "QUEST_NOT_FOUND",
            "퀘스트를 찾을 수 없습니다."
    ),

    QUEST_NOT_ACTIVE(
            HttpStatus.CONFLICT,
            "QUEST_NOT_ACTIVE",
            "현재 진행 중인 퀘스트가 아닙니다."
    ),

    QUEST_STAGE_MISMATCH(
            HttpStatus.CONFLICT,
            "QUEST_STAGE_MISMATCH",
            "현재 관계 단계의 퀘스트가 아닙니다."
    ),

    AI_EVALUATION_FAILED(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "AI_EVALUATION_FAILED",
            "AI 퀘스트 판정을 수행하지 못했습니다."
    );

    private final HttpStatus status;
    private final String code;
    private final String message;
}