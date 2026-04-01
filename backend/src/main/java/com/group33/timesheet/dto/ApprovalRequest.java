package com.group33.timesheet.dto;

public class ApprovalRequest {

    private String managerId;
    private String comment;

    public ApprovalRequest() {
    }

    public String getManagerId() {
        return managerId;
    }

    public void setManagerId(String managerId) {
        this.managerId = managerId;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
