import { useEffect, useState } from 'react'
import { Container, Typography } from '@mui/material';
import { getAllTimesheets } from './api/timesheetApi';
import type { TimesheetResponse } from './types/types';
import CreateTimesheetForm from './components/CreateTimesheetForm';
import TimesheetList from './components/TimesheetList';
import TimesheetDetail from './components/TimesheetDetail';

export default function App() {
  const [timesheets, setTimesheets] = useState<TimesheetResponse[]>([]);
  const [selectedId, setSelectedId] = useState<string | null>(null);

  const loadTimesheets = async () => {
    try {
      const res = await getAllTimesheets();
      setTimesheets(res.data);
    } catch {
      setTimesheets([]);
    }
  };

  useEffect(() => {
    loadTimesheets();
  }, []);

  return (
    <Container maxWidth="md" sx={{ py: 4 }}>
      <Typography variant="h4" gutterBottom>
        FDM Timesheets
      </Typography>
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
          <CreateTimesheetForm onCreated={loadTimesheets} />
          <TimesheetList timesheets={timesheets} onSelect={setSelectedId} />
        </>
      )}
    </Container>
  );
}
