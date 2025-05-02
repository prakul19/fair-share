package com.cg.fairshare.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransactionDTO {
    private String from;    // debtor’s name
    private String to;      // creditor’s name
    private Double amount;  // amount to transfer
}
