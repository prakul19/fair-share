package com.cg.fairshare.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

@Data
public class ExpenseRequest {
    @NotNull(message = "Description cant be null")
    private String description;

    @NotBlank(message = "Amount is required")
    private Double amount;

    @NotBlank(message = "User id is required")
    private Long paidByUserId;

    private List<Long> participantIds;
}