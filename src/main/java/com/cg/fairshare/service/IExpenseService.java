package com.cg.fairshare.service;

import com.cg.fairshare.dto.ExpenseRequest;
import com.cg.fairshare.model.Expense;
import org.springframework.http.ResponseEntity;

public interface IExpenseService {

    ResponseEntity<?> addExpense(Long groupId, ExpenseRequest expenseRequest);

    ResponseEntity<?> getExpenses(Long groupId);

    ResponseEntity<?> updateExpense(Long expenseId, ExpenseRequest expenseRequest);

    ResponseEntity<?> deleteExpense(Long expenseId);
}
