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
    <Paper sx={{ mb: 4 }}>
      <Box
        sx={{
          p: 2,
          borderBottom: '1px solid #e0e0e0',
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
        }}
      >
        <Typography variant="h6">All Timesheets</Typography>
        <FormControl size="small" sx={{ minWidth: 180 }}>
          <InputLabel>Filter by status</InputLabel>
          <Select
            value={filter}
            label="Filter by status"
            onChange={(e) =>
              setFilter(e.target.value as TimesheetStatus | 'ALL')
            }
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
        <Typography sx={{ p: 2 }} color="text.secondary">
          No timesheets found.
        </Typography>
      ) : (
        <List disablePadding>
          {filtered.map((ts) => (
            <ListItem
              key={ts.id}
              disablePadding
              divider
              secondaryAction={
                <Chip
                  label={ts.status}
                  color={statusColor[ts.status]}
                  size="small"
                />
              }
            >
              <ListItemButton onClick={() => onSelect(ts.id)}>
                <ListItemText
                  primary={`Consultant: ${ts.consultantId}`}
                  secondary={`${ts.weekStart} → ${ts.weekEnd}   |   Manager: ${ts.managerId}`}
                />
              </ListItemButton>
            </ListItem>
          ))}
        </List>
      )}
    </Paper>
  );
}