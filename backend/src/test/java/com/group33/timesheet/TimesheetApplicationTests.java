package com.group33.timesheet;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.UUID;

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
import com.group33.timesheet.exception.ResourceNotFoundException;
import com.group33.timesheet.service.TimesheetService;

@SpringBootTest
class TimesheetApplicationTests {

    @Autowired
    private TimesheetService timesheetService;

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private Timesheet createTimesheet(String consultantId, String managerId, LocalDate start, LocalDate end) {
        CreateTimesheetRequest req = new CreateTimesheetRequest();
        req.setConsultantId(consultantId);
        req.setManagerId(managerId);
        req.setWeekStart(start);
        req.setWeekEnd(end);
        return timesheetService.createTimesheet(req, consultantId);
    }

    private AddTimesheetEntryRequest entryRequest(DayOfWeek day, double hours) {
        AddTimesheetEntryRequest req = new AddTimesheetEntryRequest();
        req.setDay(day);
        req.setHours(BigDecimal.valueOf(hours));
        return req;
    }

    // -------------------------------------------------------------------------
    // Happy-path workflow
    // -------------------------------------------------------------------------

    @Test
    void fullWorkflow_shouldWorkCorrectly() {
        Timesheet timesheet = createTimesheet("CONSULTANT_TEST", "MANAGER_TEST",
                LocalDate.of(2026, 4, 1), LocalDate.of(2026, 4, 7));
        assertNotNull(timesheet.getId());

        timesheet = timesheetService.addEntry(timesheet.getId(),
                entryRequest(DayOfWeek.MONDAY, 8), UserRole.CONSULTANT, "CONSULTANT_TEST");
        assertEquals(1, timesheet.getEntries().size());

        timesheet = timesheetService.submitTimesheet(timesheet.getId(), "CONSULTANT_TEST", null);
        assertEquals(TimesheetStatus.PENDING_APPROVAL, timesheet.getStatus());

        ApprovalRequest approval = new ApprovalRequest();
        approval.setManagerId("MANAGER_TEST");
        approval.setComment("Looks good");

        timesheet = timesheetService.approveTimesheet(timesheet.getId(), approval, UserRole.MANAGER);
        assertEquals(TimesheetStatus.APPROVED, timesheet.getStatus());
    }

    @Test
    void fullRejectResubmitWorkflow_shouldWorkCorrectly() {
        Timesheet timesheet = createTimesheet("CONSULTANT_RESUBMIT", "MANAGER_RESUBMIT",
                LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 7));

        timesheet = timesheetService.addEntry(timesheet.getId(),
                entryRequest(DayOfWeek.WEDNESDAY, 7), UserRole.CONSULTANT, "CONSULTANT_RESUBMIT");

        timesheet = timesheetService.submitTimesheet(timesheet.getId(), "CONSULTANT_RESUBMIT", null);
        assertEquals(TimesheetStatus.PENDING_APPROVAL, timesheet.getStatus());

        ApprovalRequest rejection = new ApprovalRequest();
        rejection.setManagerId("MANAGER_RESUBMIT");
        rejection.setComment("Wrong hours");
        timesheet = timesheetService.rejectTimesheet(timesheet.getId(), rejection, UserRole.MANAGER);
        assertEquals(TimesheetStatus.REJECTED, timesheet.getStatus());

        // Consultant removes wrong entry and adds a corrected one
        UUID entryId = timesheet.getEntries().get(0).getId();
        timesheet = timesheetService.removeEntry(timesheet.getId(), entryId, UserRole.CONSULTANT, "CONSULTANT_RESUBMIT");
        assertEquals(0, timesheet.getEntries().size());

        timesheet = timesheetService.addEntry(timesheet.getId(),
                entryRequest(DayOfWeek.WEDNESDAY, 8), UserRole.CONSULTANT, "CONSULTANT_RESUBMIT");
        assertEquals(1, timesheet.getEntries().size());

