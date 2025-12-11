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

    // Create a task inside a column of a board for a user
    @PostMapping
    public Task createTask(
            @RequestParam String userId,
            @RequestParam String boardId,
            @RequestParam String columnId,
            @RequestBody Task task
    ) {
        return taskService.createTask(userId, boardId, columnId, task);
    }

    // Update a task by id (search nested inside user)
    @PatchMapping("/{taskId}")
    public ResponseEntity<Task> updateTask(
            @PathVariable String taskId,
            @RequestParam String userId,
            @RequestBody Task data
    ) {
        Task updated = taskService.updateTask(userId, taskId, data);
        return ResponseEntity.ok(updated);
    }
}
