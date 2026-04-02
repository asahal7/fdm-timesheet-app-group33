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
}