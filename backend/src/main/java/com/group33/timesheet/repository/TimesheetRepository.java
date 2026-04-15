package com.group33.timesheet.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.group33.timesheet.domain.Timesheet;
import com.group33.timesheet.domain.TimesheetStatus;

public interface TimesheetRepository extends JpaRepository<Timesheet, UUID> {

    List<Timesheet> findByConsultantId(String consultantId);

    List<Timesheet> findByManagerId(String managerId);

    List<Timesheet> findByStatus(TimesheetStatus status);

    Optional<Timesheet> findByConsultantIdAndWeekStart(String consultantId, LocalDate weekStart);
}