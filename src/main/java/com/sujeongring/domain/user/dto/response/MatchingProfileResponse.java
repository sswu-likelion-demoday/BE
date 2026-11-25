package com.sujeongring.domain.user.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.sujeongring.domain.user.enums.CalendarType;
import com.sujeongring.domain.user.enums.MbtiType;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record MatchingProfileResponse(

        MbtiType mbti,

        List<Option> socialStyles,

        List<Option> interests,

        List<Option> hobbies,

        List<Option> friendTypes,

        Saju saju

) {

    public record Option(
            String code,
            String name
    ) {
    }

    public record Saju(
            boolean enabled,

            @JsonFormat(pattern = "yyyy-MM-dd")
            LocalDate birthDate,

            CalendarType calendarType,

            @JsonFormat(pattern = "HH:mm")
            LocalTime birthTime
    ) {
    }
}