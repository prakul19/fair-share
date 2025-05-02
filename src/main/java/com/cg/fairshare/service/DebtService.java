package com.cg.fairshare.service;

import com.cg.fairshare.dto.DebtResponse;
import com.cg.fairshare.model.Debt;
import com.cg.fairshare.model.Expense;
import com.cg.fairshare.model.ExpenseShare;
import com.cg.fairshare.model.Group;
import com.cg.fairshare.model.User;
import com.cg.fairshare.repository.DebtRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DebtService {

    private final DebtRepository debtRepository;
    @Transactional
    public void calculateGroupDebts(Group group) {
        debtRepository.deleteByGroup(group);

        Map<String, Debt> debtMap = new HashMap<>();
        for (Expense expense : group.getExpenses()) {
            User payer = expense.getPaidBy();
            for (ExpenseShare share : expense.getExpenseShares()) {
                User borrower = share.getUser();
                if (!borrower.getId().equals(payer.getId())) {
                    String key = borrower.getId() + "-" + payer.getId();
                    debtMap.compute(key, (k, existing) -> {
                        if (existing == null) {
                            return Debt.builder()
                                    .fromUser(borrower)
                                    .toUser(payer)
                                    .group(group)
                                    .amount(share.getAmount())
                                    .isActive(true)
                                    .build();
                        } else {
                            existing.setAmount(existing.getAmount() + share.getAmount());
                            return existing;
                        }
                    });
                }
            }
        }
        debtRepository.saveAll(debtMap.values());
    }

    public List<DebtResponse> listDebtsForGroup(Group group) {
        return debtRepository.findByGroupAndIsActiveTrue(group)
                .stream()
                .map(debt -> {
                    DebtResponse dto = new DebtResponse();
                    dto.setDebtorName(debt.getFromUser().getName());
                    dto.setCreditorName(debt.getToUser().getName());
                    dto.setAmount(debt.getAmount());
                    return dto;
                })
                .collect(Collectors.toList());
    }
}
