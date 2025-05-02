package com.cg.fairshare.repository;

import com.cg.fairshare.model.Debt;
import com.cg.fairshare.model.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DebtRepository extends JpaRepository<Debt, Long> {
    void deleteByGroup(Group group);
    List<Debt> findByGroupAndIsActiveTrue(Group group);
}
