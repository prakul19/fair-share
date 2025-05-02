package com.cg.fairshare.dto;

import lombok.Data;

@Data
public class DebtResponse {
    private String debtorName;
    private String creditorName;
    private Double amount;
}
