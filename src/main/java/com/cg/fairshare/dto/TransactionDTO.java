package com.cg.fairshare.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransactionDTO {
    @NotNull(message = "Field cannot be null")
    private String from;    // debtor’s name

    @NotNull(message = "Field cannot be null")
    private String to;  // creditor’s name

    @NotBlank(message = "Amount is required")
    private Double amount;  // amount to transfer
}
