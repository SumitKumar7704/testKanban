package com.example.kanbanboard.service;

import com.example.kanbanboard.model.Board;
import com.example.kanbanboard.model.Column;
import com.example.kanbanboard.model.User;
import com.example.kanbanboard.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class BoardService {

    @Autowired
    private UserRepository userRepo;

    // new board
    public Board create(String userId, Board board) {
        // Load user
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Generate board id
        board.setId(UUID.randomUUID().toString());

        // Auto-create 3 default columns if none exist yet
        if (board.getColumns() == null || board.getColumns().isEmpty()) {
            List<Column> cols = new ArrayList<>();

            Column todo = new Column();
            todo.setId(UUID.randomUUID().toString());
            todo.setName("TODO");
            todo.setTasks(new ArrayList<>());

            Column inProgress = new Column();
            inProgress.setId(UUID.randomUUID().toString());
            inProgress.setName("IN_PROGRESS");
            inProgress.setTasks(new ArrayList<>());

            Column done = new Column();
            done.setId(UUID.randomUUID().toString());
            done.setName("DONE");
            done.setTasks(new ArrayList<>());

            cols.add(todo);
            cols.add(inProgress);
            cols.add(done);

            board.setColumns(cols);
        }

        // Add board to user's boards list
        user.getBoards().add(board);

        // Save user (boards are embedded)
        userRepo.save(user);

        return board;
    }

    // Get all boards for a given user
    public List<Board> getUserBoards(String userId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return user.getBoards();
    }

    // Delete a board (only admin can do this)
    public void deleteBoard(String adminUserId, String targetUserId, String boardId) {
        // 1. Load admin user and check flag
        User admin = userRepo.findById(adminUserId)
                .orElseThrow(() -> new RuntimeException("Admin user not found"));

        if (!admin.getAdmin()) {
            throw new RuntimeException("Only admin can delete boards");
        }

        // 2. Load the target user whose board will be deleted
        User targetUser = userRepo.findById(targetUserId)
                .orElseThrow(() -> new RuntimeException("Target user not found"));

        // 3. Remove board from target user's boards list
        boolean removed = targetUser.getBoards()
                .removeIf(b -> b.getId().equals(boardId));

        if (!removed) {
            throw new RuntimeException("Board not found for this user");
        }

        // 4. Save target user (embedded boards updated)
        userRepo.save(targetUser);
    }
}
