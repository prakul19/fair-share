package com.cg.fairshare.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import java.util.List;

@Data
public class ExpenseRequest {
    @NotNull(message = "Description cant be null")
    private String description;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount should be positive")
    private Double amount;

    @NotNull(message = "User id is required")
    private Long paidByUserId;

    private List<Long> participantIds;
}