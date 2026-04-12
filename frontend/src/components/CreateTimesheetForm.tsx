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
  userId: string;
  onCreated: () => void;
}

export default function CreateTimesheetForm({ userId, onCreated }: Props) {
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
      await createTimesheet({ consultantId: userId, managerId, weekStart, weekEnd }, userId);
      setSuccess('Timesheet created successfully.');
      setManagerId('');
      setWeekStart('');
      setWeekEnd('');
      onCreated();
    } catch (err: any) {
      setError(err.response?.data?.message || 'Timesheet failed to create.');
    }
  };

  return (
    <Paper sx={{ p: 3, mb: 3 }}>
      <Typography
        variant="subtitle2"
        sx={{
          color: '#C5FF00',
          textTransform: 'uppercase',
          letterSpacing: '0.08em',
          fontSize: '0.7rem',
          mb: 0.5,
        }}
      >
        New Timesheet
      </Typography>
      <Typography variant="h6" sx={{ mb: 2 }}>
        Create Weekly Timesheet
      </Typography>
      {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}
      {success && <Alert severity="success" sx={{ mb: 2 }}>{success}</Alert>}
      <Box component="form" onSubmit={handleSubmit}>
        <TextField
          label="Manager ID"
          value={managerId}
          onChange={(e) => setManagerId(e.target.value)}
          fullWidth
          required
          sx={{ mb: 2 }}
        />
        <Box sx={{ display: 'flex', gap: 2, mb: 2 }}>
          <TextField
            label="Week Start"
            type="date"
            value={weekStart}
            onChange={(e) => setWeekStart(e.target.value)}
            fullWidth
            required
            InputLabelProps={{ shrink: true }}
          />
          <TextField
            label="Week End"
            type="date"
            value={weekEnd}
            onChange={(e) => setWeekEnd(e.target.value)}
            fullWidth
            required
            InputLabelProps={{ shrink: true }}
          />
        </Box>
        <Button type="submit" variant="contained" fullWidth size="large">
          Create Timesheet
        </Button>
      </Box>
    </Paper>
  );
}
