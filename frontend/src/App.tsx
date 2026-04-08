import { useEffect, useState } from 'react'
import {
  Box,
  Button,
  ButtonGroup,
  Container,
  Typography,
} from '@mui/material';
import { getAllTimesheets, setUserRole } from './api/timesheetApi';
import type { TimesheetResponse, UserRole } from './types/types';
import CreateTimesheetForm from './components/CreateTimesheetForm';
import TimesheetList from './components/TimesheetList';
import TimesheetDetail from './components/TimesheetDetail';
import FinanceView from './components/FinanceView';

export default function App() {
  const [timesheets, setTimesheets] = useState<TimesheetResponse[]>([]);
  const [selectedId, setSelectedId] = useState<string | null>(null);
  const [role, setRole] = useState<UserRole | null>(null);

  const loadTimesheets = async () => {
    try {
      const res = await getAllTimesheets();
      setTimesheets(res.data);
    } catch {
      setTimesheets([]);
    }
  };

 useEffect(() => {
    if (role && role !== 'FINANCE') {
      setUserRole(role);
      loadTimesheets();
    }
    if (role === 'FINANCE') {
      setUserRole(role);
    }
  }, [role]);

  if (!role) {
    return (
      <Container maxWidth="sm" sx={{ py: 8, textAlign: 'center' }}>
        <Typography variant="h4" gutterBottom>
          FDM Timesheets
        </Typography>
        <Typography variant="body1" sx={{ mb: 4 }} color="text.secondary">
          Select your role to continue
        </Typography>
        <ButtonGroup orientation="vertical" fullWidth>
          <Button
            variant="outlined"
            size="large"
            onClick={() => setRole('CONSULTANT')}
          >
            Consultant
          </Button>
          <Button
            variant="outlined"
            size="large"
            onClick={() => setRole('MANAGER')}
          >
            Line Manager
          </Button>
          <Button
            variant="outlined"
            size="large"
            onClick={() => setRole('FINANCE')}
          >
            Finance Team
          </Button>
        </ButtonGroup>
      </Container>
    );
  }

  if (role === 'FINANCE') {
    return (
      <Container maxWidth="md" sx={{ py: 4 }}>
        <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 3 }}>
          <Typography variant="h4">FDM Timesheets — Finance</Typography>
          <Button variant="outlined" onClick={() => setRole(null)}>
            Switch Role
          </Button>
        </Box>
        <FinanceView />
      </Container>
    );
  }

  return (
    <Container maxWidth="md" sx={{ py: 4 }}>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 3 }}>
        <Typography variant="h4">
          FDM Timesheets —{' '}
          {role === 'CONSULTANT' ? 'Consultant' : 'Line Manager'}
        </Typography>
        <Button variant="outlined" onClick={() => setRole(null)}>
          Switch Role
        </Button>
      </Box>
      {selectedId ? (
        <TimesheetDetail
          timesheetId={selectedId}
          onBack={() => {
            setSelectedId(null);
            loadTimesheets();
          }}
        />
      ) : (
        <>
          {role === 'CONSULTANT' && (
            <CreateTimesheetForm onCreated={loadTimesheets} />
          )}
          <TimesheetList timesheets={timesheets} onSelect={setSelectedId} />
        </>
      )}
    </Container>
  );
}