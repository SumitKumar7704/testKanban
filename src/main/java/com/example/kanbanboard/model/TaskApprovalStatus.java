package com.example.kanbanboard.model;

public enum TaskApprovalStatus {
    NONE,           // not in DONE yet
    PENDING_REVIEW, // user marked DONE, waiting for admin
    APPROVED,       // admin confirmed
    REJECTED        // admin rejected
}
