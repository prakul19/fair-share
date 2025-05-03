package com.cg.fairshare.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ParticipantRequest {
    private Long userId;
}

