import axios from 'axios';
import type {
  AddEntryRequest,
  ApprovalRequest,
  CreateTimesheetRequest,
  FinanceTimesheetResponse,
  TimesheetResponse,
  UserRole,
} from '../types/types';

const api = axios.create({
  baseURL: '',
});

export const setUserRole = (role: UserRole) => {
  api.defaults.headers.common['X-User-Role'] = role;
};

export const createTimesheet = (data: CreateTimesheetRequest, consultantId: string) =>
  api.post<TimesheetResponse>('/timesheets', data, {
    headers: {
      'X-Consultant-Id': consultantId,
    },
  });

export const getAllTimesheets = () =>
  api.get<TimesheetResponse[]>('/timesheets');

export const getTimesheetById = (id: string) =>
  api.get<TimesheetResponse>(`/timesheets/${id}`);

export const addEntry = (id: string, data: AddEntryRequest) =>
  api.post<TimesheetResponse>(`/timesheets/${id}/entries`, data);

export const submitTimesheet = (id: string, consultantId: string, comment?: string) =>
  api.post<TimesheetResponse>(
    `/timesheets/${id}/submit`,
    comment ? { comment } : null,
    { headers: { 'X-Consultant-Id': consultantId } },
  );

export const resubmitTimesheet = (id: string, consultantId: string, comment?: string) =>
  api.post<TimesheetResponse>(
    `/timesheets/${id}/resubmit`,
    comment ? { comment } : null,
    { headers: { 'X-Consultant-Id': consultantId } },
  );

export const approveTimesheet = (id: string, data: ApprovalRequest) =>
  api.post<TimesheetResponse>(`/timesheets/${id}/approve`, data);

export const rejectTimesheet = (id: string, data: ApprovalRequest) =>
  api.post<TimesheetResponse>(`/timesheets/${id}/reject`, data);

export const getFinanceTimesheets = (params?: {
  consultantId?: string;
  managerId?: string;
  fromDate?: string;
  toDate?: string;
}) =>
  api.get<FinanceTimesheetResponse[]>('/timesheets/finance', { params });

export const exportFinanceCsv = (params?: {
  consultantId?: string;
  managerId?: string;
  fromDate?: string;
  toDate?: string;
}) =>
  api.get<string>('/timesheets/finance/export', {
    params,
    responseType: 'blob',
  });