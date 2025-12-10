package com.example.kanbanboard.service;

import com.example.kanbanboard.model.Task;
import com.example.kanbanboard.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class TaskService {

    @Autowired
    private TaskRepository taskRepo;

    public Task create(Task task) {
        return taskRepo.save(task);
    }

    public Optional<Task> update(String id, Task newData) {
        return taskRepo.findById(id).map(task -> {
            if (newData.getTitle() != null) {
                task.setTitle(newData.getTitle());
            }
            if (newData.getDescription() != null) {
                task.setDescription(newData.getDescription());
            }
            if (newData.getStatus() != null) {
                task.setStatus(newData.getStatus());
            }
            if (newData.getColumnId() != null) {
                task.setColumnId(newData.getColumnId());
            }
            return taskRepo.save(task);
        });
    }
}
