package com.cg.fairshare.service;

import com.cg.fairshare.dto.GroupRequest;
import com.cg.fairshare.model.Group;

import java.util.List;

public interface IGroupService {
    Group createGroup(Long creatorId, GroupRequest request);
    List<Group> getGroupsForUser(Long userId);
}
