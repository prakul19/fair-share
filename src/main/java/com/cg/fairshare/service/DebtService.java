package com.cg.fairshare.service;

import com.cg.fairshare.model.*;
import com.cg.fairshare.repository.DebtRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class DebtService {

    private final DebtRepository debtRepository;

    @Transactional
    public void calculateGroupDebts(Group group) {
        // Delete existing debts for the group to recalculate from scratch
        debtRepository.deleteByGroup(group);

        Map<String, Debt> debtMap = new HashMap<>();

        for (Expense expense : group.getExpenses()) {
            User payer = expense.getPaidBy();

            for (ExpenseShare share : expense.getExpenseShares()) {
                User borrower = share.getUser();
                Double shareAmount = share.getAmount();

                if (!borrower.getId().equals(payer.getId())) {
                    String key = borrower.getId() + "-" + payer.getId();

                    debtMap.compute(key, (k, existingDebt) -> {
                        if (existingDebt == null) {
                            return Debt.builder()
                                    .fromUser(borrower)
                                    .toUser(payer)
                                    .group(group)
                                    .amount(shareAmount)
                                    .isActive(true)
                                    .build();
                        } else {
                            existingDebt.setAmount(existingDebt.getAmount() + shareAmount);
                            return existingDebt;
                        }
                    });
                }
            }
        }

        System.out.println(debtMap.values());

        debtRepository.saveAll(debtMap.values());
    }
}
