package com.cg.fairshare.service;

import com.cg.fairshare.dto.DebtResponse;
import com.cg.fairshare.dto.DebtUpdateRequest;

import com.cg.fairshare.dto.TransactionDTO;
import com.cg.fairshare.model.Debt;
import com.cg.fairshare.model.Expense;
import com.cg.fairshare.model.ExpenseShare;
import com.cg.fairshare.model.Group;
import com.cg.fairshare.model.User;
import com.cg.fairshare.repository.DebtRepository;
import com.cg.fairshare.repository.GroupRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DebtService {

    @Autowired
    private EmailServiceImpl emailService;

    @Autowired
    private DebtRepository debtRepository;

    @Autowired
    private GroupRepository groupRepository;

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

    public ResponseEntity<DebtResponse> updateDebt(Long id, DebtUpdateRequest debtUpdateRequest){
        Optional<Debt> currDebt = debtRepository.findById(id);
        if(currDebt.isPresent()){
            Debt debt = currDebt.get();

            if(debtUpdateRequest.getAmount() != null){
                debt.setAmount(debtUpdateRequest.getAmount());
            }
            debt.setActive(true);
            debtRepository.save(debt);
            return new ResponseEntity<>(new DebtResponse(), HttpStatus.OK);
        }
        return new ResponseEntity<>(new DebtResponse(), HttpStatus.BAD_REQUEST);
    }

    public ResponseEntity<?> settleDebtService(Long id) {
        Optional<Group> getGroup = groupRepository.findById(id);

        if(getGroup.isPresent()){
            Group group = getGroup.get();

            if(!group.isDebtSettled()){
                optimizeGroupDebts(group);
            }
            List<Debt> list = debtRepository.findByGroupAndIsActiveTrue(group);

            for(Debt debt:list){
                String fromUserEmail = debt.getFromUser().getEmail(); // email of user who owes to the other user
                String subject = "Settle your debts";

                String text = "You owe " + debt.getToUser().getName() + " $" + debt.getAmount();

                emailService.sendSimpleMessage(fromUserEmail,subject, text);
            }
            return new ResponseEntity<>("The Debts are settled and everyone is informed via email", HttpStatus.OK);
        }
        return new ResponseEntity<>("No such group exist", HttpStatus.BAD_REQUEST);
    }
}
