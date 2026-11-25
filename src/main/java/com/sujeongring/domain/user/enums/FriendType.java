package com.sujeongring.domain.user.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum FriendType {

    EASY_TO_TALK("편하게 대화할 수 있는 친구"),
    FUN_TO_BE_WITH("같이 있으면 재미있는 친구"),
    EMPATHETIC("서로 공감하고 배려하는 친구"),
    MOTIVATING("서로에게 좋은 자극이 되는 친구"),
    SHARED_INTEREST("취미와 관심사를 함께 즐길 수 있는 친구"),
    NEW_PERSPECTIVE("새로운 생각과 이야기를 나눌 수 있는 친구"),
    LONG_TERM("오래 편하게 지낼 수 있는 친구");

    private final String label;
}