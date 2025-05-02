package com.cg.fairshare.service;

import com.cg.fairshare.dto.GroupRequest;
import com.cg.fairshare.dto.ParticipantRequest;
import com.cg.fairshare.model.Group;
import com.cg.fairshare.model.Participant;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface IGroupService {
    Group createGroup(GroupRequest request);

    Participant addParticipant(Long groupId, ParticipantRequest dto);

    List<Participant> listParticipants(Long groupId);

    ResponseEntity<?> deleteGroupById(Long groupId);

    Group removeParticipant(Long groupId, Long userId, Long participantId);
}
