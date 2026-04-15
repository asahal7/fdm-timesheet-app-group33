package com.group33.timesheet.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.group33.timesheet.domain.ApprovalDecision;
import com.group33.timesheet.domain.AuditActionType;
import com.group33.timesheet.domain.AuditLogEntry;
import com.group33.timesheet.domain.DecisionType;
import com.group33.timesheet.domain.Timesheet;
import com.group33.timesheet.domain.TimesheetEntry;
import com.group33.timesheet.domain.TimesheetStatus;
import com.group33.timesheet.domain.UserRole;
import com.group33.timesheet.dto.AddTimesheetEntryRequest;
import com.group33.timesheet.dto.ApprovalRequest;
import com.group33.timesheet.dto.CreateTimesheetRequest;
import com.group33.timesheet.dto.SubmitTimesheetRequest;
import com.group33.timesheet.dto.FinanceTimesheetResponse;
import com.group33.timesheet.exception.BadRequestException;
import com.group33.timesheet.exception.ResourceNotFoundException;
import com.group33.timesheet.repository.ApprovalDecisionRepository;
import com.group33.timesheet.repository.AuditLogEntryRepository;
import com.group33.timesheet.repository.TimesheetEntryRepository;
import com.group33.timesheet.repository.TimesheetRepository;

@Service
@Transactional
public class TimesheetService {

    private final TimesheetRepository timesheetRepository;
    private final ApprovalDecisionRepository approvalDecisionRepository;
    private final AuditLogEntryRepository auditLogEntryRepository;
    private final TimesheetEntryRepository timesheetEntryRepository;

    public TimesheetService(TimesheetRepository timesheetRepository,
                            ApprovalDecisionRepository approvalDecisionRepository,
                            AuditLogEntryRepository auditLogEntryRepository,
                            TimesheetEntryRepository timesheetEntryRepository) {
        this.timesheetRepository = timesheetRepository;
        this.approvalDecisionRepository = approvalDecisionRepository;
        this.auditLogEntryRepository = auditLogEntryRepository;
        this.timesheetEntryRepository = timesheetEntryRepository;
    }

