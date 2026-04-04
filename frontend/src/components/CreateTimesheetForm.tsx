import { useState } from 'react';
import {
  Alert,
  Box,
  Button,
  Paper,
  TextField,
  Typography,
} from '@mui/material';
import { createTimesheet } from '../api/timesheetApi';

interface Props {
  onCreated: () => void;
}

export default function CreateTimesheetForm({ onCreated }: Props) {
  const [consultantId, setConsultantId] = useState('');
  const [managerId, setManagerId] = useState('');
  const [weekStart, setWeekStart] = useState('');
  const [weekEnd, setWeekEnd] = useState('');
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setSuccess('');
    try {
      await createTimesheet({ consultantId, managerId, weekStart, weekEnd });
      setSuccess('Timesheet created successfully.');
      setConsultantId('');
      setManagerId('');
      setWeekStart('');
      setWeekEnd('');
      onCreated();
    } catch (err: any) {
      setError(err.response?.data?.message || 'Timesheet failed to create.');
    }
  };

  return (
    <Paper sx={{ p: 3, mb: 4 }}>
      <Typography variant="h6" gutterBottom>
        Create New Timesheet
      </Typography>
      {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}
      {success && <Alert severity="success" sx={{ mb: 2 }}>{success}</Alert>}
      <Box component="form" onSubmit={handleSubmit}>
        <TextField
          label="Consultant ID"
          value={consultantId}
          onChange={(e) => setConsultantId(e.target.value)}
          fullWidth
          required
          sx={{ mb: 2 }}
        />
        <TextField
          label="Manager ID"
          value={managerId}
          onChange={(e) => setManagerId(e.target.value)}
          fullWidth
          required
          sx={{ mb: 2 }}
        />
        <TextField
          label="Week Start"
          type="date"
          value={weekStart}
          onChange={(e) => setWeekStart(e.target.value)}
          fullWidth
          required
          InputLabelProps={{ shrink: true }}
          sx={{ mb: 2 }}
        />
        <TextField
          label="Week End"
          type="date"
          value={weekEnd}
          onChange={(e) => setWeekEnd(e.target.value)}
          fullWidth
          required
          InputLabelProps={{ shrink: true }}
          sx={{ mb: 2 }}
        />
        <Button type="submit" variant="contained" fullWidth>
          Create Timesheet
        </Button>
      </Box>
    </Paper>
  );
}
