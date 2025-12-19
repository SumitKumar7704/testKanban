package com.example.kanbanboard.model;

import java.time.LocalDateTime;

public class Task {

    private String id;
    private String title;
    private String description;

    private TaskStatus status;
    private TaskPriority priority;

    private LocalDateTime createdAt;
    private LocalDateTime assignedAt;
    private LocalDateTime deadline;

    private String columnId;

    //admin reopening approved task
    private Boolean approvedReopened;

    public Boolean getApprovedReopened() {
        return approvedReopened;
    }

    public void setApprovedReopened(Boolean approvedReopened) {
        this.approvedReopened = approvedReopened;
    }

    // approval flow
    private TaskApprovalStatus approvalStatus = TaskApprovalStatus.NONE;

    // user remark when marking DONE
    private String completionRemark;

    // admin remarks
    private String adminApprovalRemark;   // when approved (10 words max, enforced in service)
    private String adminRejectionRemark;  // when rejected (20 words max, enforced in service)

    // empty constructor ONLY
    public Task() {}

    // ===== User completion remark =====
    public String getCompletionRemark() {
        return completionRemark;
    }

    public void setCompletionRemark(String completionRemark) {
        this.completionRemark = completionRemark;
    }

    // ===== Admin approval remark =====
    public String getAdminApprovalRemark() {
        return adminApprovalRemark;
    }

    public void setAdminApprovalRemark(String adminApprovalRemark) {
        this.adminApprovalRemark = adminApprovalRemark;
    }

    // ===== Admin rejection remark =====
    public String getAdminRejectionRemark() {
        return adminRejectionRemark;
    }

    public void setAdminRejectionRemark(String adminRejectionRemark) {
        this.adminRejectionRemark = adminRejectionRemark;
    }

    // ===== Approval status =====
    public TaskApprovalStatus getApprovalStatus() {
        return approvalStatus;
    }

    public void setApprovalStatus(TaskApprovalStatus approvalStatus) {
        this.approvalStatus = approvalStatus;
    }

    // ===== existing getters & setters =====

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getColumnId() {
        return columnId;
    }

    public void setColumnId(String columnId) {
        this.columnId = columnId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public TaskPriority getPriority() {
        return priority;
    }

    public void setPriority(TaskPriority priority) {
        this.priority = priority;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getAssignedAt() {
        return assignedAt;
    }

    public void setAssignedAt(LocalDateTime assignedAt) {
        this.assignedAt = assignedAt;
    }

    public LocalDateTime getDeadline() {
        return deadline;
    }

    public void setDeadline(LocalDateTime deadline) {
        this.deadline = deadline;
    }
}