    public Timesheet createTimesheet(CreateTimesheetRequest request, String callerConsultantId) {
        if (request.getConsultantId() == null || request.getConsultantId().isBlank()) {
            throw new BadRequestException("consultantId is required.");
        }

        if (!request.getConsultantId().equals(callerConsultantId)) {
            throw new BadRequestException("You can only create timesheets for yourself.");
        }
        if (request.getManagerId() == null || request.getManagerId().isBlank()) {
            throw new BadRequestException("managerId is required.");
        }
        if (request.getWeekStart() == null || request.getWeekEnd() == null) {
            throw new BadRequestException("weekStart and weekEnd are required.");
        }
        if (timesheetRepository.findByConsultantIdAndWeekStart(request.getConsultantId(), request.getWeekStart()).isPresent()) {
            throw new BadRequestException("A timesheet for this week already exists.");
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
    public List<Timesheet> getAllTimesheets(UserRole role, String userId) {
        List<Timesheet> timesheets;
        if (role == UserRole.CONSULTANT) {
            timesheets = timesheetRepository.findByConsultantId(userId);
        } else if (role == UserRole.MANAGER) {
            timesheets = timesheetRepository.findByManagerId(userId);
        } else {
            timesheets = timesheetRepository.findAll();
        }
        return timesheets.stream()
                .sorted(java.util.Comparator.comparing(Timesheet::getWeekStart).reversed())
                .toList();
    }

    @Transactional(readOnly = true)
    public Timesheet getTimesheetById(UUID id) {
        return timesheetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Timesheet not found: " + id));
    }

    public Timesheet addEntry(UUID timesheetId, AddTimesheetEntryRequest request, UserRole callerRole, String callerConsultantId) {
        requireConsultantRole(callerRole);

        if (request.getDay() == null) {
            throw new BadRequestException("Day is required.");
        }
        if (request.getHours() == null) {
            throw new BadRequestException("Hours is required.");
        }

        Timesheet timesheet = getTimesheetById(timesheetId);

        if (!timesheet.getConsultantId().equals(callerConsultantId)) {
            throw new BadRequestException("You can only add entries to your own timesheets.");
        }

        if (timesheet.getStatus() != TimesheetStatus.DRAFT && timesheet.getStatus() != TimesheetStatus.REJECTED) {
            throw new BadRequestException("Entries can only be added while the timesheet is in DRAFT or REJECTED status.");
        }

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

    public Timesheet removeEntry(UUID timesheetId, UUID entryId, UserRole callerRole, String callerConsultantId) {
        requireConsultantRole(callerRole);

        Timesheet timesheet = getTimesheetById(timesheetId);

        if (!timesheet.getConsultantId().equals(callerConsultantId)) {
            throw new BadRequestException("You can only remove entries from your own timesheets.");
        }

        if (timesheet.getStatus() != TimesheetStatus.DRAFT && timesheet.getStatus() != TimesheetStatus.REJECTED) {
            throw new BadRequestException("Entries can only be removed while the timesheet is in DRAFT or REJECTED status.");
        }

        TimesheetEntry entry = timesheetEntryRepository.findById(entryId)
                .orElseThrow(() -> new ResourceNotFoundException("Entry not found: " + entryId));

        if (!entry.getTimesheet().getId().equals(timesheetId)) {
            throw new BadRequestException("Entry does not belong to this timesheet.");
        }

        timesheet.getEntries().remove(entry);
        timesheetEntryRepository.delete(entry);

        auditLogEntryRepository.save(
                new AuditLogEntry(
                        AuditActionType.ENTRY_REMOVED,
                        timesheet.getConsultantId(),
                        "Entry removed for " + entry.getDay() + ".",
                        timesheet
                )
        );

        return timesheetRepository.save(timesheet);
    }

    public Timesheet submitTimesheet(UUID timesheetId, String consultantId, SubmitTimesheetRequest request) {
        Timesheet timesheet = getTimesheetById(timesheetId);

        if (!timesheet.getConsultantId().equals(consultantId)) {
            throw new BadRequestException("You can only submit your own timesheets.");
        }

        if (timesheet.getStatus() != TimesheetStatus.DRAFT) {
            throw new BadRequestException("Only DRAFT timesheets can be submitted.");
        }

        if (timesheet.getEntries() == null || timesheet.getEntries().isEmpty()) {
            throw new BadRequestException("Cannot submit a timesheet with no entries.");
        }

        String comment = request != null ? request.getComment() : null;
        timesheet.setConsultantComment(comment);
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

    public Timesheet resubmitTimesheet(UUID timesheetId, String consultantId, SubmitTimesheetRequest request) {
        Timesheet timesheet = getTimesheetById(timesheetId);

        if (!timesheet.getConsultantId().equals(consultantId)) {
            throw new BadRequestException("You can only resubmit your own timesheets.");
        }

        if (timesheet.getStatus() != TimesheetStatus.REJECTED) {
            throw new BadRequestException("Only REJECTED timesheets can be resubmitted.");
        }

        String comment = request != null ? request.getComment() : null;
        timesheet.setConsultantComment(comment);
        timesheet.setApprovalDecision(null);
        timesheet.resubmit();

        auditLogEntryRepository.save(
                new AuditLogEntry(
                        AuditActionType.RESUBMITTED,
                        timesheet.getConsultantId(),
                        "Timesheet resubmitted for approval.",
                        timesheet
                )
        );

        return timesheetRepository.save(timesheet);
    }

    public Timesheet approveTimesheet(UUID timesheetId, ApprovalRequest request, UserRole callerRole) {
        requireManagerRole(callerRole);

        Timesheet timesheet = getTimesheetById(timesheetId);

        if (!timesheet.getManagerId().equals(request.getManagerId())) {
            throw new BadRequestException("Only the assigned manager can approve this timesheet.");
        }

        if (timesheet.getStatus() != TimesheetStatus.PENDING_APPROVAL) {
            throw new BadRequestException("Only PENDING_APPROVAL timesheets can be approved.");
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

    public Timesheet rejectTimesheet(UUID timesheetId, ApprovalRequest request, UserRole callerRole) {
        requireManagerRole(callerRole);

        Timesheet timesheet = getTimesheetById(timesheetId);

        if (!timesheet.getManagerId().equals(request.getManagerId())) {
            throw new BadRequestException("Only the assigned manager can reject this timesheet.");
        }

        if (timesheet.getStatus() != TimesheetStatus.PENDING_APPROVAL) {
            throw new BadRequestException("Only PENDING_APPROVAL timesheets can be rejected.");
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

    @Transactional(readOnly = true)
    public List<FinanceTimesheetResponse> getApprovedTimesheetsForFinance(
            UserRole userRole,
            String consultantId,
            String managerId,
            LocalDate fromDate,
            LocalDate toDate
    ) {
        requireFinanceRole(userRole);

        return timesheetRepository.findByStatus(TimesheetStatus.APPROVED).stream()
                .filter(timesheet -> consultantId == null || consultantId.isBlank()
                        || consultantId.equals(timesheet.getConsultantId()))
                .filter(timesheet -> managerId == null || managerId.isBlank()
                        || managerId.equals(timesheet.getManagerId()))
                .filter(timesheet -> fromDate == null
                        || !timesheet.getWeekStart().isBefore(fromDate))
                .filter(timesheet -> toDate == null
                        || !timesheet.getWeekEnd().isAfter(toDate))
                .map(this::mapToFinanceResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public String exportApprovedTimesheetsCsv(
            UserRole userRole,
            String consultantId,
            String managerId,
            LocalDate fromDate,
            LocalDate toDate
    ) {
        requireFinanceRole(userRole);

        List<FinanceTimesheetResponse> timesheets = getApprovedTimesheetsForFinance(
                userRole, consultantId, managerId, fromDate, toDate
        );

        StringBuilder csv = new StringBuilder();
        csv.append("Timesheet ID,Consultant ID,Manager ID,Week Start,Week End,Status,Total Hours\n");

        for (FinanceTimesheetResponse timesheet : timesheets) {
            csv.append(timesheet.getId()).append(",")
                    .append(safeCsv(timesheet.getConsultantId())).append(",")
                    .append(safeCsv(timesheet.getManagerId())).append(",")
                    .append(timesheet.getWeekStart()).append(",")
                    .append(timesheet.getWeekEnd()).append(",")
                    .append(timesheet.getStatus()).append(",")
                    .append(timesheet.getTotalHours())
                    .append("\n");
        }

        return csv.toString();
    }

    private void requireConsultantRole(UserRole userRole) {
        if (userRole != UserRole.CONSULTANT) {
            throw new BadRequestException("Access denied. Consultant role is required.");
        }
    }

    private void requireManagerRole(UserRole userRole) {
        if (userRole != UserRole.MANAGER) {
            throw new BadRequestException("Access denied. Manager role is required.");
        }
    }

    private void requireFinanceRole(UserRole userRole) {
        if (userRole != UserRole.FINANCE) {
            throw new BadRequestException("Access denied. Finance role is required.");
        }
    }

    private FinanceTimesheetResponse mapToFinanceResponse(Timesheet timesheet) {
        return new FinanceTimesheetResponse(
                timesheet.getId(),
                timesheet.getConsultantId(),
                timesheet.getManagerId(),
                timesheet.getWeekStart(),
                timesheet.getWeekEnd(),
                timesheet.getStatus(),
                calculateTotalHours(timesheet)
        );
    }

    private BigDecimal calculateTotalHours(Timesheet timesheet) {
        if (timesheet.getEntries() == null || timesheet.getEntries().isEmpty()) {
            return BigDecimal.ZERO;
        }

        return timesheet.getEntries().stream()
                .map(TimesheetEntry::getHours)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private String safeCsv(String value) {
        if (value == null) {
            return "";
        }

        String escaped = value.replace("\"", "\"\"");
        return "\"" + escaped + "\"";
    }
}