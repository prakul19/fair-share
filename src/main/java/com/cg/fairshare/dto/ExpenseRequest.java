package com.cg.fairshare.dto;

import lombok.Data;
import java.util.List;

@Data
public class ExpenseRequest {
    private String description;
    private Double amount;
    private Long paidByUserId;
//    private List<Long> participantIds;
}

