
package com.sujeongring.domain.user.dto.request;

import com.sujeongring.domain.user.enums.CalendarType;

import jakarta.validation.constraints.PastOrPresent;

import java.time.LocalDate;
import java.time.LocalTime;

public record SajuSettingsRequest(
        boolean matchingEnabled,
        CalendarType calendarType,
        @PastOrPresent LocalDate birthDate,
        LocalTime birthTime
) {
}
