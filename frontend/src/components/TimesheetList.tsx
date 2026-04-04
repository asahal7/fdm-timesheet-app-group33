import {
  Chip,
  List,
  ListItem,
  ListItemButton,
  ListItemText,
  Paper,
  Typography,
} from '@mui/material';
import type { TimesheetResponse, TimesheetStatus } from '../types/types';

interface Props {
  timesheets: TimesheetResponse[];
  onSelect: (id: string) => void;
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

export default function TimesheetList({ timesheets, onSelect }: Props) {
  if (timesheets.length === 0) {
    return (
      <Paper sx={{ p: 3 }}>
        <Typography color="text.secondary">No timesheets found.</Typography>
      </Paper>
    );
  }

  return (
    <Paper sx={{ mb: 4 }}>
      <Typography variant="h6" sx={{ p: 2, borderBottom: '1px solid #e0e0e0' }}>
        All Timesheets
      </Typography>
      <List disablePadding>
        {timesheets.map((ts) => (
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
    </Paper>
  );
}
