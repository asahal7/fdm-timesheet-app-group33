export type TimesheetStatus = 'DRAFT' | 'PENDING_APPROVAL' | 'APPROVED' | 'REJECTED';

export type UserRole = 'CONSULTANT' | 'MANAGER' | 'FINANCE';

export type DayOfWeek =
  | 'MONDAY'
  | 'TUESDAY'
  | 'WEDNESDAY'
  | 'THURSDAY'
  | 'FRIDAY'
  | 'SATURDAY'
  | 'SUNDAY';

export interface EntryResponse {
  day: DayOfWeek;
  hours: number;
}

export interface ApprovalDecisionResponse {
  id: string;
  decision: 'APPROVED' | 'REJECTED';
  decidedAt: string;
  managerId: string;
  comment: string | null;
}

export interface TimesheetResponse {
  id: string;
  consultantId: string;
  managerId: string;
  weekStart: string;
  weekEnd: string;
  status: TimesheetStatus;
  submittedAt: string | null;
  locked: boolean;
  consultantComment: string | null;
  approvalDecision: ApprovalDecisionResponse | null;
  entries: EntryResponse[];
}

export interface FinanceTimesheetResponse {
  id: string;
  consultantId: string;
  managerId: string;
  weekStart: string;
  weekEnd: string;
  status: TimesheetStatus;
  totalHours: number;
}

export interface CreateTimesheetRequest {
  consultantId: string;
  managerId: string;
  weekStart: string;
  weekEnd: string;
}

export interface AddEntryRequest {
  day: DayOfWeek;
  hours: number;
}

export interface ApprovalRequest {
  managerId: string;
  comment: string;
}