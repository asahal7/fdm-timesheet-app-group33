package com.group33.timesheet.domain;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "timesheets")
public class Timesheet {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String consultantId;

    @Column(nullable = false)
    private String managerId;

    @Column(nullable = false)
    private LocalDate weekStart;

    @Column(nullable = false)
    private LocalDate weekEnd;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TimesheetStatus status;

    private LocalDateTime submittedAt;

    @Column(nullable = false)
    private boolean locked;

    @OneToMany(mappedBy = "timesheet", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TimesheetEntry> entries = new ArrayList<>();

    @OneToOne(mappedBy = "timesheet", cascade = CascadeType.ALL, orphanRemoval = true)
    private ApprovalDecision approvalDecision;

    @OneToMany(mappedBy = "timesheet", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AuditLogEntry> auditLogs = new ArrayList<>();

    public Timesheet() {
    }

    public Timesheet(String consultantId, String managerId, LocalDate weekStart, LocalDate weekEnd) {
        this.consultantId = consultantId;
        this.managerId = managerId;
        this.weekStart = weekStart;
        this.weekEnd = weekEnd;
        this.status = TimesheetStatus.DRAFT;
        this.locked = false;
    }

    public void submit() {
        if (locked) {
            throw new IllegalStateException("Locked timesheet cannot be submitted.");
        }
        if (entries.isEmpty()) {
            throw new IllegalStateException("Cannot submit a timesheet with no entries.");
        }

        this.status = TimesheetStatus.PENDING_APPROVAL;
        this.submittedAt = LocalDateTime.now();
    }

    public void approve() {
        if (status != TimesheetStatus.PENDING_APPROVAL) {
            throw new IllegalStateException("Only pending timesheets can be approved.");
        }
        this.status = TimesheetStatus.APPROVED;
        this.locked = true;
    }

    public void reject() {
        if (status != TimesheetStatus.PENDING_APPROVAL) {
            throw new IllegalStateException("Only pending timesheets can be rejected.");
        }
        this.status = TimesheetStatus.REJECTED;
        this.locked = false;
    }

    public void addEntry(TimesheetEntry entry) {
        if (locked) {
            throw new IllegalStateException("Cannot edit a locked timesheet.");
        }
        if (entries.size() >= 7) {
            throw new IllegalStateException("A timesheet cannot contain more than 7 entries.");
        }

        entry.setTimesheet(this);
        this.entries.add(entry);
    }

    public UUID getId() {
        return id;
    }

    public String getConsultantId() {
        return consultantId;
    }

    public String getManagerId() {
        return managerId;
    }

    public LocalDate getWeekStart() {
        return weekStart;
    }

    public LocalDate getWeekEnd() {
        return weekEnd;
    }

    public TimesheetStatus getStatus() {
        return status;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public boolean isLocked() {
        return locked;
    }

    public List<TimesheetEntry> getEntries() {
        return entries;
    }

    public ApprovalDecision getApprovalDecision() {
        return approvalDecision;
    }

    public List<AuditLogEntry> getAuditLogs() {
        return auditLogs;
    }

    public void setApprovalDecision(ApprovalDecision approvalDecision) {
        this.approvalDecision = approvalDecision;
    }
}