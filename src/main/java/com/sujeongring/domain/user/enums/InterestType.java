package com.sujeongring.domain.user.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum InterestType {

    ART_EXHIBITION("예술전시"),
    MOVIE_DRAMA("영화·드라마"),
    MUSIC_CONCERT("음악·공연"),
    TRAVEL("여행"),
    FOOD_CAFE("맛집·카페"),
    FASHION_BEAUTY("패션·뷰티"),
    BOOK_WRITING("책·글쓰기"),
    IT_TECH("IT·테크"),
    FINANCE("경제·재테크"),
    ANIMAL("동물"),
    GAME("게임"),
    SPORTS("스포츠"),
    COOKING_BAKING("요리·베이킹"),
    SELF_DEVELOPMENT("자기계발"),
    CAREER("취업·커리어");

    private final String label;
}
