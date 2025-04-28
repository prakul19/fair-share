package com.cg.fairshare.dto;

import lombok.Data;

@Data
public class SettlementResponse {
    private Long fromUserId;
    private Long toUserId;
    private Double amount;
}

