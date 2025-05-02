package com.cg.fairshare.service;

import com.cg.fairshare.dto.GroupRequest;
import com.cg.fairshare.dto.ParticipantRequest;
import com.cg.fairshare.exception.GroupNotFoundException;
import com.cg.fairshare.model.Group;
import com.cg.fairshare.model.Participant;
import com.cg.fairshare.model.User;
import com.cg.fairshare.repository.GroupRepository;
import com.cg.fairshare.repository.ParticipantRepository;
import com.cg.fairshare.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class GroupServiceImpl implements IGroupService{
    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ParticipantRepository participantRepository;

    @Override
    public Group createGroup(GroupRequest dto) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        Optional<User> user = userRepository.findByEmail(email);

        Group group = new Group();
        if(user.isPresent()) {
            group.setCreatedBy(user.get());
        }
        group.setName(dto.getName());
        return groupRepository.save(group);
    }

    @Override
    public Participant addParticipant(Long groupId, ParticipantRequest dto) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new GroupNotFoundException("Group not found with ID: " + groupId));

        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + dto.getUserId()));

        boolean exists = participantRepository.existsByGroupAndUser(group, user);
        if (exists) {
            throw new RuntimeException("Participant already exists in the group");
        }

        Participant participant = new Participant();
        participant.setUser(user);
        participant.setGroup(group);
        participant.setJoinedAt(LocalDate.now());

        return participantRepository.save(participant);
    }

    @Override
    public List<Participant> listParticipants(Long groupId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new GroupNotFoundException("Group not found with id: " + groupId));
        return participantRepository.findByGroup(group);
    }

    public ResponseEntity<String> deleteGroupById(Long groupId) {
        groupRepository.deleteById(groupId);
        return new ResponseEntity<>("Your group has been deleted", HttpStatus.OK);
    }

    @Override
    public Group removeParticipant(Long groupId, Long userId, Long participantId) {
        Group group = groupRepository.getGroupById(groupId);
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            participantRepository.deleteById(participantId);
        }
        return groupRepository.save(group);
    }
}
