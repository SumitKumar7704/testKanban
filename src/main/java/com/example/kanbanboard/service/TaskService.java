package com.example.kanbanboard.service;

import com.example.kanbanboard.model.Board;
import com.example.kanbanboard.model.Column;
import com.example.kanbanboard.model.Task;
import com.example.kanbanboard.model.User;
import com.example.kanbanboard.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TaskService {

    @Autowired
    private UserRepository userRepo;

    // Create a task inside a specific column of a board for a user
    public Task createTask(String userId, String boardId, String columnId, Task newTask) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // find board
        Board board = user.getBoards().stream()
                .filter(b -> b.getId().equals(boardId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Board not found"));

        // find column
        Column column = board.getColumns().stream()
                .filter(c -> c.getId().equals(columnId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Column not found"));

        // generate a task id (e.g. UUID)
        newTask.setId(java.util.UUID.randomUUID().toString());

        // add task to column and save user
        column.getTasks().add(newTask);
        userRepo.save(user);

        return newTask;
    }

    // Update a task by id (search nested)
    public Task updateTask(String userId, String taskId, Task newData) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        for (Board board : user.getBoards()) {
            for (Column column : board.getColumns()) {
                for (Task task : column.getTasks()) {
                    if (task.getId().equals(taskId)) {
                        if (newData.getTitle() != null) {
                            task.setTitle(newData.getTitle());
                        }
                        if (newData.getDescription() != null) {
                            task.setDescription(newData.getDescription());
                        }
                        if (newData.getStatus() != null) {
                            task.setStatus(newData.getStatus());
                        }
                        userRepo.save(user);
                        return task;
                    }
                }
            }
        }

        throw new RuntimeException("Task not found");
    }
}
