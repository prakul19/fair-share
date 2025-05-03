package com.cg.fairshare.controller;

import com.cg.fairshare.dto.DebtResponse;
import com.cg.fairshare.dto.DebtUpdateRequest;
import com.cg.fairshare.dto.GroupRequest;
import com.cg.fairshare.dto.ParticipantRequest;
import com.cg.fairshare.dto.TransactionDTO;
import com.cg.fairshare.exception.GroupNotFoundException;
import com.cg.fairshare.model.Group;
import com.cg.fairshare.model.Participant;
import com.cg.fairshare.repository.GroupRepository;
import com.cg.fairshare.response.ApiResponse;
import com.cg.fairshare.service.DebtService;
import com.cg.fairshare.service.EmailService;
import com.cg.fairshare.service.GroupServiceImpl;
import com.cg.fairshare.util.ResponseUtil;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Transactional
@RestController
@RequestMapping("/groups")
@RequiredArgsConstructor
public class GroupController {

    private final DebtService debtService;
    private final GroupServiceImpl groupService;
    private final GroupRepository groupRepository;
    private final EmailService emailService;

    @PostMapping
    public ResponseEntity<ApiResponse<Group>> createGroup(@Valid @RequestBody GroupRequest dto) {
        Group group = groupService.createGroup(dto);
        return ResponseUtil.ok(group, "Group created successfully");
    }

    @PostMapping("/{groupId}/participants")
    public ResponseEntity<ApiResponse<Participant>> addParticipant(
            @PathVariable Long groupId,
            @Valid @RequestBody ParticipantRequest dto) {
        Participant participant = groupService.addParticipant(groupId, dto);
        return ResponseUtil.ok(participant, "Participant added successfully");
    }

    @GetMapping("/{groupId}/participants")
    public ResponseEntity<ApiResponse<List<Participant>>> listParticipants(@PathVariable Long groupId) {
        List<Participant> participants = groupService.listParticipants(groupId);
        return ResponseUtil.ok(participants, "Participants fetched successfully");
    }

    @GetMapping("/calculatedebt/{groupId}")
    public ResponseEntity<ApiResponse<Group>> getGroupDetails(@PathVariable Long groupId) {
        Group group = groupRepository.getGroupById(groupId);
        debtService.calculateGroupDebts(group);
        return ResponseUtil.ok(group, "Group details with calculated debts");
    }

    @GetMapping("/{groupId}/debts")
    public ResponseEntity<ApiResponse<List<DebtResponse>>> listGroupDebts(@PathVariable Long groupId) {
        Group group = groupRepository.getGroupById(groupId);
        debtService.calculateGroupDebts(group);
        List<DebtResponse> debts = debtService.listDebtsForGroup(group);
        return ResponseUtil.ok(debts, "Debts listed successfully");
    }

    @GetMapping("/{groupId}/debts/optimize")
    public ResponseEntity<ApiResponse<List<TransactionDTO>>> optimizeGroupDebts(@PathVariable Long groupId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new GroupNotFoundException("Group with ID " + groupId + " not found"));

        List<TransactionDTO> plan = debtService.optimizeGroupDebts(group);
        return ResponseUtil.ok(plan, "Optimized debt transactions generated");
    }


    @DeleteMapping("/deletegroup/{groupId}")
    public ResponseEntity<ApiResponse<Void>> deleteGroup(@PathVariable Long groupId) {
        String result = groupService.deleteGroupById(groupId).getBody();
        return ResponseUtil.ok(result);
    }

    @DeleteMapping("/{groupId}/participants/{userId}/{participantId}")
    public ResponseEntity<ApiResponse<Group>> removeParticipant(
            @PathVariable Long groupId,
            @PathVariable Long userId,
            @PathVariable Long participantId) {
        Group updated = groupService.removeParticipant(groupId, userId, participantId);
        return ResponseUtil.ok(updated, "Participant removed successfully");
    }

    @PutMapping("/{debtId}/updateDebt")
    public ResponseEntity<ApiResponse<DebtResponse>> updateDebt(
            @PathVariable Long debtId,
            @RequestBody DebtUpdateRequest debtUpdateRequest) {
        DebtResponse updated = debtService.updateDebt(debtId, debtUpdateRequest).getBody();
        return ResponseUtil.ok(updated, "Debt updated successfully");
    }

    @GetMapping("/{groupId}/sendNotifications")
    public ResponseEntity<?> settleDebt(@PathVariable("groupId") Long groupId) {
        return debtService.notificationService(groupId);
    }

    @GetMapping("/{groupId}/debts/download")
    public ResponseEntity<ApiResponse<Void>> sendGroupSummaryEmail(@PathVariable Long groupId) {
        List<String> participantEmails = getParticipantsEmails(groupId);
        participantEmails.forEach(email -> emailService.sendGroupSummaryEmail(email, groupId));
        return ResponseUtil.ok("Summary email sent to all participants");
    }

    @GetMapping("/{groupId}/balance/{userId}")
    public ResponseEntity<ApiResponse<List<String>>> getUserBalance(
            @PathVariable Long groupId,
            @PathVariable Long userId) {
        List<String> balance = debtService.getUserBalanceInGroup(groupId, userId);
        return ResponseUtil.ok(balance, "User balance fetched successfully");
    }

    // Helper to fetch participant emails
    private List<String> getParticipantsEmails(Long groupId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));
        return group.getParticipants().stream()
                .map(p -> p.getUser().getEmail())
                .collect(Collectors.toList());
    }
}
