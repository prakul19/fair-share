package com.cg.fairshare.controller;

import com.cg.fairshare.dto.GroupRequest;
import com.cg.fairshare.dto.ParticipantRequest;
import com.cg.fairshare.model.Group;
import com.cg.fairshare.model.Participant;
import com.cg.fairshare.service.GroupServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/groups")
public class GroupController {
    @Autowired
    private GroupServiceImpl groupService;

    @PostMapping
    public ResponseEntity<Group> createGroup(@RequestBody GroupRequest dto) {
        return ResponseEntity.ok(groupService.createGroup(dto));
    }

    @PostMapping("/{groupId}/participants")
    public ResponseEntity<Participant> addParticipant(
            @PathVariable Long groupId,
            @RequestBody ParticipantRequest dto) {

        return ResponseEntity.ok(groupService.addParticipant(groupId, dto));
    }

    @GetMapping("/{groupId}/participants")
    public ResponseEntity<List<Participant>> listParticipants(@PathVariable Long groupId) {
        return ResponseEntity.ok(groupService.listParticipants(groupId));
    }
}
