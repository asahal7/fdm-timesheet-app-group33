export type TimesheetStatus = 'DRAFT' | 'PENDING_APPROVAL' | 'APPROVED' | 'REJECTED';

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

export interface TimesheetResponse {
  id: string;
  consultantId: string;
  managerId: string;
  weekStart: string;
  weekEnd: string;
  status: TimesheetStatus;
  submittedAt: string | null;
  locked: boolean;
  entries: EntryResponse[];
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