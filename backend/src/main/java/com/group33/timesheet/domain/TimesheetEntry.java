package com.group33.timesheet.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.util.UUID;

@Entity
@Table(name = "timesheet_entries")
public class TimesheetEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DayOfWeek day;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal hours;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "timesheet_id", nullable = false)
    private Timesheet timesheet;

    public TimesheetEntry() {
    }

    public TimesheetEntry(DayOfWeek day, BigDecimal hours) {
        this.day = day;
        this.hours = hours;
    }

    public boolean isValid() {
        return hours != null
                && hours.compareTo(BigDecimal.ZERO) >= 0
                && hours.compareTo(new BigDecimal("24")) <= 0;
    }

    public UUID getId() {
        return id;
    }

    public DayOfWeek getDay() {
        return day;
    }

    public BigDecimal getHours() {
        return hours;
    }

    public Timesheet getTimesheet() {
        return timesheet;
    }

    public void setTimesheet(Timesheet timesheet) {
        this.timesheet = timesheet;
    }
}