package com.example.kanbanboard.controller;

import com.example.kanbanboard.model.Task;
import com.example.kanbanboard.model.TaskStatus;
import com.example.kanbanboard.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;




@RestController
@RequestMapping("/tasks")
public class TaskController {

    @Autowired
    private TaskRepository taskRepository;







    // 1) Create a task (card) - goes to TO_DO column by default
    @PostMapping
    public Task createTask(@RequestBody Task task) {
        task.setId(null); // ensure new
        if (task.getStatus() == null) {
            task.setStatus(TaskStatus.TO_DO);
        }
        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());
        return taskRepository.save(task);
    }





    // 2) Get tasks, optionally by column(status)
    @GetMapping
    public List<Task> getTasks(@RequestParam(required = false) TaskStatus status) {
        if (status == null) {
            return taskRepository.findAll();
        }
        return taskRepository.findByStatus(status);
    }




    // 3) Move task between columns (change status only)
    @PatchMapping("/{id}/status")
    public Task updateTaskStatus(@PathVariable String id,
                                 @RequestParam TaskStatus status) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found"));
        task.setStatus(status);
        task.setUpdatedAt(LocalDateTime.now());
        return taskRepository.save(task);
    }
}
