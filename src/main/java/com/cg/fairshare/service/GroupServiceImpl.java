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
import org.springframework.stereotype.Service;

import java.util.List;

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
        Group group = new Group();
        group.setName(dto.getName());
        return groupRepository.save(group);
    }

    @Override
    public Participant addParticipant(Long groupId, ParticipantRequest dto) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new GroupNotFoundException("Group not found with ID: " + groupId));

        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + dto.getUserId()));

        Participant participant = new Participant();
        participant.setUser(user);
        participant.setGroup(group);

        return participantRepository.save(participant);
    }

    @Override
    public List<Participant> listParticipants(Long groupId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new GroupNotFoundException("Group not found with id: " + groupId));
        return participantRepository.findByGroup(group);
    }
}
