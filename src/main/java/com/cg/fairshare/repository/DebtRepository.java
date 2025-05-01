package com.cg.fairshare.repository;

import com.cg.fairshare.model.Debt;
import com.cg.fairshare.model.Group;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DebtRepository extends JpaRepository<Debt, Long> {
    void deleteByGroup(Group group);
}
