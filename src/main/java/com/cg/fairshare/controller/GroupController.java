package com.cg.fairshare.controller;

import com.cg.fairshare.dto.GroupRequest;
import com.cg.fairshare.model.Group;
import com.cg.fairshare.service.IGroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
public class GroupController {

    private final IGroupService groupService;

    // Create a new group
    @PostMapping("/create")
    public ResponseEntity<Group> createGroup(@RequestParam Long creatorId,
                                             @RequestBody GroupRequest groupRequest) {
        Group createdGroup = groupService.createGroup(creatorId, groupRequest);
        return ResponseEntity.ok(createdGroup);
    }

    // Get all groups for a user
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Group>> getGroupsForUser(@PathVariable Long userId) {
        List<Group> groups = groupService.getGroupsForUser(userId);
        return ResponseEntity.ok(groups);
    }
}
