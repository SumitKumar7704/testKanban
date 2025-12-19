package com.example.kanbanboard.service;

import com.example.kanbanboard.exception.WipLimitExceededException;
import com.example.kanbanboard.model.*;
import com.example.kanbanboard.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class TaskService {

    @Autowired
    private UserRepository userRepo;

    // ===================== CREATE TASK =====================

    public Task createTask(String creatorId, String targetUserId,
                           String boardId, Task newTask) {

        User creator = userRepo.findById(creatorId)
                .orElseThrow(() -> new RuntimeException("Creator not found"));

        if (!creator.getAdmin()) {
            throw new RuntimeException("Only admin can create tasks");
        }

        User user = userRepo.findById(targetUserId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Board board = user.getBoards().stream()
                .filter(b -> b.getId().equals(boardId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Board not found"));

        Column todoColumn = board.getColumns().stream()
                .filter(c ->
                        c.getName().equalsIgnoreCase("TODO") ||
                                c.getName().equalsIgnoreCase("To Do"))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("TODO column not found"));

        newTask.setId(UUID.randomUUID().toString());

        // 🔒 Enforced defaults for creation ONLY
        newTask.setStatus(TaskStatus.TODO);
        newTask.setColumnId(todoColumn.getId());

        if (newTask.getAssignedAt() == null) {
            newTask.setAssignedAt(LocalDateTime.now());
        }

        if (newTask.getDescription() == null || newTask.getDescription().isBlank()) {
            throw new RuntimeException("Task description is required");
        }

        if (newTask.getPriority() == null) {
            newTask.setPriority(TaskPriority.MEDIUM);
        }

        todoColumn.getTasks().add(newTask);
        userRepo.save(user);

        return newTask;
    }

    // ===================== UPDATE TASK =====================

    public Task updateTask(String userId, String taskId, Task newData) {

        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        for (Board board : user.getBoards()) {
            for (Column column : board.getColumns()) {
                for (Task task : column.getTasks()) {

                    if (!task.getId().equals(taskId)) continue;

                    /* ================= STATUS UPDATE ================= */
                    if (newData.getStatus() != null &&
                            newData.getStatus() != task.getStatus()) {

                        TaskStatus newStatus = newData.getStatus();

                        // WIP limit check
                        if (newStatus == TaskStatus.IN_PROGRESS &&
                                task.getStatus() != TaskStatus.IN_PROGRESS) {

                            long inProgressCount = countInProgressTasks(user);
                            if (inProgressCount >= 3) {
                                throw new WipLimitExceededException(
                                        "You cannot have more than 3 tasks In Progress at the same time."
                                );
                            }
                        }

                        // 🔥 Move column ONLY when status changes
                        Column targetColumn = findColumnForStatus(board, newStatus);
                        if (targetColumn != null &&
                                !targetColumn.getId().equals(column.getId())) {

                            moveTaskBetweenColumns(task, column, targetColumn);
                        }

                        task.setStatus(newStatus);
                    }

                    /* ================= SAFE FIELD UPDATES ================= */

                    if (newData.getTitle() != null) {
                        task.setTitle(newData.getTitle());
                    }

                    if (newData.getDescription() != null) {
                        task.setDescription(newData.getDescription());
                    }

                    if (newData.getDeadline() != null) {
                        task.setDeadline(newData.getDeadline());
                    }

                    // 🔒 Priority update does NOT touch status or column
                    if (newData.getPriority() != null) {
                        task.setPriority(newData.getPriority());
                    }

                    userRepo.save(user);
                    return task;
                }
            }
        }

        throw new RuntimeException("Task not found");
    }

    // ===================== HELPERS =====================

    private long countInProgressTasks(User user) {
        return user.getBoards().stream()
                .flatMap(b -> b.getColumns().stream())
                .flatMap(c -> c.getTasks().stream())
                .filter(t -> t.getStatus() == TaskStatus.IN_PROGRESS)
                .count();
    }

    private Column findColumnForStatus(Board board, TaskStatus status) {

        return board.getColumns().stream()
                .filter(c ->
                        (status == TaskStatus.TODO &&
                                (c.getName().equalsIgnoreCase("TODO")
                                        || c.getName().equalsIgnoreCase("To Do")))
                                || (status == TaskStatus.IN_PROGRESS &&
                                c.getName().equalsIgnoreCase("In Progress"))
                                || (status == TaskStatus.DONE &&
                                c.getName().equalsIgnoreCase("DONE"))
                )
                .findFirst()
                .orElse(null);
    }

    private void moveTaskBetweenColumns(Task task,
                                        Column fromColumn,
                                        Column toColumn) {

        fromColumn.getTasks().removeIf(t -> t.getId().equals(task.getId()));
        toColumn.getTasks().add(task);
        task.setColumnId(toColumn.getId());
    }
}
