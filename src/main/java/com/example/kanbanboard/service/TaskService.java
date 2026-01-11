package com.example.kanbanboard.service;

import com.example.kanbanboard.exception.WipLimitExceededException;
import com.example.kanbanboard.model.Board;
import com.example.kanbanboard.model.Column;
import com.example.kanbanboard.model.Task;
import com.example.kanbanboard.model.TaskPriority;
import com.example.kanbanboard.model.TaskStatus;
import com.example.kanbanboard.model.User;
import com.example.kanbanboard.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.springframework.security.access.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class TaskService {

    @Autowired
    private UserRepository userRepo;

    // Admin creates a task for a target user inside a board
    // Logically the task is in the TODO column (status = TODO)
    public Task createTask(String creatorId, String targetUserId,
                           String boardId, Task newTask) {

        // 1) Load creator and check admin
        User creator = userRepo.findById(creatorId)
                .orElseThrow(() -> new RuntimeException("Creator not found"));

        if (!creator.getAdmin()) {
            throw new RuntimeException("Only admin can create tasks");
        }

        // 2) Load target user (the one who will own the task)
        User user = userRepo.findById(targetUserId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 3) Find board on target user
        Board board = user.getBoards().stream()
                .filter(b -> b.getId().equals(boardId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Board not found"));

        // 4) Decide where to physically store the task
        Column todoColumn = board.getColumns().stream()
                .filter(c ->
                        c.getName().equalsIgnoreCase("TODO") ||
                                c.getName().equalsIgnoreCase("To Do"))
                .findFirst()
                .orElseGet(() -> board.getColumns().isEmpty() ? null : board.getColumns().get(0));

        if (todoColumn == null) {
            throw new RuntimeException("No columns defined on this board");
        }

        // 5) Generate task id and status = TODO by default
        newTask.setId(UUID.randomUUID().toString());

        // force TODO for new tasks regardless of what frontend sends
        newTask.setStatus(TaskStatus.TODO);

        // ensure assignedAt is set when admin creates it
        if (newTask.getAssignedAt() == null) {
            newTask.setAssignedAt(LocalDateTime.now());
        }

        // ensure description is present (admin must provide it)
        if (newTask.getDescription() == null || newTask.getDescription().isBlank()) {
            throw new RuntimeException("Task description is required");
        }

        // default priority ONLY if admin did not choose one
        if (newTask.getPriority() == null) {
            newTask.setPriority(TaskPriority.MEDIUM);
        }

        newTask.setColumnId(todoColumn.getId());

        // 6) Add task and save target user
        todoColumn.getTasks().add(newTask);
        userRepo.save(user);

        return newTask;
    }

    // Update a task by id (search nested) with WIP limit = 3
    public Task updateTask(String userId, String taskId, Task newData) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        for (Board board : user.getBoards()) {
            for (Column column : board.getColumns()) {
                for (Task task : column.getTasks()) {
                    if (task.getId().equals(taskId)) {

                        // 1) Status: only if explicitly provided and changed
                        if (newData.getStatus() != null &&
                                newData.getStatus() != task.getStatus()) {

                            TaskStatus newStatus = newData.getStatus();

                            if (newStatus == TaskStatus.IN_PROGRESS) {
                                long inProgressCount = countInProgressTasks(user);
                                if (task.getStatus() != TaskStatus.IN_PROGRESS &&
                                        inProgressCount >= 3) {
                                    throw new WipLimitExceededException(
                                            "You cannot have more than 3 tasks In Progress at the same time. Please finish some work before starting new tasks."
                                    );
                                }
                            }

                            task.setStatus(newStatus);

                            // keep columnId in sync with status
                            Column targetColumn = findColumnForStatus(board, newStatus);
                            if (targetColumn != null &&
                                    !targetColumn.getId().equals(column.getId())) {
                                moveTaskBetweenColumns(board, task, column, targetColumn);
                            }
                        }

                        // 2) Title: only if explicitly provided
                        if (newData.getTitle() != null) {
                            task.setTitle(newData.getTitle());
                        }

                        // 3) Description: only if explicitly provided
                        if (newData.getDescription() != null) {
                            task.setDescription(newData.getDescription());
                        }

                        // 4) Deadline: only if explicitly provided
                        if (newData.getDeadline() != null) {
                            task.setDeadline(newData.getDeadline());
                        }

//                        // 5) Priority: only if explicitly provided
//                        if (newData.getPriority() != null) {
//                            task.setPriority(newData.getPriority());
//                        }

                        userRepo.save(user);
                        return task;
                    }
                }
            }
        }

        throw new RuntimeException("Task not found");
    }

    // helper: count IN_PROGRESS tasks for this user across all boards/columns
    private long countInProgressTasks(User user) {
        return user.getBoards().stream()
                .flatMap(b -> b.getColumns().stream())
                .flatMap(c -> c.getTasks().stream())
                .filter(t -> t.getStatus() == TaskStatus.IN_PROGRESS)
                .count();
    }

    // helper: find a column that corresponds to a status
    private Column findColumnForStatus(Board board, TaskStatus status) {
        String expectedName;

        switch (status) {
            case TODO:
                expectedName = "TODO";
                break;
            case IN_PROGRESS:
                expectedName = "IN_PROGRESS";
                break;
            case DONE:
                expectedName = "DONE";
                break;
            default:
                expectedName = status.name();
        }

        return board.getColumns().stream()
                .filter(c -> c.getName().equalsIgnoreCase(expectedName)
                        || (status == TaskStatus.TODO && c.getName().equalsIgnoreCase("To Do"))
                        || (status == TaskStatus.IN_PROGRESS && c.getName().equalsIgnoreCase("In Progress")))
                .findFirst()
                .orElse(null);
    }

    // helper: actually move the task between columns and sync columnId
    private void moveTaskBetweenColumns(Board board, Task task,
                                        Column fromColumn, Column toColumn) {
        fromColumn.getTasks().removeIf(t -> t.getId().equals(task.getId()));
        toColumn.getTasks().add(task);
        task.setColumnId(toColumn.getId());
    }

    public void updatePriority(String taskId,
                               String priority,
                               String adminId,
                               String targetUserId) {

        // 1) Validate admin
        User admin = userRepo.findById(adminId)
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        if (!Boolean.TRUE.equals(admin.getAdmin())) {
            throw new RuntimeException("Only admin can change priority");
        }

        // 2) Load TARGET user (task owner)
        User user = userRepo.findById(targetUserId)
                .orElseThrow(() -> new RuntimeException("Target user not found"));

        // 3) Find task inside target user's boards
        for (Board board : user.getBoards()) {
            for (Column column : board.getColumns()) {
                for (Task task : column.getTasks()) {

                    if (task.getId().equals(taskId)) {

                        // 🚫 BLOCK priority change for DONE tasks
                        if (task.getStatus() == TaskStatus.DONE) {
                            throw new RuntimeException(
                                    "Cannot change priority of a completed task"
                            );
                        }

                        // ✅ Update priority
                        task.setPriority(TaskPriority.valueOf(priority));
                        userRepo.save(user);
                        return;
                    }
                }
            }
        }

        throw new RuntimeException("Task not found");
    }




}
