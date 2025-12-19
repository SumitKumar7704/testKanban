package com.example.kanbanboard.controller;

import com.example.kanbanboard.model.Task;
import com.example.kanbanboard.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/tasks")
@CrossOrigin(origins = "*")
public class TaskController {

    @Autowired
    private TaskService taskService;

    // =====================================================
    // CREATE TASK (ADMIN ONLY)
    // =====================================================
    @PostMapping
    public ResponseEntity<Task> createTask(
            @RequestParam String creatorId, // logged-in admin
            @RequestParam String userId,     // target user
            @RequestParam String boardId,
            @RequestBody Task task
    ) {
        /*
         🔒 SECURITY & CONSISTENCY:
         - Status is ALWAYS enforced in service (TODO)
         - Priority is allowed if admin selected it
         */
        task.setStatus(null);
        task.setColumnId(null);

        Task created = taskService.createTask(creatorId, userId, boardId, task);
        return ResponseEntity.ok(created);
    }

    // =====================================================
    // UPDATE TASK (PARTIAL UPDATE - PATCH)
    // =====================================================
    @PatchMapping("/{taskId}")
    public ResponseEntity<Task> updateTask(
            @PathVariable String taskId,
            @RequestParam String userId,
            @RequestBody Task data
    ) {
        /*
         ✅ IMPORTANT RULE:
         - Only fields present in `data` will be updated
         - Missing fields are preserved by TaskService
         - Priority update DOES NOT affect status
         - Status update DOES move columns
         */
        Task updated = taskService.updateTask(userId, taskId, data);
        return ResponseEntity.ok(updated);
    }
}
