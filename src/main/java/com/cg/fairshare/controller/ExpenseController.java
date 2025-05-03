package com.cg.fairshare.controller;

import com.cg.fairshare.dto.ExpenseRequest;
import com.cg.fairshare.model.Expense;
import com.cg.fairshare.service.ExpenseService;
import com.cg.fairshare.util.ResponseUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/expense")
public class ExpenseController {

    @Autowired private ExpenseService expenseService;

    @PostMapping("/add/{groupId}")
    public ResponseEntity<?> addExpense(@PathVariable Long groupId, @RequestBody ExpenseRequest expenseRequest) {
        return expenseService.addExpense(groupId, expenseRequest);
    }

    @GetMapping("/all/{groupId}")
    public ResponseEntity<?> getExpenses(@PathVariable Long groupId) {
        return expenseService.getExpenses(groupId);
    }

    @PutMapping("/update/{expenseId}")
    public ResponseEntity<?> updateExpense(@PathVariable Long expenseId, @RequestBody ExpenseRequest expenseRequest) {
        return expenseService.updateExpense(expenseId, expenseRequest);
    }

    @DeleteMapping("/delete/{expenseId}")
    public ResponseEntity<?> deleteExpense(@PathVariable Long expenseId) {
        return expenseService.deleteExpense(expenseId);
    }
}
