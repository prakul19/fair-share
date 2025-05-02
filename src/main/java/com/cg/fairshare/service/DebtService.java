package com.cg.fairshare.service;

import com.cg.fairshare.dto.DebtResponse;
import com.cg.fairshare.dto.TransactionDTO;
import com.cg.fairshare.model.*;
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
                .map(d -> {
                    DebtResponse dto = new DebtResponse();
                    dto.setDebtorName(d.getFromUser().getName());
                    dto.setCreditorName(d.getToUser().getName());
                    dto.setAmount(d.getAmount());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public List<TransactionDTO> optimizeGroupDebts(Group group) {
        calculateGroupDebts(group);

        Map<Long, Double> net = new HashMap<>();
        group.getParticipants().forEach(p ->
                net.put(p.getUser().getId(), 0.0)
        );
        debtRepository.findByGroupAndIsActiveTrue(group).forEach(d -> {
            net.compute(d.getFromUser().getId(), (id, bal) -> bal - d.getAmount());
            net.compute(d.getToUser().getId(),   (id, bal) -> bal + d.getAmount());
        });

        PriorityQueue<Map.Entry<Long, Double>> debtors = new PriorityQueue<>(
                Comparator.comparingDouble(Map.Entry::getValue)
        );
        PriorityQueue<Map.Entry<Long, Double>> creditors = new PriorityQueue<>(
                Comparator.<Map.Entry<Long, Double>>comparingDouble(Map.Entry::getValue).reversed()
        );
        net.forEach((userId, bal) -> {
            if (bal < -1e-6) debtors.add(Map.entry(userId, bal));
            else if (bal >  1e-6) creditors.add(Map.entry(userId, bal));
        });

        Map<Long, String> idToName = group.getParticipants()
                .stream()
                .collect(Collectors.toMap(p -> p.getUser().getId(), p -> p.getUser().getName()));

        List<TransactionDTO> plan = new ArrayList<>();
        while (!debtors.isEmpty() && !creditors.isEmpty()) {
            var d = debtors.poll();
            var c = creditors.poll();
            double settle = Math.min(-d.getValue(), c.getValue());

            plan.add(new TransactionDTO(
                    idToName.get(d.getKey()),
                    idToName.get(c.getKey()),
                    settle
            ));

            double newDebtBal = d.getValue() + settle;
            double newCredBal = c.getValue() - settle;
            if (newDebtBal < -1e-6) debtors.add(Map.entry(d.getKey(), newDebtBal));
            if (newCredBal >  1e-6) creditors.add(Map.entry(c.getKey(), newCredBal));
        }
        return plan;
    }
}
