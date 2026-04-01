package com.group33.timesheet.dto;

import java.math.BigDecimal;
import java.time.DayOfWeek;

public class AddTimesheetEntryRequest {

    private DayOfWeek day;
    private BigDecimal hours;

    public AddTimesheetEntryRequest() {
    }

    public DayOfWeek getDay() {
        return day;
    }

    public void setDay(DayOfWeek day) {
        this.day = day;
    }

    public BigDecimal getHours() {
        return hours;
    }

    public void setHours(BigDecimal hours) {
        this.hours = hours;
    }
}