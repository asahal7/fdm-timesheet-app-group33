package com.group33.timesheet.service;

import com.group33.timesheet.domain.*;
import com.group33.timesheet.dto.AddTimesheetEntryRequest;
import com.group33.timesheet.dto.ApprovalRequest;
import com.group33.timesheet.dto.CreateTimesheetRequest;
import com.group33.timesheet.exception.BadRequestException;
import com.group33.timesheet.exception.ResourceNotFoundException;
import com.group33.timesheet.repository.ApprovalDecisionRepository;
import com.group33.timesheet.repository.AuditLogEntryRepository;
import com.group33.timesheet.repository.TimesheetRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class TimesheetService {

    private final TimesheetRepository timesheetRepository;
    private final ApprovalDecisionRepository approvalDecisionRepository;
    private final AuditLogEntryRepository auditLogEntryRepository;

    public TimesheetService(TimesheetRepository timesheetRepository,
                            ApprovalDecisionRepository approvalDecisionRepository,
                            AuditLogEntryRepository auditLogEntryRepository) {
        this.timesheetRepository = timesheetRepository;
        this.approvalDecisionRepository = approvalDecisionRepository;
        this.auditLogEntryRepository = auditLogEntryRepository;
    }

    public Timesheet createTimesheet(CreateTimesheetRequest request) {
        if (request.getConsultantId() == null || request.getConsultantId().isBlank()) {
            throw new BadRequestException("consultantId is required.");
        }
        if (request.getManagerId() == null || request.getManagerId().isBlank()) {
            throw new BadRequestException("managerId is required.");
        }
        if (request.getWeekStart() == null || request.getWeekEnd() == null) {
            throw new BadRequestException("weekStart and weekEnd are required.");
        }

        Timesheet timesheet = new Timesheet(
                request.getConsultantId(),
                request.getManagerId(),
                request.getWeekStart(),
                request.getWeekEnd()
        );

        Timesheet saved = timesheetRepository.save(timesheet);

        auditLogEntryRepository.save(
                new AuditLogEntry(AuditActionType.CREATED, request.getConsultantId(), "Timesheet created.", saved)
        );

        return saved;
    }

    @Transactional(readOnly = true)
    public List<Timesheet> getAllTimesheets() {
        return timesheetRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Timesheet getTimesheetById(UUID id) {
        return timesheetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Timesheet not found: " + id));
    }

    public Timesheet addEntry(UUID timesheetId, AddTimesheetEntryRequest request) {
        Timesheet timesheet = getTimesheetById(timesheetId);

        TimesheetEntry entry = new TimesheetEntry(request.getDay(), request.getHours());
        if (!entry.isValid()) {
            throw new BadRequestException("Entry hours must be between 0 and 24.");
        }

        boolean dayAlreadyExists = timesheet.getEntries().stream()
                .anyMatch(existing -> existing.getDay() == request.getDay());

        if (dayAlreadyExists) {
            throw new BadRequestException("An entry for " + request.getDay() + " already exists.");
        }

        timesheet.addEntry(entry);

        auditLogEntryRepository.save(
                new AuditLogEntry(
                        AuditActionType.ENTRY_ADDED,
                        timesheet.getConsultantId(),
                        "Entry added for " + request.getDay() + " with " + request.getHours() + " hours.",
                        timesheet
                )
        );

        return timesheetRepository.save(timesheet);
    }

    public Timesheet submitTimesheet(UUID timesheetId) {
        Timesheet timesheet = getTimesheetById(timesheetId);
        timesheet.submit();

        auditLogEntryRepository.save(
                new AuditLogEntry(
                        AuditActionType.SUBMITTED,
                        timesheet.getConsultantId(),
                        "Timesheet submitted for approval.",
                        timesheet
                )
        );

        return timesheetRepository.save(timesheet);
    }

    public Timesheet approveTimesheet(UUID timesheetId, ApprovalRequest request) {
        Timesheet timesheet = getTimesheetById(timesheetId);

        if (!timesheet.getManagerId().equals(request.getManagerId())) {
            throw new BadRequestException("Only the assigned manager can approve this timesheet.");
        }

        timesheet.approve();

        ApprovalDecision decision = new ApprovalDecision(
                DecisionType.APPROVED,
                request.getManagerId(),
                request.getComment(),
                timesheet
        );

        timesheet.setApprovalDecision(decision);
        approvalDecisionRepository.save(decision);

        auditLogEntryRepository.save(
                new AuditLogEntry(
                        AuditActionType.APPROVED,
                        request.getManagerId(),
                        "Timesheet approved.",
                        timesheet
                )
        );

        return timesheetRepository.save(timesheet);
    }

    public Timesheet rejectTimesheet(UUID timesheetId, ApprovalRequest request) {
        Timesheet timesheet = getTimesheetById(timesheetId);

        if (!timesheet.getManagerId().equals(request.getManagerId())) {
            throw new BadRequestException("Only the assigned manager can reject this timesheet.");
        }

        if (request.getComment() == null || request.getComment().isBlank()) {
            throw new BadRequestException("A rejection comment is required.");
        }

        timesheet.reject();

        ApprovalDecision decision = new ApprovalDecision(
                DecisionType.REJECTED,
                request.getManagerId(),
                request.getComment(),
                timesheet
        );

        timesheet.setApprovalDecision(decision);
        approvalDecisionRepository.save(decision);

        auditLogEntryRepository.save(
                new AuditLogEntry(
                        AuditActionType.REJECTED,
                        request.getManagerId(),
                        "Timesheet rejected: " + request.getComment(),
                        timesheet
                )
        );

        return timesheetRepository.save(timesheet);
    }
}