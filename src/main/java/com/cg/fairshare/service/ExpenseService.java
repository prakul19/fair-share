package com.cg.fairshare.service;

import com.cg.fairshare.dto.ExpenseRequest;
import com.cg.fairshare.model.*;
import com.cg.fairshare.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ExpenseService implements IExpenseService {
    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private ExpenseShareRepository expenseShareRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private ParticipantRepository participantRepository;

    public ResponseEntity<?> addExpense(Long groupId, ExpenseRequest expenseRequest) {
        try{

            Optional<User> paidByUser = userRepository.findById(expenseRequest.getPaidByUserId());

            Optional<Group> group = groupRepository.findById(groupId);

            if(paidByUser.isEmpty() || group.isEmpty()){
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User or group not found");
            }

            boolean isNotMember = participantRepository.findByUserAndGroup(paidByUser.get(),group.get()).isEmpty();

            if(isNotMember){
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User is not a member of the group");
            }

            Expense expense = new Expense();
            expense.setDescription(expenseRequest.getDescription());
            expense.setAmount(expenseRequest.getAmount());
            expense.setPaidBy(paidByUser.get());
            expense.setGroup(group.get());
            expense.setCreatedAt(LocalDateTime.now());

            Expense savedExpense = expenseRepository.save(expense);

            List<Participant> participants = participantRepository.findByGroup(group.get());

            if(participants.isEmpty()){
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No participants found");
            }

            Double shareAmount = expenseRequest.getAmount()/participants.size();

            for(Participant participant : participants){
                ExpenseShare share = new ExpenseShare();
                share.setExpense(savedExpense);
                share.setUser(participant.getUser());
                share.setAmount(shareAmount);

                expenseShareRepository.save(share);
            }

            return ResponseEntity.ok("Expense added and split equally among the members");

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error creating expense: " + e.getMessage());
        }
    }
}
