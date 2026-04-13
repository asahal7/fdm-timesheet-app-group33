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

const statusLabel: Record<TimesheetStatus, string> = {
  DRAFT: 'Draft',
  PENDING_APPROVAL: 'Pending Approval',
  APPROVED: 'Approved',
  REJECTED: 'Rejected',
};

const ALL = 'ALL';

export default function TimesheetList({ timesheets, onSelect }: Props) {
  const [filter, setFilter] = useState<TimesheetStatus | 'ALL'>(ALL);

  const filtered =
    filter === ALL
      ? timesheets
      : timesheets.filter((ts) => ts.status === filter);

   return (
    <Paper
      elevation={0}
      sx={{
        bgcolor: 'transparent',
        borderRadius: 3,
        overflow: 'hidden',
      }}
    >
      <Box
        sx={{
          p: 2,
          mb: 2,
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
          flexWrap: 'wrap',
          gap: 2,
          borderBottom: '2px solid rgba(197,255,0,0.2)',
        }}
      >
        <Box>
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
            Timesheets
          </Typography>
          <Typography variant="h6" sx={{ fontWeight: 700 }}>
            {filtered.length} {filter === ALL ? 'Total' : statusLabel[filter as TimesheetStatus]}
          </Typography>
        </Box>
        <FormControl size="small" sx={{ minWidth: 180 }}>
          <InputLabel>Filter by status</InputLabel>
          <Select
            value={filter}
            label="Filter by status"
            onChange={(e) => setFilter(e.target.value as TimesheetStatus | 'ALL')}
            sx={{ borderRadius: 2 }}
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
        <Box sx={{ p: 6, textAlign: 'center', bgcolor: 'background.paper', borderRadius: 3 }}>
          <Typography color="text.secondary" variant="body1">
            No timesheets found.
          </Typography>
        </Box>
      ) : (
        <List disablePadding sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
          {filtered.map((ts) => (
            <ListItem
              key={ts.id}
              disablePadding
              sx={{
                bgcolor: 'background.paper',
                borderRadius: 2,
                transition: 'all 0.2s',
                '&:hover': {
                  transform: 'translateX(4px)',
                  bgcolor: 'rgba(197,255,0,0.04)',
                  boxShadow: '0 4px 12px rgba(0,0,0,0.3)',
                },
              }}
            >
              <ListItemButton onClick={() => onSelect(ts.id)} sx={{ borderRadius: 2, p: 2 }}>
                <ListItemText
                  primary={
                    <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: 1 }}>
                      <Typography variant="body1" fontWeight={700} sx={{ letterSpacing: '-0.01em' }}>
                        {ts.weekStart} → {ts.weekEnd}
                      </Typography>
                      <Chip
                        label={statusLabel[ts.status]}
                        color={statusColor[ts.status]}
                        size="small"
                        sx={{ fontWeight: 600, textTransform: 'uppercase', fontSize: '0.7rem' }}
                      />
                    </Box>
                  }
                  secondary={
                    <Typography variant="caption" color="text.secondary" sx={{ mt: 0.5, display: 'block' }}>
                      Consultant: {ts.consultantId} &nbsp;|&nbsp; Manager: {ts.managerId}
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