import { createTheme } from '@mui/material/styles';

const theme = createTheme({
  palette: {
    mode: 'dark',
    primary: {
      main: '#C5FF00',
      contrastText: '#1E1E1E',
    },
    secondary: {
      main: '#70D8FF',
      contrastText: '#1E1E1E',
    },
    background: {
      default: '#1E1E1E',
      paper: '#2A2A2A',
    },
    text: {
      primary: '#EFECEA',
      secondary: '#A0A0A0',
    },
    success: {
      main: '#00D600',
      contrastText: '#1E1E1E',
    },
    error: {
      main: '#FF4444',
    },
    warning: {
      main: '#C5FF00',
      contrastText: '#1E1E1E',
    },
    divider: '#3A3A3A',
  },
  typography: {
    fontFamily: '"Inter", "Helvetica Neue", Arial, sans-serif',
    h4: {
      fontWeight: 700,
      letterSpacing: '-0.5px',
    },
    h6: {
      fontWeight: 600,
    },
  },
  shape: {
    borderRadius: 8,
  },
  components: {
    MuiPaper: {
      styleOverrides: {
        root: {
          backgroundImage: 'none',
          border: '1px solid #3A3A3A',
        },
      },
    },
    MuiButton: {
      styleOverrides: {
        root: {
          textTransform: 'none',
          fontWeight: 600,
          borderRadius: 8,
        },
        containedPrimary: {
          color: '#1E1E1E',
          '&:hover': {
            backgroundColor: '#D4FF1A',
          },
        },
      },
    },
    MuiChip: {
      styleOverrides: {
        root: {
          fontWeight: 600,
          fontSize: '0.75rem',
        },
      },
    },
    MuiTextField: {
      styleOverrides: {
        root: {
          '& .MuiOutlinedInput-root': {
            '&:hover fieldset': {
              borderColor: '#C5FF00',
            },
            '&.Mui-focused fieldset': {
              borderColor: '#C5FF00',
            },
          },
          '& label.Mui-focused': {
            color: '#C5FF00',
          },
        },
      },
    },
    MuiSelect: {
      styleOverrides: {
        root: {
          '&.Mui-focused .MuiOutlinedInput-notchedOutline': {
            borderColor: '#C5FF00',
          },
        },
      },
    },
    MuiTableHead: {
      styleOverrides: {
        root: {
          '& .MuiTableCell-root': {
            backgroundColor: '#333333',
            color: '#C5FF00',
            fontWeight: 600,
            fontSize: '0.8rem',
            textTransform: 'uppercase',
            letterSpacing: '0.05em',
          },
        },
      },
    },
    MuiTableRow: {
      styleOverrides: {
        root: {
          '&:hover': {
            backgroundColor: '#333333',
          },
        },
      },
    },
    MuiListItemButton: {
      styleOverrides: {
        root: {
          '&:hover': {
            backgroundColor: '#333333',
          },
        },
      },
    },
    MuiDivider: {
      styleOverrides: {
        root: {
          borderColor: '#3A3A3A',
        },
      },
    },
  },
});

export default theme;
