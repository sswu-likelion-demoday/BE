package com.sujeongring.domain.relationship.dto.response;

import com.sujeongring.domain.relationship.enums.RelationshipStatus;

import java.util.List;

public record PartnerProfileResponse(

        Long userId,
        String nickname,

        RelationshipStatus relationshipStatus,
        CurrentStage currentStage,

        ValueField<String> profileImage,
        ValuesField interests,
        ValuesField hobbies,
        ValueField<String> mbti,

        ValueField<String> name,
        ValueField<String> studentNumber,
        ValueField<String> department,

        ValueField<String> contact
) {

    public record CurrentStage(
            int stage,
            String name
    ) {
    }

    public record ValueField<T>(
            boolean unlocked,
            T value
    ) {
    }

    public record ValuesField(
            boolean unlocked,
            List<String> values
    ) {
    }
}
