package com.example.kanbanboard.repository;

import com.example.kanbanboard.model.Task;
import com.example.kanbanboard.model.TaskStatus;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface TaskRepository extends MongoRepository<Task, String> {

    List<Task> findByStatus(TaskStatus status);
}
