package com.group33.timesheet.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import com.group33.timesheet.domain.ApprovalDecision;
import com.group33.timesheet.domain.DecisionType;

public class ApprovalDecisionResponse {

    private UUID id;
    private DecisionType decision;
    private LocalDateTime decidedAt;
    private String managerId;
    private String comment;

    public static ApprovalDecisionResponse from(ApprovalDecision decision) {
        ApprovalDecisionResponse response = new ApprovalDecisionResponse();
        response.id = decision.getId();
        response.decision = decision.getDecision();
        response.decidedAt = decision.getDecidedAt();
        response.managerId = decision.getManagerId();
        response.comment = decision.getComment();
        return response;
    }

    public UUID getId() {
        return id;
    }

    public DecisionType getDecision() {
        return decision;
    }

    public LocalDateTime getDecidedAt() {
        return decidedAt;
    }

    public String getManagerId() {
        return managerId;
    }

    public String getComment() {
        return comment;
    }
}
