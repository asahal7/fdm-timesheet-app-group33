// App.tsx – replace entire file content
import { useEffect, useState } from 'react';
import {
  AppBar,
  Box,
  Button,
  Container,
  TextField,
  Toolbar,
  Typography,
  Paper,
  Grid,
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
      setUserIdError('Please enter your employee ID to continue.');
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
    const roleCards: { role: UserRole; title: string; description: string}[] = [
      { role: 'CONSULTANT', title: 'Consultant', description: 'Submit and manage your weekly timesheets'},
      { role: 'MANAGER', title: 'Line Manager', description: 'Review and approve teams timesheets'},
      { role: 'FINANCE', title: 'Finance Team', description: 'See all submitted timesheets'},
    ];

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
          position: 'relative',
          '&::before': {
            content: '""',
            position: 'absolute',
            top: 0,
            left: 0,
            width: '100%',
            height: '100%',
            background: 'radial-gradient(circle at 20% 30%, rgba(197,255,0,0.08) 0%, rgba(0,0,0,0) 70%)',
            pointerEvents: 'none',
          },
        }}
      >
        <Box sx={{ mb: 5, textAlign: 'center', zIndex: 1 }}>
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
              boxShadow: '0 8px 20px rgba(197,255,0,0.2)',
            }}
          >
            <FdmLogo height={40} variant="dark" />
          </Box>
          <Typography
            variant="h4"
            sx={{ color: 'text.primary', mb: 1, fontWeight: 600 }}
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
            maxWidth: 900,
            mx: 'auto',
            zIndex: 1,
          }}
        >
          <Grid container spacing={3} sx={{ mb: 4 }}>
            {roleCards.map((card) => (
              <Grid size={{ xs: 12, sm: 4 }} key={card.role}>
                <Paper
                  elevation={0}
                  onClick={() => setPendingRole(card.role)}
                  sx={{
                    p: 3,
                    height: '100%',
                    cursor: 'pointer',
                    transition: 'all 0.2s ease-in-out',
                    bgcolor: pendingRole === card.role ? 'rgba(197,255,0,0.08)' : 'background.paper',
                    border: pendingRole === card.role ? '1px solid #C5FF00' : '1px solid #3A3A3A',
                    borderRadius: 3,
                    '&:hover': {
                      transform: 'translateY(-4px)',
                      borderColor: '#C5FF00',
                      bgcolor: 'rgba(197,255,0,0.04)',
                      boxShadow: '0 12px 24px rgba(0,0,0,0.3)',
                    },
                  }}
                >
                  <Typography variant="h2" sx={{ fontSize: '2.5rem', mb: 1 }}>
                  </Typography>
                  <Typography variant="h6" sx={{ fontWeight: 700, mb: 1, color: pendingRole === card.role ? '#C5FF00' : 'text.primary' }}>
                    {card.title}
                  </Typography>
                  <Typography variant="body2" color="text.secondary">
                    {card.description}
                  </Typography>
                </Paper>
              </Grid>
            ))}
          </Grid>

          {pendingRole && (
            <Paper
              sx={{
                p: 3,
                bgcolor: 'background.paper',
                borderRadius: 3,
                border: '1px solid #3A3A3A',
                transition: 'all 0.2s',
              }}
            >
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
                InputProps={{
                  sx: { borderRadius: 2 },
                }}
              />
              <Button
                variant="contained"
                fullWidth
                size="large"
                onClick={handleConfirmRole}
                sx={{
                  fontWeight: 700,
                  bgcolor: '#C5FF00',
                  color: '#1E1E1E',
                  '&:hover': { bgcolor: '#b0e000' },
                }}
              >
                Continue
              </Button>
            </Paper>
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
          backdropFilter: 'blur(8px)',
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
              sx={{ borderColor: '#3A3A3A', color: 'text.secondary', '&:hover': { borderColor: '#C5FF00', color: '#C5FF00' } }}
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