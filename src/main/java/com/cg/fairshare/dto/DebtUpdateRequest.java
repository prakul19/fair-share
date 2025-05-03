package com.cg.fairshare.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class DebtUpdateRequest {
    @NotNull(message = "Amount cant be null")
    Double amount;
}
