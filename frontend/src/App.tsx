import { useEffect, useState } from 'react'
import {
  Box,
  Button,
  ButtonGroup,
  Container,
  TextField,
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
  const [userId, setUserId] = useState('');
  const [pendingRole, setPendingRole] = useState<UserRole | null>(null);
  const [userIdError, setUserIdError] = useState('');

  const loadTimesheets = async () => {
    try {
      const res = await getAllTimesheets();
      setTimesheets(res.data);
    } catch {
      setTimesheets([]);
    }
  };

 useEffect(() => {
    if (role) {
      setUserRole(role);
      if (role !== 'FINANCE') {
        loadTimesheets();
      }
    }
  }, [role]);

  const handleConfirmRole = () => {
    if (!userId.trim()) {
      setUserIdError('Please enter your ID to continue.');
      return;
    }
    setUserIdError('');
    setRole(pendingRole);
  };

  if (!role) {
    return (
      <Container maxWidth="sm" sx={{ py: 8, textAlign: 'center' }}>
        <Typography variant="h4" gutterBottom>
          FDM Timesheets
        </Typography>
        <Typography variant="body1" sx={{ mb: 4 }} color="text.secondary">
          Select your role to continue
        </Typography>
        <ButtonGroup orientation="vertical" fullWidth sx={{ mb: 3 }}>
          <Button
            variant={pendingRole === 'CONSULTANT' ? 'contained' : 'outlined'}
            size="large"
            onClick={() => setPendingRole('CONSULTANT')}
          >
            Consultant
          </Button>
          <Button
            variant={pendingRole === 'MANAGER' ? 'contained' : 'outlined'}
            size="large"
            onClick={() => setPendingRole('MANAGER')}
          >
            Line Manager
          </Button>
          <Button
            variant={pendingRole === 'FINANCE' ? 'contained' : 'outlined'}
            size="large"
            onClick={() => setPendingRole('FINANCE')}
          >
            Finance Team
          </Button>
        </ButtonGroup>
        {pendingRole && (
          <Box>
            <TextField
              label="Enter your Employee ID"
              value={userId}
              onChange={(e) => setUserId(e.target.value)}
              fullWidth
              error={!!userIdError}
              helperText={userIdError}
              sx={{ mb: 2 }}
            />
            <Button
              variant="contained"
              fullWidth
              size="large"
              onClick={handleConfirmRole}
            >
              Continue as {pendingRole}
            </Button>
          </Box>
        )}
      </Container>
    );
  }

  if (role === 'FINANCE') {
    return (
      <Container maxWidth="md" sx={{ py: 4 }}>
        <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 3 }}>
          <Typography variant="h4">FDM Timesheets — Finance</Typography>
          <Button
            variant="outlined"
            onClick={() => {
              setRole(null);
              setPendingRole(null);
              setUserId('');
            }}
          >
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
          {role === 'CONSULTANT' ? 'Consultant' : 'Line Manager'} ({userId})
        </Typography>
        <Button
          variant="outlined"
          onClick={() => {
            setRole(null);
            setPendingRole(null);
            setUserId('');
          }}
        >
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