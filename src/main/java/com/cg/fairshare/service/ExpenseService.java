package com.cg.fairshare.service;

import com.cg.fairshare.dto.*;
import com.cg.fairshare.model.*;
import com.cg.fairshare.repository.*;
import com.cg.fairshare.util.ResponseUtil;
import org.apache.coyote.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
                return ResponseUtil.error(HttpStatus.NOT_FOUND.value(),"User or group not found");
            }

            boolean isNotMember = participantRepository.findByUserAndGroup(paidByUser.get(),group.get()).isEmpty();

            if(isNotMember){
                return ResponseUtil.error(HttpStatus.BAD_REQUEST.value(),"User is not a member of the group");
            }

            if(expenseRequest.getParticipantIds()==null || expenseRequest.getParticipantIds().isEmpty()){
                return ResponseUtil.error(HttpStatus.BAD_REQUEST.value(), "Must specify at least one participant");
            }

            List<Participant> participants = new ArrayList<>();
            for(Long participantId : expenseRequest.getParticipantIds()){
                Optional<User> user = userRepository.findById(participantId);
                if(user.isEmpty()){
                    return ResponseUtil.error(HttpStatus.NOT_FOUND.value(),"User with ID "+participantId+" not found");
                }

                Optional<Participant> participant = participantRepository.findByUserAndGroup(user.get(),group.get());
                if(participant.isEmpty()){
                    return ResponseUtil.error(HttpStatus.BAD_REQUEST.value(),"User with ID "+participantId+" is not a member of this group");
                }
                participants.add(participant.get());
            }

            Expense expense = new Expense();
            expense.setDescription(expenseRequest.getDescription());
            expense.setAmount(expenseRequest.getAmount());
            expense.setPaidBy(paidByUser.get());
            expense.setGroup(group.get());
            expense.setCreatedAt(LocalDateTime.now());

            Expense savedExpense = expenseRepository.save(expense);


            Double shareAmount = expenseRequest.getAmount()/participants.size();

            for(Participant participant : participants){
                ExpenseShare share = new ExpenseShare();
                share.setExpense(savedExpense);
                share.setUser(participant.getUser());
                share.setAmount(shareAmount);

                expenseShareRepository.save(share);
            }

            ExpenseDTO expenseDTO = convertToExpenseDTO(savedExpense);
            return ResponseUtil.ok(expenseDTO,"Expense added and split equally among the members");

        } catch (Exception e) {
            return ResponseUtil.error(HttpStatus.INTERNAL_SERVER_ERROR.value()
                    ,"Error creating expense: " + e.getMessage());
        }
    }

    @Override
    public ResponseEntity<?> getExpenses(Long groupId) {
        try{
            Optional<Group> group = groupRepository.findById(groupId);
            if(group.isEmpty()){
                return ResponseUtil.error(HttpStatus.NOT_FOUND.value(),"Group not found");
            }

            List<Expense> expenses = expenseRepository.findByGroup(group.get());


            if(expenses.isEmpty()){
                return ResponseUtil.error(HttpStatus.NOT_FOUND.value(),"No expenses found for this group");
            }

            List<ExpenseDTO> expenseDTOS = new ArrayList<>();

            for(Expense expense : expenses){
                ExpenseDTO dto = convertToExpenseDTO(expense);
                expenseDTOS.add(dto);
            }

            return ResponseUtil.ok(expenseDTOS,"Expense List of Group "+groupId);

        } catch (Exception e) {
            return ResponseUtil.error(HttpStatus.INTERNAL_SERVER_ERROR.value(),"Error creating expense: " + e.getMessage());
        }
    }

    @Override
    public ResponseEntity<?> updateExpense(Long expenseId, ExpenseRequest expenseRequest) {
        try{
            Optional<Expense> expense = expenseRepository.findById(expenseId);
            if (expense.isEmpty()) {
                return ResponseUtil.error(HttpStatus.NOT_FOUND.value(),"Expense not found");
            }

            Expense existingExpense = expense.get();
            Group group = existingExpense.getGroup();

            Optional<User> paidByUser = userRepository.findById(expenseRequest.getPaidByUserId());
            if (paidByUser.isEmpty()) {
                return ResponseUtil.error(HttpStatus.NOT_FOUND.value(),"Paying user not found");
            }

            if(expenseRequest.getParticipantIds()==null || expenseRequest.getParticipantIds().isEmpty()){
                return ResponseUtil.error(HttpStatus.BAD_REQUEST.value(), "Must specify at least one participant");
            }

            boolean isNotMember = participantRepository.findByUserAndGroup(paidByUser.get(), group).isEmpty();
            if (isNotMember) {
                return ResponseUtil.error(HttpStatus.BAD_REQUEST.value(),"User is not a member of the group");
            }

            List<Participant> participants = new ArrayList<>();
            for(Long participantId : expenseRequest.getParticipantIds()){
                Optional<User> user = userRepository.findById(participantId);
                if(user.isEmpty()){
                    return ResponseUtil.error(HttpStatus.NOT_FOUND.value(),"User with ID "+participantId+" not found");
                }

                Optional<Participant> participant = participantRepository.findByUserAndGroup(user.get(),group);
                if(participant.isEmpty()){
                    return ResponseUtil.error(HttpStatus.BAD_REQUEST.value(),"User with ID "+participantId+" is not a member of this group");
                }

                participants.add(participant.get());
            }

            existingExpense.setDescription(expenseRequest.getDescription());
            existingExpense.setAmount(expenseRequest.getAmount());
            existingExpense.setPaidBy(paidByUser.get());
            existingExpense.setCreatedAt(LocalDateTime.now());


            List<ExpenseShare> expenseShares = expenseShareRepository.findByExpense(existingExpense);
            expenseShareRepository.deleteAll(expenseShares);

            Double shareAmount = expenseRequest.getAmount() / participants.size();
            for (Participant participant : participants) {
                ExpenseShare share = new ExpenseShare();
                share.setExpense(existingExpense);
                share.setUser(participant.getUser());
                share.setAmount(shareAmount);
                expenseShareRepository.save(share);
            }

            expenseRepository.save(existingExpense);
            ExpenseDTO expenseDTO = convertToExpenseDTO(existingExpense);


            return ResponseUtil.ok(expenseDTO,"Expense updated successfully");
        } catch (Exception e) {
            return ResponseUtil.error(HttpStatus.INTERNAL_SERVER_ERROR.value(),"Error updating expense: " + e.getMessage());
        }

    }

    @Override
    @Transactional
    public ResponseEntity<?> deleteExpense(Long expenseId) {
        try{
            Optional<Expense> expense = expenseRepository.findById(expenseId);
            if (expense.isEmpty()) {
                return ResponseUtil.error(HttpStatus.NOT_FOUND.value(),"Expense not found");
            }

            expenseShareRepository.deleteAllByExpense(expense.get());

            expenseRepository.delete(expense.get());

            return ResponseUtil.ok("Expense deleted successfully");
        } catch (Exception e) {
            return ResponseUtil.error(HttpStatus.INTERNAL_SERVER_ERROR.value(),"Error deleting expense: " + e.getMessage());
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
