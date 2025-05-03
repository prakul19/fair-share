package com.cg.fairshare.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class DebtUpdateRequest {
    @NotNull(message = "Amount cant be null")
    @Positive(message = "Amount should be positive")
    Double amount;
}
