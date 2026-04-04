import axios from 'axios';
import type {
  AddEntryRequest,
  ApprovalRequest,
  CreateTimesheetRequest,
  TimesheetResponse,
} from '../types/types';

const api = axios.create({
  baseURL: '',
});

export const createTimesheet = (data: CreateTimesheetRequest) =>
  api.post<TimesheetResponse>('/timesheets', data);

export const getAllTimesheets = () =>
  api.get<TimesheetResponse[]>('/timesheets');

export const getTimesheetById = (id: string) =>
  api.get<TimesheetResponse>(`/timesheets/${id}`);

export const addEntry = (id: string, data: AddEntryRequest) =>
  api.post<TimesheetResponse>(`/timesheets/${id}/entries`, data);

export const submitTimesheet = (id: string) =>
  api.post<TimesheetResponse>(`/timesheets/${id}/submit`);

export const approveTimesheet = (id: string, data: ApprovalRequest) =>
  api.post<TimesheetResponse>(`/timesheets/${id}/approve`, data);

export const rejectTimesheet = (id: string, data: ApprovalRequest) =>
  api.post<TimesheetResponse>(`/timesheets/${id}/reject`, data);