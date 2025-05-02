package com.cg.fairshare.dto;

import lombok.Data;

@Data
public class ExpenseShareResponse {
    private Long id;
    private UserResponse user;
    private Double amount;
}
