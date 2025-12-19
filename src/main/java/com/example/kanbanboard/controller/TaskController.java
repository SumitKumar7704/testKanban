package com.example.kanbanboard.controller;

import com.example.kanbanboard.model.Task;
import com.example.kanbanboard.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/tasks")
@CrossOrigin("*")
public class TaskController {

    @Autowired
    private TaskService taskService;

    // Admin (creatorId) creates a task for a target user in a board.
    // Task always starts in TODO column logically.
    @PostMapping
    public Task createTask(
            @RequestParam String creatorId, // logged-in admin
            @RequestParam String userId,    // target user to assign to
            @RequestParam String boardId,
            @RequestBody Task task
    ) {
        // ignore any status sent from frontend, enforce TODO via service
        task.setStatus(null);
        return taskService.createTask(creatorId, userId, boardId, task);
    }

    // Normal update (status, title, description, deadline, priority, completionRemark)
    // still enforces "approved and locked" rule.
    @PatchMapping("/{taskId}")
    public ResponseEntity<Task> updateTask(
            @PathVariable String taskId,
            @RequestParam String userId,
            @RequestBody Task data
    ) {
        Task updated = taskService.updateTask(userId, taskId, data);
        return ResponseEntity.ok(updated);
    }

    // Admin review: approve/reject a DONE task
    @PatchMapping("/{taskId}/review")
    public ResponseEntity<Task> reviewTask(
            @PathVariable String taskId,
            @RequestParam String adminId,
            @RequestParam String userId,
            @RequestParam boolean approved,
            @RequestParam(required = false) String remark
    ) {
        Task reviewed = taskService.reviewTask(adminId, userId, taskId, approved, remark);
        return ResponseEntity.ok(reviewed);
    }

    // Admin override to move an already approved DONE task back to TODO
    // and optionally mark it as "reopened" + set a remark visible to both.
    @PatchMapping("/{taskId}/override-status")
    public ResponseEntity<Task> overrideTaskStatus(
            @PathVariable String taskId,
            @RequestParam String adminId,          // logged-in admin id
            @RequestParam String userId,           // owner of the task (board user)
            @RequestBody Task data                 // expect at least status + completionRemark
    ) {
        Task updated = taskService.overrideTaskStatus(adminId, userId, taskId, data);
        return ResponseEntity.ok(updated);
    }

    // NEW: Admin delete task
    @DeleteMapping("/{taskId}")
    public ResponseEntity<Void> deleteTask(
            @PathVariable String taskId,
            @RequestParam String userId,   // board owner (whose boards/columns contain the task)
            @RequestParam String adminId   // logged-in admin performing delete
    ) {
        taskService.deleteTask(adminId, userId, taskId);
        return ResponseEntity.noContent().build();
    }
}
