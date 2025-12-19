package com.example.kanbanboard.service;

import com.example.kanbanboard.exception.TaskLockedException;
import com.example.kanbanboard.exception.WipLimitExceededException;
import com.example.kanbanboard.model.*;
import com.example.kanbanboard.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
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

        // Enforced defaults for creation ONLY
        newTask.setStatus(TaskStatus.TODO);
        newTask.setColumnId(todoColumn.getId());
        newTask.setApprovalStatus(TaskApprovalStatus.NONE);
        newTask.setApprovedReopened(false);

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

    // ===================== UPDATE TASK (normal path) =====================

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

                        // Block any status change if task is already approved
                        if (task.getApprovalStatus() == TaskApprovalStatus.APPROVED) {
                            throw new TaskLockedException(
                                    "This task has been approved and locked by the admin." +
                                            " Completed tasks can’t be moved back to In Progress or To Do."
                            );
                        }

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

                        // Move column ONLY when status changes
                        Column targetColumn = findColumnForStatus(board, newStatus);
                        if (targetColumn != null &&
                                !targetColumn.getId().equals(column.getId())) {

                            moveTaskBetweenColumns(task, column, targetColumn);
                        }

                        task.setStatus(newStatus);

                        // approval flow based on status
                        if (newStatus == TaskStatus.DONE) {
                            task.setApprovalStatus(TaskApprovalStatus.PENDING_REVIEW);
                            task.setApprovedReopened(false); // fresh completion, not reopened
                        } else {
                            // whenever task is not DONE, clear approval + admin remarks
                            task.setApprovalStatus(TaskApprovalStatus.NONE);
                            task.setAdminApprovalRemark(null);
                            task.setAdminRejectionRemark(null);
                            task.setApprovedReopened(false);
                        }
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

                    // user completion remark when marking DONE
                    if (newData.getCompletionRemark() != null) {
                        task.setCompletionRemark(newData.getCompletionRemark());
                    }

                    // priority update does NOT touch status or column
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

    // ===================== ADMIN OVERRIDE STATUS (approved -> TODO) =====================

    public Task overrideTaskStatus(String adminId,
                                   String userId,
                                   String taskId,
                                   Task overrideData) {

        // Ensure caller is admin
        User admin = userRepo.findById(adminId)
                .orElseThrow(() -> new RuntimeException("Admin not found"));
        if (!admin.getAdmin()) {
            throw new RuntimeException("Only admin can override task status");
        }

        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        for (Board board : user.getBoards()) {
            for (Column column : board.getColumns()) {
                for (Task task : column.getTasks()) {

                    if (!task.getId().equals(taskId)) continue;

                    // Only allow override if it was approved DONE
                    if (task.getApprovalStatus() != TaskApprovalStatus.APPROVED ||
                            task.getStatus() != TaskStatus.DONE) {
                        throw new RuntimeException("Override allowed only for approved DONE tasks");
                    }

                    TaskStatus targetStatus = overrideData.getStatus();
                    if (targetStatus == null || targetStatus != TaskStatus.TODO) {
                        throw new RuntimeException("Override can only move task back to TODO");
                    }

                    // Move task to TODO column
                    Column targetColumn = findColumnForStatus(board, TaskStatus.TODO);
                    if (targetColumn != null &&
                            !targetColumn.getId().equals(column.getId())) {
                        moveTaskBetweenColumns(task, column, targetColumn);
                    }

                    // Set status & flags
                    task.setStatus(TaskStatus.TODO);
                    task.setApprovalStatus(TaskApprovalStatus.NONE);
                    task.setAdminApprovalRemark(null);
                    task.setAdminRejectionRemark(null);
                    task.setApprovedReopened(true); // flag for yellow highlight

                    // Optional: store admin-provided explanation as completionRemark (max 20 words)
                    if (overrideData.getCompletionRemark() != null &&
                            !overrideData.getCompletionRemark().isBlank()) {

                        String remark = overrideData.getCompletionRemark().trim();
                        String[] words = remark.split("\\s+");
                        if (words.length > 20) {
                            remark = String.join(" ", Arrays.copyOf(words, 20));
                        }
                        task.setCompletionRemark(remark);
                    }

                    userRepo.save(user);
                    return task;
                }
            }
        }

        throw new RuntimeException("Task not found");
    }

    // ===================== ADMIN REVIEW TASK =====================

    public Task reviewTask(String adminId,
                           String userId,
                           String taskId,
                           boolean approved,
                           String remark) {

        User admin = userRepo.findById(adminId)
                .orElseThrow(() -> new RuntimeException("Admin not found"));
        if (!admin.getAdmin()) {
            throw new RuntimeException("Only admin can review tasks");
        }

        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        for (Board board : user.getBoards()) {
            for (Column column : board.getColumns()) {
                for (Task task : column.getTasks()) {
                    if (!task.getId().equals(taskId)) continue;

                    if (approved) {
                        // lock as approved DONE
                        task.setApprovalStatus(TaskApprovalStatus.APPROVED);
                        task.setStatus(TaskStatus.DONE);
                        task.setApprovedReopened(false);

                        // 10-word max appreciation remark
                        if (remark != null && !remark.isBlank()) {
                            String[] words = remark.trim().split("\\s+");
                            if (words.length > 10) {
                                remark = String.join(" ",
                                        Arrays.copyOf(words, 10));
                            }
                            task.setAdminApprovalRemark(remark);
                            task.setAdminRejectionRemark(null);
                        }
                    } else {
                        // mark as rejected and move back to TODO
                        task.setApprovalStatus(TaskApprovalStatus.REJECTED);
                        task.setStatus(TaskStatus.TODO);
                        task.setAdminApprovalRemark(null);
                        task.setApprovedReopened(false);

                        Column target = findColumnForStatus(board, TaskStatus.TODO);
                        if (target != null &&
                                !target.getId().equals(column.getId())) {
                            moveTaskBetweenColumns(task, column, target);
                        }

                        // 20-word max rejection remark
                        if (remark != null && !remark.isBlank()) {
                            String[] words = remark.trim().split("\\s+");
                            if (words.length > 20) {
                                remark = String.join(" ",
                                        Arrays.copyOf(words, 20));
                            }
                            task.setAdminRejectionRemark(remark);
                        }
                    }

                    userRepo.save(user);
                    return task;
                }
            }
        }

        throw new RuntimeException("Task not found");
    }

    // ===================== ADMIN DELETE TASK =====================

    public void deleteTask(String adminId, String userId, String taskId) {

        User admin = userRepo.findById(adminId)
                .orElseThrow(() -> new RuntimeException("Admin not found"));
        if (!admin.getAdmin()) {
            throw new RuntimeException("Only admin can delete tasks");
        }

        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        boolean removed = false;

        for (Board board : user.getBoards()) {
            for (Column column : board.getColumns()) {
                // removeIf returns true if any element was removed
                boolean colRemoved = column.getTasks().removeIf(t -> t.getId().equals(taskId));
                if (colRemoved) {
                    removed = true;
                    break;
                }
            }
            if (removed) break;
        }

        if (!removed) {
            throw new RuntimeException("Task not found");
        }

        userRepo.save(user);
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
