package com.group33.timesheet.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.group33.timesheet.domain.Timesheet;
import com.group33.timesheet.domain.TimesheetStatus;

public interface TimesheetRepository extends JpaRepository<Timesheet, UUID> {

    List<Timesheet> findByConsultantId(String consultantId);

    List<Timesheet> findByManagerId(String managerId);

    List<Timesheet> findByStatus(TimesheetStatus status);

    @Query("SELECT t FROM Timesheet t WHERE t.consultantId = :consultantId AND t.weekStart <= :weekEnd AND t.weekEnd >= :weekStart")
    List<Timesheet> findOverlappingTimesheets(@Param("consultantId") String consultantId,
                                              @Param("weekStart") LocalDate weekStart,
                                              @Param("weekEnd") LocalDate weekEnd);
}