package com.cg.fairshare.controller;

import com.cg.fairshare.dto.DebtResponse;
import com.cg.fairshare.dto.DebtUpdateRequest;
import com.cg.fairshare.dto.GroupRequest;
import com.cg.fairshare.dto.ParticipantRequest;
import com.cg.fairshare.dto.TransactionDTO;
import com.cg.fairshare.model.Group;
import com.cg.fairshare.model.Participant;
import com.cg.fairshare.repository.GroupRepository;
import com.cg.fairshare.service.DebtService;
import com.cg.fairshare.service.EmailService;
import com.cg.fairshare.service.GroupServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/groups")
@RequiredArgsConstructor
public class GroupController {

    private final DebtService debtService;
    private final GroupServiceImpl groupService;
    private final GroupRepository groupRepository;
    private final EmailService emailService;

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

    @GetMapping("/calculatedebt/{groupId}")
    public ResponseEntity<Group> getGroupDetails(@PathVariable Long groupId) {
        Group group = groupRepository.getGroupById(groupId);
        debtService.calculateGroupDebts(group);
        return ResponseEntity.ok(group);
    }

    @GetMapping("/{groupId}/debts")
    public ResponseEntity<List<DebtResponse>> listGroupDebts(@PathVariable Long groupId) {
        Group group = groupRepository.getGroupById(groupId);
        debtService.calculateGroupDebts(group);
        List<DebtResponse> debts = debtService.listDebtsForGroup(group);
        return ResponseEntity.ok(debts);
    }

    @GetMapping("/{groupId}/debts/optimize")
    public ResponseEntity<List<TransactionDTO>> optimizeGroupDebts(@PathVariable Long groupId) {
        Group group = groupRepository.getGroupById(groupId);
        List<TransactionDTO> plan = debtService.optimizeGroupDebts(group);
        return ResponseEntity.ok(plan);
    }

    @DeleteMapping("/deletegroup/{groupId}")
    public ResponseEntity<String> deleteGroup(@PathVariable Long groupId){
        return groupService.deleteGroupById(groupId);
    }

    @DeleteMapping("/{groupId}/participants/{userId}/{participantId}")
    public ResponseEntity<Group> removeParticipant(
            @PathVariable Long groupId,
            @PathVariable Long userId,
            @PathVariable Long participantId) {
        return ResponseEntity.ok(groupService.removeParticipant(groupId, userId, participantId));
    }

    @PutMapping("/{debtId}/updateDebt")
    public ResponseEntity<DebtResponse> UpdateDepts(@PathVariable Long debtId, @RequestBody DebtUpdateRequest debtUpdateRequest){
            return debtService.updateDebt(debtId,debtUpdateRequest);
    }

    @GetMapping("/{groupId}/debts/download")
    public ResponseEntity<String> sendGroupSummaryEmail(@PathVariable Long groupId) {
        // Get the list of participants' emails for the group
        List<String> participantEmails = getParticipantsEmails(groupId); // This can be a method fetching emails from the participants

        for (String email : participantEmails) {
            // Send the generated Excel file to each participant's email
            emailService.sendGroupSummaryEmail(email, groupId);
        }

        return ResponseEntity.ok("File has been sent to participants.");
    }

    // Helper method to get participant emails
    private List<String> getParticipantsEmails(Long groupId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));

        // Assuming you have a method to get the participants' emails
        return group.getParticipants().stream()
                .map(participant -> participant.getUser().getEmail())
                .collect(Collectors.toList());
    }

    @Transactional
    @GetMapping("/{groupId}/balance/{userId}")
    public ResponseEntity<List<String>> getUserBalance(
            @PathVariable Long groupId,
            @PathVariable Long userId) {

        List<String> balance = debtService.getUserBalanceInGroup(groupId, userId);
        return ResponseEntity.ok(balance);
    }

}
