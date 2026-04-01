package com.group33.timesheet.dto;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.group33.timesheet.domain.Timesheet;
import com.group33.timesheet.domain.TimesheetEntry;
import com.group33.timesheet.domain.TimesheetStatus;

public class TimesheetResponse {

    private UUID id;
    private String consultantId;
    private String managerId;
    private LocalDate weekStart;
    private LocalDate weekEnd;
    private TimesheetStatus status;
    private LocalDateTime submittedAt;
    private boolean locked;
    private List<EntryResponse> entries;

    public static TimesheetResponse from(Timesheet timesheet) {
        TimesheetResponse response = new TimesheetResponse();
        response.id = timesheet.getId();
        response.consultantId = timesheet.getConsultantId();
        response.managerId = timesheet.getManagerId();
        response.weekStart = timesheet.getWeekStart();
        response.weekEnd = timesheet.getWeekEnd();
        response.status = timesheet.getStatus();
        response.submittedAt = timesheet.getSubmittedAt();
        response.locked = timesheet.isLocked();
        response.entries = timesheet.getEntries()
                .stream()
                .map(EntryResponse::from)
                .toList();
        return response;
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

    public List<EntryResponse> getEntries() {
        return entries;
    }

    public static class EntryResponse {
        private DayOfWeek day;
        private BigDecimal hours;

        public static EntryResponse from(TimesheetEntry entry) {
            EntryResponse response = new EntryResponse();
            response.day = entry.getDay();
            response.hours = entry.getHours();
            return response;
        }

        public DayOfWeek getDay() {
            return day;
        }

        public BigDecimal getHours() {
            return hours;
        }
    }
}