package com.sujeongring.domain.auth.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(

        @JsonProperty("student_id")
        @NotBlank
        String studentId,

        @NotBlank
        String password
) {
}