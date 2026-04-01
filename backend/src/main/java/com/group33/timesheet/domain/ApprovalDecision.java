package com.group33.timesheet.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "approval_decisions")
public class ApprovalDecision {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DecisionType decision;

    @Column(nullable = false)
    private LocalDateTime decidedAt;

    @Column(nullable = false)
    private String managerId;

    private String comment;

    @OneToOne
    @JoinColumn(name = "timesheet_id", nullable = false, unique = true)
    private Timesheet timesheet;

    public ApprovalDecision() {
    }

    public ApprovalDecision(DecisionType decision, String managerId, String comment, Timesheet timesheet) {
        this.decision = decision;
        this.decidedAt = LocalDateTime.now();
        this.managerId = managerId;
        this.comment = comment;
        this.timesheet = timesheet;
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

    public Timesheet getTimesheet() {
        return timesheet;
    }
}
