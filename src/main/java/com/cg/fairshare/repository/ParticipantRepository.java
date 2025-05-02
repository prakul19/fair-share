package com.cg.fairshare.repository;

import com.cg.fairshare.model.Participant;
import com.cg.fairshare.model.User;
import com.cg.fairshare.model.Group;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;

public interface ParticipantRepository extends JpaRepository<Participant, Long> {
    Optional<Participant> findByUserAndGroup(User user, Group group);
    List<Participant> findByGroup(Group group);
    boolean existsByGroupAndUser(Group group, User user);
}
