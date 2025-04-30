package com.cg.fairshare.repository;

import com.cg.fairshare.model.Expense;
import com.cg.fairshare.model.Group;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    List<Expense> findByGroup(Group group);
}
