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
      await approveTimesheet(timesheetId, { managerId: userId, comment });
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
      await rejectTimesheet(timesheetId, { managerId: userId, comment });
      setSuccess('Timesheet rejected.');
      load();
    } catch (err: any) {
      setError(err.response?.data?.message || 'Reject failed.');
    }
  };

   if (!timesheet) {
    return (
      <Paper sx={{ p: 4, textAlign: 'center', borderRadius: 3 }}>
        <Typography>Loading timesheet...</Typography>
      </Paper>
    );
  }

  return (
    <Paper
      elevation={0}
      sx={{
        p: { xs: 2, sm: 3 },
        borderRadius: 3,
        border: '1px solid #3A3A3A',
        bgcolor: 'background.paper',
      }}
    >
      <Button
        onClick={onBack}
        sx={{ mb: 2, color: '#C5FF00', '&:hover': { bgcolor: 'rgba(197,255,0,0.08)' } }}
      >
        ← Back to list
      </Button>

      {error && <Alert severity="error" sx={{ mb: 2, borderRadius: 2 }}>{error}</Alert>}
      {success && <Alert severity="success" sx={{ mb: 2, borderRadius: 2 }}>{success}</Alert>}

      <Box
        sx={{
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
          flexWrap: 'wrap',
          gap: 1,
          mb: 3,
        }}
      >
        <Typography variant="h6" sx={{ fontWeight: 700 }}>
          Timesheet Details
        </Typography>
        <Chip
          label={timesheet.status}
          color={statusColor[timesheet.status]}
          sx={{ fontWeight: 600, textTransform: 'uppercase' }}
        />
      </Box>

      <Box
        sx={{
          display: 'grid',
          gridTemplateColumns: { xs: '1fr', sm: '1fr 1fr' },
          gap: 2,
          mb: 3,
          p: 2,
          bgcolor: 'rgba(197,255,0,0.04)',
          borderRadius: 2,
        }}
      >
        <Typography variant="body2"><strong>Consultant ID:</strong> {timesheet.consultantId}</Typography>
        <Typography variant="body2"><strong>Manager ID:</strong> {timesheet.managerId}</Typography>
        <Typography variant="body2"><strong>Week:</strong> {timesheet.weekStart} → {timesheet.weekEnd}</Typography>
        {timesheet.submittedAt && (
          <Typography variant="body2"><strong>Submitted at:</strong> {timesheet.submittedAt}</Typography>
        )}
        {timesheet.consultantComment && (
          <Typography variant="body2" sx={{ gridColumn: '1/-1' }}>
            <strong>Your comment:</strong> {timesheet.consultantComment}
          </Typography>
        )}
      </Box>

      {timesheet.approvalDecision && (
        <Box
          sx={{
            mt: 1,
            mb: 3,
            p: 2,
            bgcolor: timesheet.approvalDecision.decision === 'APPROVED' 
             ? 'rgba(76, 175, 80, 0.1)'  
             : 'rgba(244, 67, 54, 0.1)', 
            borderRadius: 2,
           }}
        >
          <Typography variant="subtitle2" sx={{ fontWeight: 600 }}>
            {timesheet.approvalDecision.decision === 'APPROVED' ? 'Approved' : 'Rejected'} by {timesheet.approvalDecision.managerId} on {new Date(timesheet.approvalDecision.decidedAt).toLocaleString()}
          </Typography>
          {timesheet.approvalDecision.comment && (
            <Typography variant="body2" sx={{ mt: 0.5 }}>
              <strong>Manager comment:</strong> {timesheet.approvalDecision.comment}
            </Typography>
          )}
        </Box>
      )}

      <Divider sx={{ my: 2, borderColor: '#3A3A3A' }} />

      <Typography variant="subtitle1" sx={{ fontWeight: 600, mb: 1 }}>
        Daily Entries
      </Typography>
      {timesheet.entries.length === 0 ? (
        <Typography color="text.secondary" sx={{ p: 2, bgcolor: 'rgba(0,0,0,0.2)', borderRadius: 2, textAlign: 'center' }}>
          No entries yet.
        </Typography>
      ) : (
        <Table size="small" sx={{ mb: 2 }}>
          <TableHead>
            <TableRow sx={{ '& th': { fontWeight: 600, borderBottom: '1px solid #3A3A3A' } }}>
              <TableCell>Day</TableCell>
              <TableCell>Hours</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {timesheet.entries.map((entry, i) => (
              <TableRow key={i} sx={{ '& td': { borderBottom: '1px solid #3A3A3A' } }}>
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

      <Divider sx={{ my: 2, borderColor: '#3A3A3A' }} />

      {role === 'CONSULTANT' && userId === timesheet.consultantId && (timesheet.status === 'DRAFT' || timesheet.status === 'REJECTED') && (
        <Box sx={{ mt: 2 }}>
          <TextField
            label="Comment (optional)"
            value={submitComment}
            onChange={(e) => setSubmitComment(e.target.value)}
            fullWidth
            multiline
            rows={2}
            sx={{ mb: 2, '& .MuiOutlinedInput-root': { borderRadius: 2 } }}
          />
          <Button
            variant="contained"
            onClick={timesheet.status === 'DRAFT' ? handleSubmit : handleResubmit}
            sx={{
              fontWeight: 700,
              bgcolor: '#C5FF00',
              color: '#1E1E1E',
              borderRadius: 2,
              '&:hover': { bgcolor: '#b0e000' },
            }}
          >
            {timesheet.status === 'DRAFT' ? 'Submit for Approval' : 'Resubmit for Approval'}
          </Button>
        </Box>
      )}

      {timesheet.status === 'PENDING_APPROVAL' && (
        <Box sx={{ mt: 2 }}>
          <Typography variant="subtitle1" sx={{ fontWeight: 600, mb: 1 }}>
            Manager Action
          </Typography>
          <TextField
            label="Comment (required for rejection)"
            value={comment}
            onChange={(e) => setComment(e.target.value)}
            fullWidth
            multiline
            rows={2}
            sx={{ mb: 2, '& .MuiOutlinedInput-root': { borderRadius: 2 } }}
          />
          <Box sx={{ display: 'flex', gap: 2 }}>
            <Button
              variant="contained"
              color="success"
              onClick={handleApprove}
              sx={{ borderRadius: 2, fontWeight: 600 }}
            >
              Approve
            </Button>
            <Button
              variant="contained"
              color="error"
              onClick={handleReject}
              sx={{ borderRadius: 2, fontWeight: 600 }}
            >
              Reject
            </Button>
          </Box>
        </Box>
      )}
    </Paper>
  );
}
