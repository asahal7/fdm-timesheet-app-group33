package com.group33.timesheet.repository;

import com.group33.timesheet.domain.ApprovalDecision;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ApprovalDecisionRepository extends JpaRepository<ApprovalDecision, UUID> {
}
