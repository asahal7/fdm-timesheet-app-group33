import { useEffect, useState } from 'react'
import {
  AppBar,
  Box,
  Button,
  ButtonGroup,
  Container,
  TextField,
  Toolbar,
  Typography,
} from '@mui/material';
import { getAllTimesheets, setUserRole } from './api/timesheetApi';
import type { TimesheetResponse, UserRole } from './types/types';
import CreateTimesheetForm from './components/CreateTimesheetForm';
import TimesheetList from './components/TimesheetList';
import TimesheetDetail from './components/TimesheetDetail';
import FinanceView from './components/FinanceView';
import FdmLogo from './components/FdmLogo';

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

  const handleSwitchRole = () => {
    setRole(null);
    setPendingRole(null);
    setUserId('');
    setSelectedId(null);
    setTimesheets([]);
  };


  if (!role) {
    return (
      <Box
        sx={{
          minHeight: '100vh',
          bgcolor: 'background.default',
          display: 'flex',
          flexDirection: 'column',
          alignItems: 'center',
          justifyContent: 'center',
          p: 3,
        }}
      >
        <Box sx={{ mb: 4, textAlign: 'center' }}>
          <Box
            sx={{
              display: 'inline-flex',
              alignItems: 'center',
              justifyContent: 'center',
              bgcolor: '#C5FF00',
              borderRadius: 2,
              px: 3,
              py: 1.5,
              mb: 3,
            }}
          >
            <FdmLogo height={40} variant="dark" />
          </Box>
          <Typography
            variant="h4"
            sx={{ color: 'text.primary', mb: 1 }}
          >
            Timesheet Portal
          </Typography>
          <Typography variant="body1" color="text.secondary">
            Select your role to continue
          </Typography>
        </Box>

        <Box
          sx={{
            width: '100%',
            maxWidth: 400,
            bgcolor: 'background.paper',
            borderRadius: 2,
            border: '1px solid #3A3A3A',
            p: 3,
          }}
        >
          <Typography
            variant="subtitle2"
            color="text.secondary"
            sx={{ mb: 1.5, textTransform: 'uppercase', letterSpacing: '0.08em', fontSize: '0.7rem' }}
          >
            I am a
          </Typography>
          <ButtonGroup orientation="vertical" fullWidth sx={{ mb: 3 }}>
            {(['CONSULTANT', 'MANAGER', 'FINANCE'] as UserRole[]).map((r) => (
              <Button
                key={r}
                variant={pendingRole === r ? 'contained' : 'outlined'}
                size="large"
                onClick={() => setPendingRole(r)}
                sx={{
                  justifyContent: 'flex-start',
                  px: 2.5,
                  borderColor: pendingRole === r ? '#C5FF00' : '#3A3A3A',
                  color: pendingRole === r ? '#1E1E1E' : 'text.primary',
                }}
              >
                {r === 'CONSULTANT' ? 'Consultant' : r === 'MANAGER' ? 'Line Manager' : 'Finance Team'}
              </Button>
            ))}
          </ButtonGroup>

          {pendingRole && (
            <Box>
              <TextField
                label="Employee ID"
                value={userId}
                onChange={(e) => setUserId(e.target.value)}
                onKeyDown={(e) => e.key === 'Enter' && handleConfirmRole()}
                fullWidth
                error={!!userIdError}
                helperText={userIdError}
                sx={{ mb: 2 }}
                autoFocus
              />
              <Button
                variant="contained"
                fullWidth
                size="large"
                onClick={handleConfirmRole}
                sx={{ fontWeight: 700 }}
              >
                Continue
              </Button>
            </Box>
          )}
        </Box>
      </Box>
    );
  }

  const roleLabel =
    role === 'CONSULTANT' ? 'Consultant' :
    role === 'MANAGER' ? 'Line Manager' :
    'Finance Team';

  return (
    <Box sx={{ minHeight: '100vh', bgcolor: 'background.default' }}>
      <AppBar
        position="static"
        elevation={0}
        sx={{
          bgcolor: '#1E1E1E',
          borderBottom: '1px solid #3A3A3A',
        }}
      >
        <Toolbar sx={{ justifyContent: 'space-between' }}>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
            <Box
              sx={{
                bgcolor: '#C5FF00',
                borderRadius: 1,
                px: 1.5,
                py: 0.5,
                display: 'flex',
                alignItems: 'center',
              }}
            >
              <FdmLogo height={24} variant="dark" />
            </Box>
            <Typography
              variant="body1"
              sx={{ color: 'text.secondary', display: { xs: 'none', sm: 'block' } }}
            >
              Timesheet Portal
            </Typography>
          </Box>

          <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
            <Box sx={{ textAlign: 'right', display: { xs: 'none', sm: 'block' } }}>
              <Typography variant="body2" sx={{ color: '#C5FF00', fontWeight: 600, lineHeight: 1.2 }}>
                {userId}
              </Typography>
              <Typography variant="caption" color="text.secondary">
                {roleLabel}
              </Typography>
            </Box>
            <Button
              variant="outlined"
              size="small"
              onClick={handleSwitchRole}
              sx={{ borderColor: '#3A3A3A', color: 'text.secondary' }}
            >
              Switch Role
            </Button>
          </Box>
        </Toolbar>
      </AppBar>

      <Container maxWidth="md" sx={{ py: 4 }}>
        {role === 'FINANCE' ? (
          <FinanceView />
        ) : selectedId ? (
          <TimesheetDetail
            timesheetId={selectedId}
            userId={userId}
            role={role}
            onBack={() => {
              setSelectedId(null);
              loadTimesheets();
            }}
          />
        ) : (
          <>
            {role === 'CONSULTANT' && (
              <CreateTimesheetForm userId={userId} onCreated={loadTimesheets} />
            )}
            <TimesheetList timesheets={timesheets} onSelect={setSelectedId} />
          </>
        )}
      </Container>
    </Box>
  );
}
