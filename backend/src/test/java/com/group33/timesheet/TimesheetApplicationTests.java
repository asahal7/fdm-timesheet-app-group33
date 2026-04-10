package com.group33.timesheet;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.group33.timesheet.domain.Timesheet;
import com.group33.timesheet.domain.TimesheetStatus;
import com.group33.timesheet.domain.UserRole;
import com.group33.timesheet.dto.AddTimesheetEntryRequest;
import com.group33.timesheet.dto.ApprovalRequest;
import com.group33.timesheet.dto.CreateTimesheetRequest;
import com.group33.timesheet.exception.BadRequestException;
import com.group33.timesheet.service.TimesheetService;

@SpringBootTest
class TimesheetApplicationTests {

    @Autowired
    private TimesheetService timesheetService;

    @Test
    void fullWorkflow_shouldWorkCorrectly() {
        CreateTimesheetRequest createRequest = new CreateTimesheetRequest();
        createRequest.setConsultantId("CONSULTANT_TEST");
        createRequest.setManagerId("MANAGER_TEST");
        createRequest.setWeekStart(LocalDate.of(2026, 4, 1));
        createRequest.setWeekEnd(LocalDate.of(2026, 4, 7));

        Timesheet timesheet = timesheetService.createTimesheet(createRequest, createRequest.getConsultantId());
        assertNotNull(timesheet.getId());

        AddTimesheetEntryRequest entryRequest = new AddTimesheetEntryRequest();
        entryRequest.setDay(DayOfWeek.MONDAY);
        entryRequest.setHours(BigDecimal.valueOf(8));

        timesheet = timesheetService.addEntry(timesheet.getId(), entryRequest, UserRole.CONSULTANT);
        assertEquals(1, timesheet.getEntries().size());

        timesheet = timesheetService.submitTimesheet(timesheet.getId(), timesheet.getConsultantId());
        assertEquals(TimesheetStatus.PENDING_APPROVAL, timesheet.getStatus());

        ApprovalRequest approvalRequest = new ApprovalRequest();
        approvalRequest.setManagerId("MANAGER_TEST");
        approvalRequest.setComment("Looks good");

        timesheet = timesheetService.approveTimesheet(timesheet.getId(), approvalRequest, UserRole.MANAGER);
        assertEquals(TimesheetStatus.APPROVED, timesheet.getStatus());
    }

    @Test
    void createTimesheet_forAnotherConsultant_shouldThrowBadRequestException() {
        CreateTimesheetRequest createRequest = new CreateTimesheetRequest();
        createRequest.setConsultantId("CONSULTANT_VICTIM");
        createRequest.setManagerId("MANAGER_A");
        createRequest.setWeekStart(LocalDate.of(2026, 7, 1));
        createRequest.setWeekEnd(LocalDate.of(2026, 7, 7));

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> timesheetService.createTimesheet(createRequest, "CONSULTANT_ATTACKER")
        );

