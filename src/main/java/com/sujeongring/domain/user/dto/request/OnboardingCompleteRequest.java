package com.sujeongring.domain.user.dto.request;

import com.sujeongring.domain.user.enums.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record OnboardingCompleteRequest(

        @NotNull
        MbtiType mbti,

        @NotEmpty
        @Size(max = 2)
        List<SocialStyleType> socialStyles,

        @NotEmpty
        @Size(min = 3, max = 5)
        List<InterestType> interests,

        @NotEmpty
        @Size(min = 3, max = 5)
        List<HobbyType> hobbies,

        @NotEmpty
        @Size(max = 3)
        List<FriendType> friendTypes

) {
}