package com.example.kanbanboard.exception;


public class TaskLockedException extends RuntimeException {
    public TaskLockedException(String message) {
        super(message);
    }
}
