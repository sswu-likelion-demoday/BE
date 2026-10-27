package com.sujeongring.global.auth;

import com.sujeongring.domain.auth.exception.AuthErrorCode;
import com.sujeongring.global.error.exception.BaseException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class CurrentUserProvider {

    public Long getCurrentUserId() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BaseException(AuthErrorCode.UNAUTHORIZED);
        }

        Object principal = authentication.getPrincipal();

        if (principal == null || "anonymousUser".equals(principal)) {
            throw new BaseException(AuthErrorCode.UNAUTHORIZED);
        }

        if (principal instanceof Long userId) {
            return userId;
        }

        if (principal instanceof String principalValue) {
            try {
                return Long.parseLong(principalValue);
            } catch (NumberFormatException e) {
                throw new BaseException(AuthErrorCode.INVALID_ACCESS_TOKEN);
            }
        }

        throw new BaseException(AuthErrorCode.INVALID_ACCESS_TOKEN);
    }
}
