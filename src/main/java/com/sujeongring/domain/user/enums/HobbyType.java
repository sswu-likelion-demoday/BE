package com.sujeongring.domain.user.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum HobbyType {

    CAFE("카페탐방"),
    RESTAURANT("맛집탐방"),
    RUNNING("러닝"),
    READING("독서"),
    TRAVEL("여행"),
    GAME("게임"),
    COOKING_BAKING("요리·베이킹"),
    DRAWING_CRAFT("그림·공예"),
    EXHIBITION("전시·미술관 방문"),
    MUSIC("음악"),
    WALKING("산책"),
    FITNESS("운동·헬스"),
    BOARD_GAME("보드게임"),
    PHOTOGRAPHY("사진촬영"),
    CONTENT_WATCHING("콘텐츠 시청");

    private final String label;
}
