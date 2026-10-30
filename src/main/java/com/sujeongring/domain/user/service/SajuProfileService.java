
package com.sujeongring.domain.user.service;

import com.sujeongring.domain.user.dto.request.SajuSettingsRequest;
import com.sujeongring.domain.user.dto.response.SajuSettingsResponse;
import com.sujeongring.domain.user.entity.SajuProfile;
import com.sujeongring.domain.user.entity.User;
import com.sujeongring.domain.user.repository.SajuProfileRepository;
import com.sujeongring.domain.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SajuProfileService {

    private final SajuProfileRepository sajuProfileRepository;
    private final UserRepository userRepository;

    // 사주 설정 조회
    @Transactional(readOnly = true)
    public SajuSettingsResponse getSettings(Long userId) {

        return sajuProfileRepository.findByUserId(userId)
                .map(SajuSettingsResponse::from)
                .orElseGet(SajuSettingsResponse::disabled);
    }

    @Transactional
    public SajuSettingsResponse updateSettings(
            Long userId,
            SajuSettingsRequest request
    ) {

        // 사용자 조회 및 행 잠금
        User user = userRepository.findByIdForUpdate(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "사용자를 찾을 수 없습니다."
                        )
                );

        // 기존 사주 프로필 조회
        SajuProfile profile = sajuProfileRepository
                .findByUserId(userId)
                .orElse(null);

        if (profile == null) {

            // 최초 등록
            profile = new SajuProfile(
                    user,
                    request.matchingEnabled(),
                    request.calendarType(),
                    request.birthDate(),
                    request.birthTime()
            );

            sajuProfileRepository.save(profile);

        } else {

            // 기존 설정 변경
            profile.updateSettings(
                    request.matchingEnabled(),
                    request.calendarType(),
                    request.birthDate(),
                    request.birthTime()
            );
        }

        return SajuSettingsResponse.from(profile);
    }
}
