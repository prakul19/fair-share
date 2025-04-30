package com.cg.fairshare.service;

import com.cg.fairshare.dto.GroupRequest;
import com.cg.fairshare.model.Group;
import com.cg.fairshare.model.Participant;
import com.cg.fairshare.model.User;
import com.cg.fairshare.repository.GroupRepository;
import com.cg.fairshare.repository.ParticipantRepository;
import com.cg.fairshare.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GroupServiceImpl implements IGroupService {

    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final ParticipantRepository participantRepository;

    @Override
    @Transactional
    public Group createGroup(Long creatorId, GroupRequest request) {
        User creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Group group = Group.builder()
                .name(request.getName())
                .createdBy(creator)
                .build();

        group = groupRepository.save(group);

        Participant participant = Participant.builder()
                .user(creator)
                .group(group)
                .joinedAt(LocalDateTime.now())
                .build();

        participantRepository.save(participant);

        return group;
    }

    @Override
    public List<Group> getGroupsForUser(Long userId) {
        return groupRepository.findByParticipants_User_Id(userId);
    }
}