        timesheet = timesheetService.resubmitTimesheet(timesheet.getId(), "CONSULTANT_RESUBMIT", null);
        assertEquals(TimesheetStatus.PENDING_APPROVAL, timesheet.getStatus());
    }

    // -------------------------------------------------------------------------
    // removeEntry
    // -------------------------------------------------------------------------

    @Test
    void removeEntry_shouldRemoveEntrySuccessfully() {
        Timesheet timesheet = createTimesheet("CONSULTANT_REMOVE", "MANAGER_REMOVE",
                LocalDate.of(2026, 9, 8), LocalDate.of(2026, 9, 14));

        timesheet = timesheetService.addEntry(timesheet.getId(),
                entryRequest(DayOfWeek.MONDAY, 8), UserRole.CONSULTANT, "CONSULTANT_REMOVE");
        assertEquals(1, timesheet.getEntries().size());

        UUID entryId = timesheet.getEntries().get(0).getId();
        timesheet = timesheetService.removeEntry(timesheet.getId(), entryId, UserRole.CONSULTANT, "CONSULTANT_REMOVE");
        assertEquals(0, timesheet.getEntries().size());
    }

    @Test
    void removeEntry_forAnotherConsultant_shouldThrowBadRequestException() {
        Timesheet timesheet = createTimesheet("CONSULTANT_REMOVE_VICTIM", "MANAGER_R",
                LocalDate.of(2026, 9, 15), LocalDate.of(2026, 9, 21));

        timesheet = timesheetService.addEntry(timesheet.getId(),
                entryRequest(DayOfWeek.TUESDAY, 6), UserRole.CONSULTANT, "CONSULTANT_REMOVE_VICTIM");

        UUID entryId = timesheet.getEntries().get(0).getId();
        UUID timesheetId = timesheet.getId();

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> timesheetService.removeEntry(timesheetId, entryId,
                        UserRole.CONSULTANT, "CONSULTANT_ATTACKER"));

        assertEquals("You can only remove entries from your own timesheets.", ex.getMessage());
    }

    @Test
    void removeEntry_withNonExistentEntryId_shouldThrowResourceNotFoundException() {
        Timesheet timesheet = createTimesheet("CONSULTANT_REMOVE_404", "MANAGER_R2",
                LocalDate.of(2026, 9, 22), LocalDate.of(2026, 9, 28));

        assertThrows(ResourceNotFoundException.class,
                () -> timesheetService.removeEntry(timesheet.getId(), UUID.randomUUID(),
                        UserRole.CONSULTANT, "CONSULTANT_REMOVE_404"));
    }

    @Test
    void removeEntry_afterSubmit_shouldThrowBadRequestException() {
        Timesheet timesheet = createTimesheet("CONSULTANT_REMOVE_SUBMITTED", "MANAGER_RS",
                LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 7));

        timesheet = timesheetService.addEntry(timesheet.getId(),
                entryRequest(DayOfWeek.MONDAY, 8), UserRole.CONSULTANT, "CONSULTANT_REMOVE_SUBMITTED");
        timesheetService.submitTimesheet(timesheet.getId(), "CONSULTANT_REMOVE_SUBMITTED", null);

        UUID entryId = timesheet.getEntries().get(0).getId();
        UUID timesheetId = timesheet.getId();

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> timesheetService.removeEntry(timesheetId, entryId,
                        UserRole.CONSULTANT, "CONSULTANT_REMOVE_SUBMITTED"));

        assertEquals("Entries can only be removed while the timesheet is in DRAFT or REJECTED status.", ex.getMessage());
    }

    // -------------------------------------------------------------------------
    // addEntry ownership and null validation
    // -------------------------------------------------------------------------

    @Test
    void addEntry_forAnotherConsultant_shouldThrowBadRequestException() {
        Timesheet timesheet = createTimesheet("CONSULTANT_ADD_VICTIM", "MANAGER_AV",
                LocalDate.of(2026, 10, 8), LocalDate.of(2026, 10, 14));

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> timesheetService.addEntry(timesheet.getId(),
                        entryRequest(DayOfWeek.MONDAY, 8), UserRole.CONSULTANT, "CONSULTANT_ATTACKER"));

        assertEquals("You can only add entries to your own timesheets.", ex.getMessage());
    }

    @Test
    void addEntry_withNullDay_shouldThrowBadRequestException() {
        Timesheet timesheet = createTimesheet("CONSULTANT_NULL_DAY", "MANAGER_ND",
                LocalDate.of(2026, 10, 15), LocalDate.of(2026, 10, 21));

        AddTimesheetEntryRequest req = new AddTimesheetEntryRequest();
        req.setDay(null);
        req.setHours(BigDecimal.valueOf(8));

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> timesheetService.addEntry(timesheet.getId(), req, UserRole.CONSULTANT, "CONSULTANT_NULL_DAY"));

        assertEquals("Day is required.", ex.getMessage());
    }

    @Test
    void addEntry_withNullHours_shouldThrowBadRequestException() {
        Timesheet timesheet = createTimesheet("CONSULTANT_NULL_HRS", "MANAGER_NH",
                LocalDate.of(2026, 10, 22), LocalDate.of(2026, 10, 28));

        AddTimesheetEntryRequest req = new AddTimesheetEntryRequest();
        req.setDay(DayOfWeek.MONDAY);
        req.setHours(null);

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> timesheetService.addEntry(timesheet.getId(), req, UserRole.CONSULTANT, "CONSULTANT_NULL_HRS"));

        assertEquals("Hours is required.", ex.getMessage());
    }

    // -------------------------------------------------------------------------
    // Existing tests (updated addEntry signatures)
    // -------------------------------------------------------------------------

    @Test
    void createTimesheet_forAnotherConsultant_shouldThrowBadRequestException() {
        CreateTimesheetRequest req = new CreateTimesheetRequest();
        req.setConsultantId("CONSULTANT_VICTIM");
        req.setManagerId("MANAGER_A");
        req.setWeekStart(LocalDate.of(2026, 7, 1));
        req.setWeekEnd(LocalDate.of(2026, 7, 7));

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> timesheetService.createTimesheet(req, "CONSULTANT_ATTACKER"));

        assertEquals("You can only create timesheets for yourself.", ex.getMessage());
    }

    @Test
    void submitTimesheet_forAnotherConsultant_shouldThrowBadRequestException() {
        Timesheet timesheet = createTimesheet("CONSULTANT_OWNER", "MANAGER_A",
                LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 7));

        timesheetService.addEntry(timesheet.getId(),
                entryRequest(DayOfWeek.MONDAY, 8), UserRole.CONSULTANT, "CONSULTANT_OWNER");

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> timesheetService.submitTimesheet(timesheet.getId(), "CONSULTANT_OTHER", null));

        assertEquals("You can only submit your own timesheets.", ex.getMessage());
    }

    @Test
    void approveTimesheet_withConsultantRole_shouldThrowBadRequestException() {
        Timesheet timesheet = createTimesheet("CONSULTANT_ROLE_CHECK", "MANAGER_ROLE_CHECK",
                LocalDate.of(2026, 6, 8), LocalDate.of(2026, 6, 14));

        timesheetService.addEntry(timesheet.getId(),
                entryRequest(DayOfWeek.MONDAY, 8), UserRole.CONSULTANT, "CONSULTANT_ROLE_CHECK");
        timesheetService.submitTimesheet(timesheet.getId(), "CONSULTANT_ROLE_CHECK", null);

        ApprovalRequest req = new ApprovalRequest();
        req.setManagerId("MANAGER_ROLE_CHECK");
        req.setComment("Trying to self-approve");

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> timesheetService.approveTimesheet(timesheet.getId(), req, UserRole.CONSULTANT));

        assertEquals("Access denied. Manager role is required.", ex.getMessage());
    }

    @Test
    void rejectTimesheet_withConsultantRole_shouldThrowBadRequestException() {
        Timesheet timesheet = createTimesheet("CONSULTANT_REJECT_ROLE", "MANAGER_REJECT_ROLE",
                LocalDate.of(2026, 6, 15), LocalDate.of(2026, 6, 21));

        timesheetService.addEntry(timesheet.getId(),
                entryRequest(DayOfWeek.MONDAY, 8), UserRole.CONSULTANT, "CONSULTANT_REJECT_ROLE");
        timesheetService.submitTimesheet(timesheet.getId(), "CONSULTANT_REJECT_ROLE", null);

        ApprovalRequest req = new ApprovalRequest();
        req.setManagerId("MANAGER_REJECT_ROLE");
        req.setComment("Trying to self-reject");

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> timesheetService.rejectTimesheet(timesheet.getId(), req, UserRole.CONSULTANT));

        assertEquals("Access denied. Manager role is required.", ex.getMessage());
    }

    @Test
    void approveTimesheet_withWrongManager_shouldThrowBadRequestException() {
        Timesheet timesheet = createTimesheet("CONSULTANT_NEGATIVE", "MANAGER_CORRECT",
                LocalDate.of(2026, 4, 8), LocalDate.of(2026, 4, 14));

        timesheetService.addEntry(timesheet.getId(),
                entryRequest(DayOfWeek.TUESDAY, 7.5), UserRole.CONSULTANT, "CONSULTANT_NEGATIVE");
        timesheetService.submitTimesheet(timesheet.getId(), "CONSULTANT_NEGATIVE", null);

        ApprovalRequest req = new ApprovalRequest();
        req.setManagerId("MANAGER_WRONG");
        req.setComment("Trying to approve without permission");

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> timesheetService.approveTimesheet(timesheet.getId(), req, UserRole.MANAGER));

        assertEquals("Only the assigned manager can approve this timesheet.", ex.getMessage());
    }

    @Test
    void submitTimesheet_withNoEntries_shouldThrowBadRequestException() {
        Timesheet timesheet = createTimesheet("CONSULTANT_EMPTY", "MANAGER_EMPTY",
                LocalDate.of(2026, 4, 15), LocalDate.of(2026, 4, 21));

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> timesheetService.submitTimesheet(timesheet.getId(), "CONSULTANT_EMPTY", null));

        assertEquals("Cannot submit a timesheet with no entries.", ex.getMessage());
    }

    @Test
    void approveDraftTimesheet_shouldThrowBadRequestException() {
        Timesheet timesheet = createTimesheet("CONSULTANT_DRAFT", "MANAGER_DRAFT",
                LocalDate.of(2026, 4, 22), LocalDate.of(2026, 4, 28));

        ApprovalRequest req = new ApprovalRequest();
        req.setManagerId("MANAGER_DRAFT");
        req.setComment("Approving too early");

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> timesheetService.approveTimesheet(timesheet.getId(), req, UserRole.MANAGER));

        assertEquals("Only PENDING_APPROVAL timesheets can be approved.", ex.getMessage());
    }

    @Test
    void rejectDraftTimesheet_shouldThrowBadRequestException() {
        Timesheet timesheet = createTimesheet("CONSULTANT_REJECT", "MANAGER_REJECT",
                LocalDate.of(2026, 4, 29), LocalDate.of(2026, 5, 5));

        ApprovalRequest req = new ApprovalRequest();
        req.setManagerId("MANAGER_REJECT");
        req.setComment("Rejecting too early");

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> timesheetService.rejectTimesheet(timesheet.getId(), req, UserRole.MANAGER));

        assertEquals("Only PENDING_APPROVAL timesheets can be rejected.", ex.getMessage());
    }

    @Test
    void addEntry_afterSubmit_shouldThrowBadRequestException() {
        Timesheet timesheet = createTimesheet("CONSULTANT_SUBMITTED", "MANAGER_SUBMITTED",
                LocalDate.of(2026, 5, 6), LocalDate.of(2026, 5, 12));

        timesheetService.addEntry(timesheet.getId(),
                entryRequest(DayOfWeek.MONDAY, 8), UserRole.CONSULTANT, "CONSULTANT_SUBMITTED");
        timesheetService.submitTimesheet(timesheet.getId(), "CONSULTANT_SUBMITTED", null);

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> timesheetService.addEntry(timesheet.getId(),
                        entryRequest(DayOfWeek.TUESDAY, 6), UserRole.CONSULTANT, "CONSULTANT_SUBMITTED"));

        assertEquals("Entries can only be added while the timesheet is in DRAFT or REJECTED status.", ex.getMessage());
    }

    @Test
    void addEntry_afterApproval_shouldThrowBadRequestException() {
        Timesheet timesheet = createTimesheet("CONSULTANT_APPROVED", "MANAGER_APPROVED",
                LocalDate.of(2026, 5, 13), LocalDate.of(2026, 5, 19));

        timesheetService.addEntry(timesheet.getId(),
                entryRequest(DayOfWeek.MONDAY, 8), UserRole.CONSULTANT, "CONSULTANT_APPROVED");
        timesheetService.submitTimesheet(timesheet.getId(), "CONSULTANT_APPROVED", null);

        ApprovalRequest req = new ApprovalRequest();
        req.setManagerId("MANAGER_APPROVED");
        req.setComment("Approved");
        timesheetService.approveTimesheet(timesheet.getId(), req, UserRole.MANAGER);

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> timesheetService.addEntry(timesheet.getId(),
                        entryRequest(DayOfWeek.TUESDAY, 4), UserRole.CONSULTANT, "CONSULTANT_APPROVED"));

        assertEquals("Entries can only be added while the timesheet is in DRAFT or REJECTED status.", ex.getMessage());
    }
}
