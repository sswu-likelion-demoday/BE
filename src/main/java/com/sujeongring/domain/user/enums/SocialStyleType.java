package com.sujeongring.domain.user.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SocialStyleType {

    FIRST_APPROACH("먼저 다가가서 말을 거는 편이에요"),
    RESPOND_COMFORTABLY("먼저 다가와주면 편하게 친해져요"),
    CONVERSATION_BASED("대화를 많이 나누며 천천히 가까워져요"),
    ACTIVITY_BASED("같이 무언가를 하면서 자연스럽게 친해져요"),
    CASUAL_START("가벼운 만남부터 시작하는 게 편해요"),
    SLOW_AND_DEEP("한 번 친해지면 오래 깊게 지내는 편이에요");

    private final String label;
}