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
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.*;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Slf4j
public class DebtService {
    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private EmailServiceImpl emailService;

    @Autowired
    private DebtRepository debtRepository;

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
    public byte[] generateGroupSummaryExcel(Long groupId) {
        Group group = groupRepository.getGroupById(groupId);
        List<DebtResponse> summary = listDebtsForGroup(group);

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Group Summary");

            // Header row
            Row headerRow = sheet.createRow(0);
            String[] headers = {"Debtor", "Creditor", "Amount"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
            }

            // Data rows
            int rowNum = 1;
            for (DebtResponse debt : summary) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(debt.getDebtorName());
                row.createCell(1).setCellValue(debt.getCreditorName());
                row.createCell(2).setCellValue(debt.getAmount());
            }

            workbook.write(out);
            return out.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate Excel file: " + e.getMessage(), e);
        }
    }

    public List<String> getUserBalanceInGroup(Long groupId, Long userId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));



        // Recalculate debts for safety
        calculateGroupDebts(group);

        List<Debt> debts = debtRepository.findByGroupAndIsActiveTrue(group);
        List<String> balanceSummary = new ArrayList<>();

        for (Debt debt : debts) {
            Long fromId = debt.getFromUser().getId();
            Long toId = debt.getToUser().getId();
            double amount = debt.getAmount();

            if (fromId.equals(userId)) {
                balanceSummary.add("You owe ₹" + amount + " to " + debt.getToUser().getName());
            } else if (toId.equals(userId)) {
                balanceSummary.add(debt.getFromUser().getName() + " owes you ₹" + amount);
            }
        }

        if (balanceSummary.isEmpty()) {
            balanceSummary.add("You are all settled up in this group!");
        }

        return balanceSummary;
    }
}
