package com.cg.fairshare.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ParticipantRequest {
    @NotBlank(message = "User id is required")
    private Long userId;
}