        assertEquals("You can only create timesheets for yourself.", exception.getMessage());
    }

    @Test
    void submitTimesheet_forAnotherConsultant_shouldThrowBadRequestException() {
        CreateTimesheetRequest createRequest = new CreateTimesheetRequest();
        createRequest.setConsultantId("CONSULTANT_OWNER");
        createRequest.setManagerId("MANAGER_A");
        createRequest.setWeekStart(LocalDate.of(2026, 6, 1));
        createRequest.setWeekEnd(LocalDate.of(2026, 6, 7));

        Timesheet timesheet = timesheetService.createTimesheet(createRequest, createRequest.getConsultantId());

        AddTimesheetEntryRequest entryRequest = new AddTimesheetEntryRequest();
        entryRequest.setDay(DayOfWeek.MONDAY);
        entryRequest.setHours(BigDecimal.valueOf(8));

        timesheetService.addEntry(timesheet.getId(), entryRequest, UserRole.CONSULTANT);

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> timesheetService.submitTimesheet(timesheet.getId(), "CONSULTANT_OTHER")
        );

        assertEquals("You can only submit your own timesheets.", exception.getMessage());
    }

    @Test
    void approveTimesheet_withConsultantRole_shouldThrowBadRequestException() {
        CreateTimesheetRequest createRequest = new CreateTimesheetRequest();
        createRequest.setConsultantId("CONSULTANT_ROLE_CHECK");
        createRequest.setManagerId("MANAGER_ROLE_CHECK");
        createRequest.setWeekStart(LocalDate.of(2026, 6, 8));
        createRequest.setWeekEnd(LocalDate.of(2026, 6, 14));

        Timesheet timesheet = timesheetService.createTimesheet(createRequest, createRequest.getConsultantId());

        AddTimesheetEntryRequest entryRequest = new AddTimesheetEntryRequest();
        entryRequest.setDay(DayOfWeek.MONDAY);
        entryRequest.setHours(BigDecimal.valueOf(8));

        timesheetService.addEntry(timesheet.getId(), entryRequest, UserRole.CONSULTANT);
        timesheetService.submitTimesheet(timesheet.getId(), "CONSULTANT_ROLE_CHECK");

        ApprovalRequest approvalRequest = new ApprovalRequest();
        approvalRequest.setManagerId("MANAGER_ROLE_CHECK");
        approvalRequest.setComment("Trying to self-approve");

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> timesheetService.approveTimesheet(timesheet.getId(), approvalRequest, UserRole.CONSULTANT)
        );

        assertEquals("Access denied. Manager role is required.", exception.getMessage());
    }

    @Test
    void rejectTimesheet_withConsultantRole_shouldThrowBadRequestException() {
        CreateTimesheetRequest createRequest = new CreateTimesheetRequest();
        createRequest.setConsultantId("CONSULTANT_REJECT_ROLE");
        createRequest.setManagerId("MANAGER_REJECT_ROLE");
        createRequest.setWeekStart(LocalDate.of(2026, 6, 15));
        createRequest.setWeekEnd(LocalDate.of(2026, 6, 21));

        Timesheet timesheet = timesheetService.createTimesheet(createRequest, createRequest.getConsultantId());

        AddTimesheetEntryRequest entryRequest = new AddTimesheetEntryRequest();
        entryRequest.setDay(DayOfWeek.MONDAY);
        entryRequest.setHours(BigDecimal.valueOf(8));

        timesheetService.addEntry(timesheet.getId(), entryRequest, UserRole.CONSULTANT);
        timesheetService.submitTimesheet(timesheet.getId(), "CONSULTANT_REJECT_ROLE");

        ApprovalRequest rejectionRequest = new ApprovalRequest();
        rejectionRequest.setManagerId("MANAGER_REJECT_ROLE");
        rejectionRequest.setComment("Trying to self-reject");

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> timesheetService.rejectTimesheet(timesheet.getId(), rejectionRequest, UserRole.CONSULTANT)
        );

        assertEquals("Access denied. Manager role is required.", exception.getMessage());
    }

    @Test
    void approveTimesheet_withWrongManager_shouldThrowBadRequestException() {
        CreateTimesheetRequest createRequest = new CreateTimesheetRequest();
        createRequest.setConsultantId("CONSULTANT_NEGATIVE");
        createRequest.setManagerId("MANAGER_CORRECT");
        createRequest.setWeekStart(LocalDate.of(2026, 4, 8));
        createRequest.setWeekEnd(LocalDate.of(2026, 4, 14));

        Timesheet timesheet = timesheetService.createTimesheet(createRequest, createRequest.getConsultantId());

        AddTimesheetEntryRequest entryRequest = new AddTimesheetEntryRequest();
        entryRequest.setDay(DayOfWeek.TUESDAY);
        entryRequest.setHours(BigDecimal.valueOf(7.5));

        timesheetService.addEntry(timesheet.getId(), entryRequest, UserRole.CONSULTANT);
        timesheetService.submitTimesheet(timesheet.getId(), timesheet.getConsultantId());

        ApprovalRequest wrongApprovalRequest = new ApprovalRequest();
        wrongApprovalRequest.setManagerId("MANAGER_WRONG");
        wrongApprovalRequest.setComment("Trying to approve without permission");

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> timesheetService.approveTimesheet(timesheet.getId(), wrongApprovalRequest, UserRole.MANAGER)
        );

        assertEquals("Only the assigned manager can approve this timesheet.", exception.getMessage());
    }

    @Test
    void submitTimesheet_withNoEntries_shouldThrowBadRequestException() {
        CreateTimesheetRequest createRequest = new CreateTimesheetRequest();
        createRequest.setConsultantId("CONSULTANT_EMPTY");
        createRequest.setManagerId("MANAGER_EMPTY");
        createRequest.setWeekStart(LocalDate.of(2026, 4, 15));
        createRequest.setWeekEnd(LocalDate.of(2026, 4, 21));

        Timesheet timesheet = timesheetService.createTimesheet(createRequest, createRequest.getConsultantId());

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> timesheetService.submitTimesheet(timesheet.getId(), timesheet.getConsultantId())
        );

        assertEquals("Cannot submit a timesheet with no entries.", exception.getMessage());
    }

    @Test
    void approveDraftTimesheet_shouldThrowBadRequestException() {
        CreateTimesheetRequest createRequest = new CreateTimesheetRequest();
        createRequest.setConsultantId("CONSULTANT_DRAFT");
        createRequest.setManagerId("MANAGER_DRAFT");
        createRequest.setWeekStart(LocalDate.of(2026, 4, 22));
        createRequest.setWeekEnd(LocalDate.of(2026, 4, 28));

        Timesheet timesheet = timesheetService.createTimesheet(createRequest, createRequest.getConsultantId());

        ApprovalRequest approvalRequest = new ApprovalRequest();
        approvalRequest.setManagerId("MANAGER_DRAFT");
        approvalRequest.setComment("Approving too early");

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> timesheetService.approveTimesheet(timesheet.getId(), approvalRequest, UserRole.MANAGER)
        );

        assertEquals("Only PENDING_APPROVAL timesheets can be approved.", exception.getMessage());
    }

    @Test
    void rejectDraftTimesheet_shouldThrowBadRequestException() {
        CreateTimesheetRequest createRequest = new CreateTimesheetRequest();
        createRequest.setConsultantId("CONSULTANT_REJECT");
        createRequest.setManagerId("MANAGER_REJECT");
        createRequest.setWeekStart(LocalDate.of(2026, 4, 29));
        createRequest.setWeekEnd(LocalDate.of(2026, 5, 5));

        Timesheet timesheet = timesheetService.createTimesheet(createRequest, createRequest.getConsultantId());

        ApprovalRequest rejectionRequest = new ApprovalRequest();
        rejectionRequest.setManagerId("MANAGER_REJECT");
        rejectionRequest.setComment("Rejecting too early");

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> timesheetService.rejectTimesheet(timesheet.getId(), rejectionRequest, UserRole.MANAGER)
        );

        assertEquals("Only PENDING_APPROVAL timesheets can be rejected.", exception.getMessage());
    }

    @Test
    void addEntry_afterSubmit_shouldThrowBadRequestException() {
        CreateTimesheetRequest createRequest = new CreateTimesheetRequest();
        createRequest.setConsultantId("CONSULTANT_SUBMITTED");
        createRequest.setManagerId("MANAGER_SUBMITTED");
        createRequest.setWeekStart(LocalDate.of(2026, 5, 6));
        createRequest.setWeekEnd(LocalDate.of(2026, 5, 12));

        Timesheet timesheet = timesheetService.createTimesheet(createRequest, createRequest.getConsultantId());

        AddTimesheetEntryRequest firstEntry = new AddTimesheetEntryRequest();
        firstEntry.setDay(DayOfWeek.MONDAY);
        firstEntry.setHours(BigDecimal.valueOf(8));

        timesheetService.addEntry(timesheet.getId(), firstEntry, UserRole.CONSULTANT);
        timesheetService.submitTimesheet(timesheet.getId(), timesheet.getConsultantId());

        AddTimesheetEntryRequest secondEntry = new AddTimesheetEntryRequest();
        secondEntry.setDay(DayOfWeek.TUESDAY);
        secondEntry.setHours(BigDecimal.valueOf(6));

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> timesheetService.addEntry(timesheet.getId(), secondEntry, UserRole.CONSULTANT)
        );

        assertEquals("Entries can only be added while the timesheet is in DRAFT status.", exception.getMessage());
    }

    @Test
    void addEntry_afterApproval_shouldThrowBadRequestException() {
        CreateTimesheetRequest createRequest = new CreateTimesheetRequest();
        createRequest.setConsultantId("CONSULTANT_APPROVED");
        createRequest.setManagerId("MANAGER_APPROVED");
        createRequest.setWeekStart(LocalDate.of(2026, 5, 13));
        createRequest.setWeekEnd(LocalDate.of(2026, 5, 19));

        Timesheet timesheet = timesheetService.createTimesheet(createRequest, createRequest.getConsultantId());

        AddTimesheetEntryRequest firstEntry = new AddTimesheetEntryRequest();
        firstEntry.setDay(DayOfWeek.MONDAY);
        firstEntry.setHours(BigDecimal.valueOf(8));

        timesheetService.addEntry(timesheet.getId(), firstEntry, UserRole.CONSULTANT);
        timesheetService.submitTimesheet(timesheet.getId(), timesheet.getConsultantId());

        ApprovalRequest approvalRequest = new ApprovalRequest();
        approvalRequest.setManagerId("MANAGER_APPROVED");
        approvalRequest.setComment("Approved");

        timesheetService.approveTimesheet(timesheet.getId(), approvalRequest, UserRole.MANAGER);

        AddTimesheetEntryRequest secondEntry = new AddTimesheetEntryRequest();
        secondEntry.setDay(DayOfWeek.TUESDAY);
        secondEntry.setHours(BigDecimal.valueOf(4));

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> timesheetService.addEntry(timesheet.getId(), secondEntry, UserRole.CONSULTANT)
        );

        assertEquals("Entries can only be added while the timesheet is in DRAFT status.", exception.getMessage());
    }
}
