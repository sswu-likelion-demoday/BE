package com.sujeongring.global.s3;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProfileImageResolver {

    private final S3Service s3Service;

    public String resolve(
            String profileImageKey,
            boolean profileImageUnlocked
    ) {
        if (!profileImageUnlocked) {
            return null;
        }

        if (profileImageKey == null || profileImageKey.isBlank()) {
            return null;
        }

        return s3Service.getPresignedUrl(profileImageKey);
    }
}
