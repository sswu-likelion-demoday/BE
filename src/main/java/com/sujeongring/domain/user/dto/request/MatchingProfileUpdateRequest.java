package com.sujeongring.domain.user.dto.request;

import com.sujeongring.domain.user.enums.*;
import java.util.List;

public record MatchingProfileUpdateRequest(
        MbtiType mbti,
        List<SocialStyleType> socialStyles,
        List<InterestType> interests,
        List<HobbyType> hobbies,
        List<FriendType> friendTypes
) {
}