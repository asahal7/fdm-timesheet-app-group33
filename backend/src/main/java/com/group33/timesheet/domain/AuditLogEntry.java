package com.group33.timesheet.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "audit_log_entries")
public class AuditLogEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuditActionType action;

    @Column(nullable = false)
    private String actorEmployeeId;

    @Column(nullable = false)
    private LocalDateTime occurredAt;

    @Column(nullable = false)
    private String details;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "timesheet_id", nullable = false)
    private Timesheet timesheet;

    public AuditLogEntry() {
    }

    public AuditLogEntry(AuditActionType action, String actorEmployeeId, String details, Timesheet timesheet) {
        this.action = action;
        this.actorEmployeeId = actorEmployeeId;
        this.occurredAt = LocalDateTime.now();
        this.details = details;
        this.timesheet = timesheet;
    }

    public UUID getId() {
        return id;
    }

    public AuditActionType getAction() {
        return action;
    }

    public String getActorEmployeeId() {
        return actorEmployeeId;
    }

    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }

    public String getDetails() {
        return details;
    }

    public Timesheet getTimesheet() {
        return timesheet;
    }
}
