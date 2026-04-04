// This component folder file shows the full details of a single timesheet and handles all the actions for it.
// It is shown when a user clicks a timesheet in TimesheetList.
// Props: timesheetId and onBack
// Status chip colours to match my TimesheetList.tsx need to be:
// DRAFT: 'default', PENDING_APPROVAL: 'warning', APPROVED: 'success', REJECTED: 'error'
// Imports from the files I have finished that will be needed here are:
// import { getTimesheetById, submitTimesheet, approveTimesheet, rejectTimesheet } from '../api/timesheetApi'
// import type { TimesheetResponse, TimesheetStatus } from '../types/types'
// import AddEntryForm from './AddEntryForm'

interface Props {
  timesheetId: string;
  onBack: () => void;
}

export default function TimesheetDetail({ onBack }: Props) {
  return (
    <div>
      <button onClick={onBack}>Back</button>
      <p>Timesheet Detail</p>
    </div>
  );
}
