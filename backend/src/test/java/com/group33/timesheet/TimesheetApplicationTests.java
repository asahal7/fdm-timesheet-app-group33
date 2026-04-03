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

        Timesheet timesheet = timesheetService.createTimesheet(createRequest);
        assertNotNull(timesheet.getId());

        AddTimesheetEntryRequest entryRequest = new AddTimesheetEntryRequest();
        entryRequest.setDay(DayOfWeek.MONDAY);
        entryRequest.setHours(BigDecimal.valueOf(8));

        timesheet = timesheetService.addEntry(timesheet.getId(), entryRequest);
        assertEquals(1, timesheet.getEntries().size());

        timesheet = timesheetService.submitTimesheet(timesheet.getId());
        assertEquals(TimesheetStatus.PENDING_APPROVAL, timesheet.getStatus());

        ApprovalRequest approvalRequest = new ApprovalRequest();
        approvalRequest.setManagerId("MANAGER_TEST");
        approvalRequest.setComment("Looks good");

        timesheet = timesheetService.approveTimesheet(timesheet.getId(), approvalRequest);
        assertEquals(TimesheetStatus.APPROVED, timesheet.getStatus());
    }

    @Test
    void approveTimesheet_withWrongManager_shouldThrowBadRequestException() {
        CreateTimesheetRequest createRequest = new CreateTimesheetRequest();
        createRequest.setConsultantId("CONSULTANT_NEGATIVE");
        createRequest.setManagerId("MANAGER_CORRECT");
        createRequest.setWeekStart(LocalDate.of(2026, 4, 8));
        createRequest.setWeekEnd(LocalDate.of(2026, 4, 14));

        Timesheet timesheet = timesheetService.createTimesheet(createRequest);

        AddTimesheetEntryRequest entryRequest = new AddTimesheetEntryRequest();
        entryRequest.setDay(DayOfWeek.TUESDAY);
        entryRequest.setHours(BigDecimal.valueOf(7.5));

        timesheetService.addEntry(timesheet.getId(), entryRequest);
        timesheetService.submitTimesheet(timesheet.getId());

        ApprovalRequest wrongApprovalRequest = new ApprovalRequest();
        wrongApprovalRequest.setManagerId("MANAGER_WRONG");
        wrongApprovalRequest.setComment("Trying to approve without permission");

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> timesheetService.approveTimesheet(timesheet.getId(), wrongApprovalRequest)
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

        Timesheet timesheet = timesheetService.createTimesheet(createRequest);

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> timesheetService.submitTimesheet(timesheet.getId())
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

        Timesheet timesheet = timesheetService.createTimesheet(createRequest);

        ApprovalRequest approvalRequest = new ApprovalRequest();
        approvalRequest.setManagerId("MANAGER_DRAFT");
        approvalRequest.setComment("Approving too early");

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> timesheetService.approveTimesheet(timesheet.getId(), approvalRequest)
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

        Timesheet timesheet = timesheetService.createTimesheet(createRequest);

        ApprovalRequest rejectionRequest = new ApprovalRequest();
        rejectionRequest.setManagerId("MANAGER_REJECT");
        rejectionRequest.setComment("Rejecting too early");

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> timesheetService.rejectTimesheet(timesheet.getId(), rejectionRequest)
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

        Timesheet timesheet = timesheetService.createTimesheet(createRequest);

        AddTimesheetEntryRequest firstEntry = new AddTimesheetEntryRequest();
        firstEntry.setDay(DayOfWeek.MONDAY);
        firstEntry.setHours(BigDecimal.valueOf(8));

        timesheetService.addEntry(timesheet.getId(), firstEntry);
        timesheetService.submitTimesheet(timesheet.getId());

        AddTimesheetEntryRequest secondEntry = new AddTimesheetEntryRequest();
        secondEntry.setDay(DayOfWeek.TUESDAY);
        secondEntry.setHours(BigDecimal.valueOf(6));

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> timesheetService.addEntry(timesheet.getId(), secondEntry)
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

        Timesheet timesheet = timesheetService.createTimesheet(createRequest);

        AddTimesheetEntryRequest firstEntry = new AddTimesheetEntryRequest();
        firstEntry.setDay(DayOfWeek.MONDAY);
        firstEntry.setHours(BigDecimal.valueOf(8));

        timesheetService.addEntry(timesheet.getId(), firstEntry);
        timesheetService.submitTimesheet(timesheet.getId());

        ApprovalRequest approvalRequest = new ApprovalRequest();
        approvalRequest.setManagerId("MANAGER_APPROVED");
        approvalRequest.setComment("Approved");

        timesheetService.approveTimesheet(timesheet.getId(), approvalRequest);

        AddTimesheetEntryRequest secondEntry = new AddTimesheetEntryRequest();
        secondEntry.setDay(DayOfWeek.TUESDAY);
        secondEntry.setHours(BigDecimal.valueOf(4));

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> timesheetService.addEntry(timesheet.getId(), secondEntry)
        );

        assertEquals("Entries can only be added while the timesheet is in DRAFT status.", exception.getMessage());
    }
}