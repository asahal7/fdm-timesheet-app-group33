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
    <Paper
      elevation={0}
      sx={{
        p: 3,
        mb: 4,
        borderRadius: 3,
        border: '1px solid #3A3A3A',
        bgcolor: 'background.paper',
        transition: 'all 0.2s',
        '&:hover': { borderColor: '#C5FF00', boxShadow: '0 8px 20px rgba(0,0,0,0.3)' },
      }}
    >
      <Typography
        variant="subtitle2"
        sx={{
          color: '#C5FF00',
          textTransform: 'uppercase',
          letterSpacing: '0.1em',
          fontSize: '0.7rem',
          fontWeight: 600,
          mb: 0.5,
        }}
      >
        New Timesheet
      </Typography>
      <Typography variant="h6" sx={{ mb: 2, fontWeight: 700 }}>
        Create Weekly Timesheet
      </Typography>

      {error && <Alert severity="error" sx={{ mb: 2, borderRadius: 2 }}>{error}</Alert>}
      {success && <Alert severity="success" sx={{ mb: 2, borderRadius: 2 }}>{success}</Alert>}

      <Box component="form" onSubmit={handleSubmit}>
        <TextField
          label="Manager ID"
          value={managerId}
          onChange={(e) => setManagerId(e.target.value)}
          fullWidth
          required
          sx={{ mb: 2, '& .MuiOutlinedInput-root': { borderRadius: 2 } }}
        />
        <Box sx={{ display: 'flex', gap: 2, mb: 2, flexWrap: 'wrap' }}>
          <TextField
            label="Week Start"
            type="date"
            value={weekStart}
            onChange={(e) => setWeekStart(e.target.value)}
            fullWidth
            required
            InputLabelProps={{ shrink: true }}
            sx={{ flex: 1, '& .MuiOutlinedInput-root': { borderRadius: 2 } }}
          />
          <TextField
            label="Week End"
            type="date"
            value={weekEnd}
            onChange={(e) => setWeekEnd(e.target.value)}
            fullWidth
            required
            InputLabelProps={{ shrink: true }}
            sx={{ flex: 1, '& .MuiOutlinedInput-root': { borderRadius: 2 } }}
          />
        </Box>
        <Button
          type="submit"
          variant="contained"
          fullWidth
          size="large"
          sx={{
            fontWeight: 700,
            bgcolor: '#C5FF00',
            color: '#1E1E1E',
            borderRadius: 2,
            '&:hover': { bgcolor: '#b0e000', transform: 'translateY(-1px)' },
          }}
        >
          Create Timesheet
        </Button>
      </Box>
    </Paper>
  );
}