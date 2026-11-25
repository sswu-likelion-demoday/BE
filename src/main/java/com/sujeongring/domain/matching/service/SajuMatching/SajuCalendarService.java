package com.sujeongring.domain.matching.service.SajuMatching;

import com.github.usingsky.calendar.KoreanLunarCalendar;
import com.sujeongring.domain.user.entity.SajuProfile;
import com.sujeongring.domain.user.enums.CalendarType;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class SajuCalendarService {

    public String calculateGapja(SajuProfile profile) {

        LocalDate birthDate = profile.getBirthDate();

        // 날짜 변환과 간지 추출을 함께 수행
        KoreanLunarCalendar calendar =
                KoreanLunarCalendar.getInstance();

        boolean valid;

        if (profile.getCalendarType() == CalendarType.SOLAR) {
            valid = calendar.setSolarDate(
                    birthDate.getYear(),
                    birthDate.getMonthValue(),
                    birthDate.getDayOfMonth()
            );
        } else {
            valid = calendar.setLunarDate(
                    birthDate.getYear(),
                    birthDate.getMonthValue(),
                    birthDate.getDayOfMonth(),
                    false
            );
        }

        if (!valid) {
            throw new IllegalArgumentException(
                    "지원하지 않거나 유효하지 않은 생년월일입니다."
            );
        }

        return calendar.getChineseGapJaString();
    }
}
