import { useState } from 'react';
import {
  Alert,
  Box,
  Button,
  FormControl,
  InputLabel,
  MenuItem,
  Select,
  TextField,
  Typography,
} from '@mui/material';
import { addEntry } from '../api/timesheetApi';
import type { DayOfWeek } from '../types/types';

interface Props {
  timesheetId: string;
  consultantId: string;
  onEntryAdded: () => void;
}

const DAYS: DayOfWeek[] = [
  'MONDAY',
  'TUESDAY',
  'WEDNESDAY',
  'THURSDAY',
  'FRIDAY',
  'SATURDAY',
  'SUNDAY',
];

export default function AddEntryForm({ timesheetId, consultantId, onEntryAdded }: Props) {
  const [day, setDay] = useState<DayOfWeek | ''>('');
  const [hours, setHours] = useState('');
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setSuccess('');
    if (!hours || parseFloat(hours) <= 0) {
      setError('Hours must be greater than 0.');
      return;
    }
    try {
      await addEntry(timesheetId, { day: day as DayOfWeek, hours: parseFloat(hours) }, consultantId);
      setSuccess(`Entry added for ${day}.`);
      setDay('');
      setHours('');
      onEntryAdded();
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to add entry.');
    }
  };

  return (
    <Box sx={{ mt: 3 }}>
      <Typography variant="subtitle1" gutterBottom>
        Add Daily Entry
      </Typography>
      {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}
      {success && <Alert severity="success" sx={{ mb: 2 }}>{success}</Alert>}
      <Box
        component="form"
        onSubmit={handleSubmit}
        sx={{ display: 'flex', gap: 2, flexWrap: 'wrap' }}
      >
        <FormControl required sx={{ minWidth: 160 }}>
          <InputLabel>Day</InputLabel>
          <Select
            value={day}
            label="Day"
            onChange={(e) => setDay(e.target.value as DayOfWeek)}
          >
            {DAYS.map((d) => (
              <MenuItem key={d} value={d}>
                {d}
              </MenuItem>
            ))}
          </Select>
        </FormControl>
        <TextField
          label="Hours"
          type="number"
          inputProps={{ min: 0.5, max: 24, step: 0.5 }}
          value={hours}
          onChange={(e) => setHours(e.target.value)}
          required
          sx={{ width: 120 }}
        />
        <Button type="submit" variant="outlined">
          Add Entry
        </Button>
      </Box>
    </Box>
  );
}
