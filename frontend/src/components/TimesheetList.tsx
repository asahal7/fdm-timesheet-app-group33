import { useState } from 'react';
import {
  Box,
  Chip,
  FormControl,
  InputLabel,
  List,
  ListItem,
  ListItemButton,
  ListItemText,
  MenuItem,
  Paper,
  Select,
  Typography,
} from '@mui/material';
import type { TimesheetResponse, TimesheetStatus } from '../types/types';

interface Props {
  timesheets: TimesheetResponse[];
  onSelect: (id: string) => void;
}

const statusColor: Record<TimesheetStatus, 'default' | 'warning' | 'success' | 'error'> = {
  DRAFT: 'default',
  PENDING_APPROVAL: 'warning',
  APPROVED: 'success',
  REJECTED: 'error',
};

const ALL = 'ALL';

export default function TimesheetList({ timesheets, onSelect }: Props) {
  const [filter, setFilter] = useState<TimesheetStatus | 'ALL'>(ALL);

  const filtered =
    filter === ALL
      ? timesheets
      : timesheets.filter((ts) => ts.status === filter);

  return (
    <Paper>
      <Box
        sx={{
          p: 2,
          borderBottom: '1px solid #3A3A3A',
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
        }}
      >
        <Box>
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
            Timesheets
          </Typography>
          <Typography variant="h6">
            {filtered.length} {filter === ALL ? 'Total' : statusLabel[filter as TimesheetStatus]}
          </Typography>
        </Box>
        <FormControl size="small" sx={{ minWidth: 180 }}>
          <InputLabel>Filter by status</InputLabel>
          <Select
            value={filter}
            label="Filter by status"
            onChange={(e) => setFilter(e.target.value as TimesheetStatus | 'ALL')}
          >
            <MenuItem value="ALL">All</MenuItem>
            <MenuItem value="DRAFT">Draft</MenuItem>
            <MenuItem value="PENDING_APPROVAL">Pending Approval</MenuItem>
            <MenuItem value="APPROVED">Approved</MenuItem>
            <MenuItem value="REJECTED">Rejected</MenuItem>
          </Select>
        </FormControl>
      </Box>

      {filtered.length === 0 ? (
        <Box sx={{ p: 4, textAlign: 'center' }}>
          <Typography color="text.secondary">No timesheets found.</Typography>
        </Box>
      ) : (
        <List disablePadding>
          {filtered.map((ts) => (
            <ListItem
              key={ts.id}
              disablePadding
              divider
              secondaryAction={
                <Chip
                  label={statusLabel[ts.status]}
                  color={statusColor[ts.status]}
                  size="small"
                />
              }
            >
              <ListItemButton onClick={() => onSelect(ts.id)} sx={{ pr: 14 }}>
                <ListItemText
                  primary={
                    <Typography variant="body2" fontWeight={600}>
                      {ts.weekStart} → {ts.weekEnd}
                    </Typography>
                  }
                  secondary={
                    <Typography variant="caption" color="text.secondary">
                      Consultant: {ts.consultantId} · Manager: {ts.managerId}
                    </Typography>
                  }
                />
              </ListItemButton>
            </ListItem>
          ))}
        </List>
      )}
    </Paper>
  );
}
