package com.group33.timesheet.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import com.group33.timesheet.domain.TimesheetStatus;

public class FinanceTimesheetResponse {

    private UUID id;
    private String consultantId;
    private String managerId;
    private LocalDate weekStart;
    private LocalDate weekEnd;
    private TimesheetStatus status;
    private BigDecimal totalHours;

    public FinanceTimesheetResponse() {
    }

    public FinanceTimesheetResponse(UUID id,
                                    String consultantId,
                                    String managerId,
                                    LocalDate weekStart,
                                    LocalDate weekEnd,
                                    TimesheetStatus status,
                                    BigDecimal totalHours) {
        this.id = id;
        this.consultantId = consultantId;
        this.managerId = managerId;
        this.weekStart = weekStart;
        this.weekEnd = weekEnd;
        this.status = status;
        this.totalHours = totalHours;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getConsultantId() {
        return consultantId;
    }

    public void setConsultantId(String consultantId) {
        this.consultantId = consultantId;
    }

    public String getManagerId() {
        return managerId;
    }

    public void setManagerId(String managerId) {
        this.managerId = managerId;
    }

    public LocalDate getWeekStart() {
        return weekStart;
    }

    public void setWeekStart(LocalDate weekStart) {
        this.weekStart = weekStart;
    }

    public LocalDate getWeekEnd() {
        return weekEnd;
    }

    public void setWeekEnd(LocalDate weekEnd) {
        this.weekEnd = weekEnd;
    }

    public TimesheetStatus getStatus() {
        return status;
    }

    public void setStatus(TimesheetStatus status) {
        this.status = status;
    }

    public BigDecimal getTotalHours() {
        return totalHours;
    }

    public void setTotalHours(BigDecimal totalHours) {
        this.totalHours = totalHours;
    }
}