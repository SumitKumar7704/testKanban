package com.example.kanbanboard.controller;

import com.example.kanbanboard.model.Task;
import com.example.kanbanboard.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

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

    // Update a task by id (status, title, description, deadline, priority)
    @PatchMapping("/{taskId}")
    public ResponseEntity<Task> updateTask(
            @PathVariable String taskId,
            @RequestParam String userId,
            @RequestBody Task data
    ) {
        Task updated = taskService.updateTask(userId, taskId, data);
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/{taskId}/priority")
    public ResponseEntity<?> updatePriority(
            @PathVariable String taskId,
            @RequestParam String adminId,
            @RequestParam String targetUserId,
            @RequestBody Map<String, String> body) {

        String priority = body.get("priority");
        taskService.updatePriority(taskId, priority, adminId, targetUserId);
        return ResponseEntity.ok().build();
    }


}
