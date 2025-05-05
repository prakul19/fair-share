package com.cg.fairshare.service;

import com.cg.fairshare.dto.DebtResponse;
import com.cg.fairshare.dto.DebtUpdateRequest;

import com.cg.fairshare.dto.TransactionDTO;
import com.cg.fairshare.model.*;
import com.cg.fairshare.repository.DebtRepository;
import com.cg.fairshare.repository.GroupRepository;
import com.cg.fairshare.response.ApiResponse;
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
    private EmailService emailService;

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

    public ResponseEntity<?> notificationService(Long id) {
        Optional<Group> getGroup = groupRepository.findById(id);
        if(getGroup.isPresent()){
            Group group = getGroup.get();
            List<TransactionDTO> optimizedDebts = optimizeGroupDebts(group);
            for (TransactionDTO txn : optimizedDebts) {
                Optional<User> fromUserOpt = group.getParticipants().stream()
                        .map(p -> p.getUser())
                        .filter(u -> u.getName().equals(txn.getFrom()))
                        .findFirst();
                if (fromUserOpt.isPresent()) {
                    String fromUserEmail = fromUserOpt.get().getEmail();
                    String subject = "Settle your debts";
                    String text = "You owe " + txn.getTo() + " Rs." + txn.getAmount();
                    emailService.sendSimpleMessage(fromUserEmail, subject, text);
                }
            }
            ApiResponse<String> response = new ApiResponse<>(
                    true,
                    "Optimized Debts settlement notifications have been sent via email",
                    null
            );
            return ResponseEntity.ok(response);
        }
        ApiResponse<String> response = new ApiResponse<>(
                false,
                "No such group exists",
                null
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }


    public byte[] generateGroupSummaryExcel(Long groupId) {
        Group group = groupRepository.getGroupById(groupId);
        List<Debt> activeDebts = debtRepository.findByGroupAndIsActiveTrue(group);

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
            for (Debt debt : activeDebts) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(debt.getFromUser().getName());
                row.createCell(1).setCellValue(debt.getToUser().getName());
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

        // Check if user exists in group
        boolean userFound = false;
        for (Participant participant : group.getParticipants()) {
            if (participant.getUser().getId().equals(userId)) {
                userFound = true;
                break;
            }
        }

        if (!userFound) {
            throw new RuntimeException("User not found in the group");
        }

        // Use optimized debt calculation
        List<Debt> activeDebts = debtRepository.findByGroupAndIsActiveTrue(group);

        List<String> balanceSummary = new ArrayList<>();

        for (Debt debt : activeDebts) {
            if (debt.getFromUser().getId().equals(userId)) {
                balanceSummary.add("You owe ₹" + debt.getAmount() + " to " + debt.getToUser().getName());
            } else if (debt.getToUser().getId().equals(userId)) {
                balanceSummary.add(debt.getFromUser().getName() + " owes you ₹" + debt.getAmount());
            }
        }

        if (balanceSummary.isEmpty()) {
            balanceSummary.add("You are all settled up in this group!");
        }

        return balanceSummary;
    }


    public ResponseEntity<?> settleDebtsService(Long debtId, TransactionDTO transactionDTO) {
        Optional<Debt> debt = debtRepository.findById(debtId);
        if(debt.isPresent()){
            Debt currDebt = debt.get();
            if(currDebt.getAmount()>=transactionDTO.getAmount()){
                if(currDebt.getAmount().equals(transactionDTO.getAmount())) {
                    currDebt.setActive(false);  // Mark debt as settled
                    debtRepository.save(currDebt);
                    return new ResponseEntity<>("Your Debt has been settled", HttpStatus.OK);
                } else {
                    currDebt.setAmount(currDebt.getAmount() - transactionDTO.getAmount());
                    debtRepository.save(currDebt);
                }
            }
            else{
                User currDebtFrom = currDebt.getFromUser();
                User currDebtTo = currDebt.getToUser();
                currDebt.setFromUser(currDebtTo);
                currDebt.setToUser(currDebtFrom);
                currDebt.setAmount(transactionDTO.getAmount()-currDebt.getAmount());
            }
            debtRepository.save(currDebt);
            return new ResponseEntity<>("Your Debt has been settled", HttpStatus.OK);
        }
        return new ResponseEntity<>("No such debt is present", HttpStatus.BAD_REQUEST);
    }
}
