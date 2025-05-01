package com.cg.fairshare.service;

import com.cg.fairshare.dto.GroupRequest;
import com.cg.fairshare.dto.ParticipantRequest;
import com.cg.fairshare.model.Group;
import com.cg.fairshare.model.Participant;

import java.util.List;

public interface IGroupService {
    Group createGroup(GroupRequest request);
    Participant addParticipant(Long groupId, ParticipantRequest dto);

    List<Participant> listParticipants(Long groupId);
}
