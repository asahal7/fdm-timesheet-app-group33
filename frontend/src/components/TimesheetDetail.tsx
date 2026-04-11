import { useEffect, useState } from 'react';
import {
  Alert,
  Box,
  Button,
  Chip,
  Divider,
  Paper,
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableRow,
  TextField,
  Typography,
} from '@mui/material';
import {
  approveTimesheet,
  getTimesheetById,
  rejectTimesheet,
  resubmitTimesheet,
  submitTimesheet,
} from '../api/timesheetApi';
import type { TimesheetResponse, TimesheetStatus, UserRole } from '../types/types';
import AddEntryForm from './AddEntryForm';

interface Props {
  timesheetId: string;
  userId: string;
  role: UserRole;
  onBack: () => void;
}

const statusColor: Record<
  TimesheetStatus,
  'default' | 'warning' | 'success' | 'error'
> = {
  DRAFT: 'default',
  PENDING_APPROVAL: 'warning',
  APPROVED: 'success',
  REJECTED: 'error',
};

export default function TimesheetDetail({ timesheetId, userId, role, onBack }: Props) {
  const [timesheet, setTimesheet] = useState<TimesheetResponse | null>(null);
  const [managerId, setManagerId] = useState('');
  const [comment, setComment] = useState('');
  const [submitComment, setSubmitComment] = useState('');
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  const load = async () => {
    try {
      const res = await getTimesheetById(timesheetId);
      setTimesheet(res.data);
    } catch {
      setError('Could not load timesheet.');
    }
  };

  useEffect(() => {
    load();
  }, [timesheetId]);

  const handleSubmit = async () => {
    setError('');
    setSuccess('');
    try {
      await submitTimesheet(timesheetId, userId, submitComment || undefined);
      setSuccess('Timesheet submitted for approval.');
      setSubmitComment('');
      load();
    } catch (err: any) {
      setError(err.response?.data?.message || 'Submit failed.');
    }
  };

  const handleResubmit = async () => {
    setError('');
    setSuccess('');
    try {
      await resubmitTimesheet(timesheetId, userId, submitComment || undefined);
      setSuccess('Timesheet resubmitted for approval.');
      setSubmitComment('');
      load();
    } catch (err: any) {
      setError(err.response?.data?.message || 'Resubmit failed.');
    }
  };

  const handleApprove = async () => {
    setError('');
    setSuccess('');
    try {
      await approveTimesheet(timesheetId, { managerId, comment });
      setSuccess('Timesheet approved.');
      load();
    } catch (err: any) {
      setError(err.response?.data?.message || 'Approve failed.');
    }
  };

  const handleReject = async () => {
    setError('');
    setSuccess('');
    try {
      await rejectTimesheet(timesheetId, { managerId, comment });
      setSuccess('Timesheet rejected.');
      load();
    } catch (err: any) {
      setError(err.response?.data?.message || 'Reject failed.');
    }
  };

  if (!timesheet) {
    return <Typography>Loading...</Typography>;
  }

  return (
    <Paper sx={{ p: 3 }}>
      <Button onClick={onBack} sx={{ mb: 2 }}>
        ← Back to list
      </Button>
      {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}
      {success && <Alert severity="success" sx={{ mb: 2 }}>{success}</Alert>}
      <Box
        sx={{
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
          mb: 2,
        }}
      >
        <Typography variant="h6">Timesheet Details</Typography>
        <Chip label={timesheet.status} color={statusColor[timesheet.status]} />
      </Box>
      <Typography>
        <strong>Consultant ID:</strong> {timesheet.consultantId}
      </Typography>
      <Typography>
        <strong>Manager ID:</strong> {timesheet.managerId}
      </Typography>
      <Typography>
        <strong>Week:</strong> {timesheet.weekStart} → {timesheet.weekEnd}
      </Typography>
      {timesheet.submittedAt && (
        <Typography>
          <strong>Submitted at:</strong> {timesheet.submittedAt}
        </Typography>
      )}
      {timesheet.consultantComment && (
        <Typography>
          <strong>Your comment:</strong> {timesheet.consultantComment}
        </Typography>
      )}
      {timesheet.approvalDecision && (
        <Box sx={{ mt: 1, p: 1.5, bgcolor: timesheet.approvalDecision.decision === 'APPROVED' ? 'success.50' : 'error.50', borderRadius: 1 }}>
          <Typography variant="subtitle2">
            {timesheet.approvalDecision.decision === 'APPROVED' ? 'Approved' : 'Rejected'} by {timesheet.approvalDecision.managerId} on {new Date(timesheet.approvalDecision.decidedAt).toLocaleString()}
          </Typography>
          {timesheet.approvalDecision.comment && (
            <Typography variant="body2">
              <strong>Manager comment:</strong> {timesheet.approvalDecision.comment}
            </Typography>
          )}
        </Box>
      )}
      <Divider sx={{ my: 2 }} />
      <Typography variant="subtitle1" gutterBottom>
        Daily Entries
      </Typography>
      {timesheet.entries.length === 0 ? (
        <Typography color="text.secondary">No entries yet.</Typography>
      ) : (
        <Table size="small">
          <TableHead>
            <TableRow>
              <TableCell>Day</TableCell>
              <TableCell>Hours</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {timesheet.entries.map((entry, i) => (
              <TableRow key={i}>
                <TableCell>{entry.day}</TableCell>
                <TableCell>{entry.hours}</TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      )}
      {role === 'CONSULTANT' && (timesheet.status === 'DRAFT' || timesheet.status === 'REJECTED') && !timesheet.locked && (
        <AddEntryForm timesheetId={timesheetId} onEntryAdded={load} />
      )}
      <Divider sx={{ my: 2 }} />
      {role === 'CONSULTANT' && userId === timesheet.consultantId && (timesheet.status === 'DRAFT' || timesheet.status === 'REJECTED') && (
        <Box>
          <TextField
            label="Comment (optional)"
            value={submitComment}
            onChange={(e) => setSubmitComment(e.target.value)}
            fullWidth
            multiline
            rows={2}
            sx={{ mb: 2 }}
          />
          {timesheet.status === 'DRAFT' ? (
            <Button variant="contained" onClick={handleSubmit}>
              Submit for Approval
            </Button>
          ) : (
            <Button variant="contained" color="warning" onClick={handleResubmit}>
              Resubmit for Approval
            </Button>
          )}
        </Box>
      )}
      {timesheet.status === 'PENDING_APPROVAL' && (
        <Box>
          <Typography variant="subtitle1" gutterBottom>
            Manager Action
          </Typography>
          <TextField
            label="Your Manager ID"
            value={managerId}
            onChange={(e) => setManagerId(e.target.value)}
            fullWidth
            sx={{ mb: 2 }}
          />
          <TextField
            label="Comment (required for rejection)"
            value={comment}
            onChange={(e) => setComment(e.target.value)}
            fullWidth
            multiline
            rows={2}
            sx={{ mb: 2 }}
          />
          <Box sx={{ display: 'flex', gap: 2 }}>
            <Button variant="contained" color="success" onClick={handleApprove}>
              Approve
            </Button>
            <Button variant="contained" color="error" onClick={handleReject}>
              Reject
            </Button>
          </Box>
        </Box>
      )}
    </Paper>
  );
}
