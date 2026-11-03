
package com.sujeongring.domain.user.dto.response;

import com.sujeongring.domain.user.entity.SajuProfile;
import com.sujeongring.domain.user.enums.CalendarType;

import java.time.LocalDate;
import java.time.LocalTime;

public record SajuSettingsResponse(
        boolean matchingEnabled,
        CalendarType calendarType,
        LocalDate birthDate,
        LocalTime birthTime
) {

    public static SajuSettingsResponse from(
            SajuProfile profile
    ) {
        return new SajuSettingsResponse(
                profile.isMatchingEnabled(),
                profile.getCalendarType(),
                profile.getBirthDate(),
                profile.getBirthTime()
        );
    }

    public static SajuSettingsResponse disabled() {
        return new SajuSettingsResponse(
                false,
                null,
                null,
                null
        );
    }
}
