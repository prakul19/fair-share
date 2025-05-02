package com.cg.fairshare.repository;

import com.cg.fairshare.model.ExpenseShare;
import com.cg.fairshare.model.Expense;
import com.cg.fairshare.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExpenseShareRepository extends JpaRepository<ExpenseShare, Long> {
    List<ExpenseShare> findByUser(User user);
    List<ExpenseShare> findByExpense(Expense expense);
    void deleteAllByExpense(Expense expense);

}
