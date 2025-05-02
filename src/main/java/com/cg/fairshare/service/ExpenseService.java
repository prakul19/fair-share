package com.cg.fairshare.service;

import com.cg.fairshare.dto.*;
import com.cg.fairshare.model.*;
import com.cg.fairshare.repository.*;
import org.apache.coyote.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResponseExtractor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
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

    @Override
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

    @Override
    public ResponseEntity<?> getAllExpenses(Long groupId) {
        try{
            Optional<Group> group = groupRepository.findById(groupId);
            if(group.isEmpty()){
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Group not found");
            }

            List<Expense> expenses = expenseRepository.findByGroup(group.get());
            List<ExpenseDTO> expenseDTOS = new ArrayList<>();

            for(Expense expense : expenses){
                ExpenseDTO dto = convertToExpenseDTO(expense);
                expenseDTOS.add(dto);
            }

            return ResponseEntity.ok(expenseDTOS);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error creating expense: " + e.getMessage());
        }
    }

    private ExpenseDTO convertToExpenseDTO(Expense expense) {
        ExpenseDTO dto = new ExpenseDTO();
        dto.setId(expense.getId());
        dto.setDescription(expense.getDescription());
        dto.setAmount(expense.getAmount());
        dto.setCreatedAt(expense.getCreatedAt().toLocalDate());


        User user = expense.getPaidBy();
        UserResponse userResponse = new UserResponse();
        userResponse.setId(user.getId());
        userResponse.setEmail(user.getEmail());
        userResponse.setName(user.getName());
        dto.setPaidBy(userResponse);


        Group group = expense.getGroup();
        GroupResponse groupResponse = new GroupResponse();
        groupResponse.setId(group.getId());
        groupResponse.setName(group.getName());
        dto.setGroup(groupResponse);

        List<ExpenseShare> expenseShares = expense.getExpenseShares();
        List<ExpenseShareResponse> shareResponses = new ArrayList<>();

        for(ExpenseShare share : expenseShares){
            ExpenseShareResponse shareResponse = convertToExpenseShareResponse(share);
            shareResponses.add(shareResponse);
        }
        dto.setExpenseShareResponseList(shareResponses);

        return dto;
    }





    private ExpenseShareResponse convertToExpenseShareResponse(ExpenseShare share) {
        ExpenseShareResponse response = new ExpenseShareResponse();
        response.setId(share.getId());
        response.setAmount(share.getAmount());

        User shareUser = share.getUser();
        UserResponse userResponse = new UserResponse();
        userResponse.setId(shareUser.getId());
        userResponse.setName(shareUser.getName());
        userResponse.setEmail(shareUser.getEmail());
        response.setUser(userResponse);

        return response;
    }
}
