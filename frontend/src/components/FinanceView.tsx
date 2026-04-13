import { useState } from 'react';
import {
  Alert,
  Box,
  Button,
  Paper,
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableRow,
  TextField,
  Typography,
} from '@mui/material';
import { exportFinanceCsv, getFinanceTimesheets } from '../api/timesheetApi';
import type { FinanceTimesheetResponse } from '../types/types';

export default function FinanceView() {
  const [timesheets, setTimesheets] = useState<FinanceTimesheetResponse[]>([]);
  const [consultantId, setConsultantId] = useState('');
  const [managerId, setManagerId] = useState('');
  const [fromDate, setFromDate] = useState('');
  const [toDate, setToDate] = useState('');
  const [error, setError] = useState('');
  const [searched, setSearched] = useState(false);

  const handleSearch = async () => {
    setError('');
    try {
      const res = await getFinanceTimesheets({
        consultantId: consultantId || undefined,
        managerId: managerId || undefined,
        fromDate: fromDate || undefined,
        toDate: toDate || undefined,
      });
      setTimesheets(res.data);
      setSearched(true);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to load timesheets.');
    }
  };

  const handleExport = async () => {
    setError('');
    try {
      const res = await exportFinanceCsv({
        consultantId: consultantId || undefined,
        managerId: managerId || undefined,
        fromDate: fromDate || undefined,
        toDate: toDate || undefined,
      });
      const url = window.URL.createObjectURL(new Blob([res.data]));
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', 'approved-timesheets.csv');
      document.body.appendChild(link);
      link.click();
      link.remove();
    } catch {
      setError('Failed to export CSV.');
    }
  };

 return (
    <Box>
      <Paper
        elevation={0}
        sx={{
          p: 3,
          mb: 3,
          borderRadius: 3,
          border: '1px solid #3A3A3A',
          bgcolor: 'background.paper',
          transition: 'all 0.2s',
        }}
      >
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
          Finance View
        </Typography>
        <Typography variant="h6" sx={{ mb: 2, fontWeight: 700 }}>
          Approved Timesheets
        </Typography>

        {error && <Alert severity="error" sx={{ mb: 2, borderRadius: 2 }}>{error}</Alert>}

        <Box sx={{ display: 'flex', gap: 2, flexWrap: 'wrap', mb: 2 }}>
          <TextField
            label="Consultant ID"
            value={consultantId}
            onChange={(e) => setConsultantId(e.target.value)}
            size="small"
            sx={{ flex: 1, minWidth: 140, '& .MuiOutlinedInput-root': { borderRadius: 2 } }}
          />
          <TextField
            label="Manager ID"
            value={managerId}
            onChange={(e) => setManagerId(e.target.value)}
            size="small"
            sx={{ flex: 1, minWidth: 140, '& .MuiOutlinedInput-root': { borderRadius: 2 } }}
          />
          <TextField
            label="From Date"
            type="date"
            value={fromDate}
            onChange={(e) => setFromDate(e.target.value)}
            InputLabelProps={{ shrink: true }}
            size="small"
            sx={{ flex: 1, minWidth: 140, '& .MuiOutlinedInput-root': { borderRadius: 2 } }}
          />
          <TextField
            label="To Date"
            type="date"
            value={toDate}
            onChange={(e) => setToDate(e.target.value)}
            InputLabelProps={{ shrink: true }}
            size="small"
            sx={{ flex: 1, minWidth: 140, '& .MuiOutlinedInput-root': { borderRadius: 2 } }}
          />
        </Box>

        <Box sx={{ display: 'flex', gap: 2 }}>
          <Button
            variant="contained"
            onClick={handleSearch}
            sx={{
              fontWeight: 700,
              bgcolor: '#C5FF00',
              color: '#1E1E1E',
              borderRadius: 2,
              '&:hover': { bgcolor: '#b0e000' },
            }}
          >
            Search
          </Button>
          <Button
            variant="outlined"
            onClick={handleExport}
            sx={{
              borderColor: '#C5FF00',
              color: '#C5FF00',
              borderRadius: 2,
              '&:hover': { borderColor: '#b0e000', bgcolor: 'rgba(197,255,0,0.08)' },
            }}
          >
            Export CSV
          </Button>
        </Box>
      </Paper>

      {searched && timesheets.length === 0 && (
        <Paper
          sx={{
            p: 4,
            textAlign: 'center',
            borderRadius: 3,
            border: '1px solid #3A3A3A',
            bgcolor: 'background.paper',
          }}
        >
          <Typography color="text.secondary">No approved timesheets found.</Typography>
        </Paper>
      )}

      {timesheets.length > 0 && (
        <Paper
          elevation={0}
          sx={{
            borderRadius: 3,
            border: '1px solid #3A3A3A',
            bgcolor: 'background.paper',
            overflow: 'hidden',
          }}
        >
          <Table sx={{ '& th': { fontWeight: 600, borderBottom: '1px solid #3A3A3A' } }}>
            <TableHead>
              <TableRow>
                <TableCell>Consultant ID</TableCell>
                <TableCell>Manager ID</TableCell>
                <TableCell>Week Start</TableCell>
                <TableCell>Week End</TableCell>
                <TableCell align="right">Total Hours</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {timesheets.map((ts) => (
                <TableRow
                  key={ts.id}
                  sx={{ '& td': { borderBottom: '1px solid #3A3A3A' }, '&:last-child td': { borderBottom: 0 } }}
                >
                  <TableCell>{ts.consultantId}</TableCell>
                  <TableCell>{ts.managerId}</TableCell>
                  <TableCell>{ts.weekStart}</TableCell>
                  <TableCell>{ts.weekEnd}</TableCell>
                  <TableCell align="right" sx={{ color: '#C5FF00', fontWeight: 700 }}>
                    {ts.totalHours}h
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </Paper>
      )}
    </Box>
  );
}
