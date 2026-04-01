package com.group33.timesheet.repository;

import com.group33.timesheet.domain.TimesheetEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TimesheetEntryRepository extends JpaRepository<TimesheetEntry, UUID> {
}