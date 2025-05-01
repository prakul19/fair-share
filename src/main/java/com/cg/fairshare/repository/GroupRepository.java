package com.cg.fairshare.repository;

import com.cg.fairshare.model.Group;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GroupRepository extends JpaRepository<Group, Long> {
    List<Group> findByParticipants_User_Id(Long userId);
    Group getGroupById(Long groupId);
}
