package com.group33.timesheet.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.group33.timesheet.dto.AddTimesheetEntryRequest;
import com.group33.timesheet.dto.ApprovalRequest;
import com.group33.timesheet.dto.CreateTimesheetRequest;
import com.group33.timesheet.dto.TimesheetResponse;
import com.group33.timesheet.service.TimesheetService;

@RestController
@RequestMapping("/timesheets")
public class TimesheetController {

    private final TimesheetService timesheetService;

    public TimesheetController(TimesheetService timesheetService) {
        this.timesheetService = timesheetService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TimesheetResponse createTimesheet(@RequestBody CreateTimesheetRequest request) {
        return TimesheetResponse.from(timesheetService.createTimesheet(request));
    }

    @GetMapping
    public List<TimesheetResponse> getAllTimesheets() {
        return timesheetService.getAllTimesheets()
                .stream()
                .map(TimesheetResponse::from)
                .toList();
    }

    @GetMapping("/{id}")
    public TimesheetResponse getTimesheetById(@PathVariable UUID id) {
        return TimesheetResponse.from(timesheetService.getTimesheetById(id));
    }

    @PostMapping("/{id}/entries")
    public TimesheetResponse addEntry(@PathVariable UUID id,
                                      @RequestBody AddTimesheetEntryRequest request) {
        return TimesheetResponse.from(timesheetService.addEntry(id, request));
    }

    @PostMapping("/{id}/submit")
    public TimesheetResponse submit(@PathVariable UUID id) {
        return TimesheetResponse.from(timesheetService.submitTimesheet(id));
    }

    @PostMapping("/{id}/approve")
    public TimesheetResponse approve(@PathVariable UUID id,
                                     @RequestBody ApprovalRequest request) {
        return TimesheetResponse.from(timesheetService.approveTimesheet(id, request));
    }

    @PostMapping("/{id}/reject")
    public TimesheetResponse reject(@PathVariable UUID id,
                                    @RequestBody ApprovalRequest request) {
        return TimesheetResponse.from(timesheetService.rejectTimesheet(id, request));
    }
}