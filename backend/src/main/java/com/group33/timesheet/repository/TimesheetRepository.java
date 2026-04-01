package com.group33.timesheet.repository;

import com.group33.timesheet.domain.Timesheet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TimesheetRepository extends JpaRepository<Timesheet, UUID> {
    List<Timesheet> findByConsultantId(String consultantId);
    List<Timesheet> findByManagerId(String managerId);
}