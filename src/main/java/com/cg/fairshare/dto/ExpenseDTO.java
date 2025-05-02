package com.cg.fairshare.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class ExpenseDTO {
    private Long id;
    private String description;
    private Double amount;
    private LocalDate createdAt;
    private UserResponse paidBy;
    private GroupResponse group;
    private List<ExpenseShareResponse> expenseShareResponseList;
}
