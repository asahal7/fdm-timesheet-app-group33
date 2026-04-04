// This component folder file is a form that lets a consultant add a daily hours entry to a timesheet.
// It is shown inside TimesheetDetail (also needs to be filled out) when the timesheet status is DRAFT.
// Props: timesheetId and onEntryAdded 
// Imports from the files I finished that are needed here:
// import { addEntry } from '../api/timesheetApi'
// import type { DayOfWeek } from '../types/types'

interface Props {
  timesheetId: string;
  onEntryAdded: () => void;
}

export default function AddEntryForm({ timesheetId, onEntryAdded }: Props) {
  return (
    <div>
      <p>Add Entry Form</p>
    </div>
  );
}