package com.example.kanbanboard.service;

import com.example.kanbanboard.model.Board;
import com.example.kanbanboard.model.Column;
import com.example.kanbanboard.model.User;
import com.example.kanbanboard.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ColumnService {

    @Autowired
    private UserRepository userRepo;

    // Create a column under a specific board for a user
    public Column create(String userId, String boardId, Column column) {
        // Load user
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Find board inside user
        Board board = user.getBoards().stream()
                .filter(b -> b.getId().equals(boardId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Board not found"));

        // Generate column id and add to board
        column.setId(UUID.randomUUID().toString());
        board.getColumns().add(column);

        // Save user (cascades columns and tasks inside)
        userRepo.save(user);

        return column;
    }

    // Get all columns for a board of a user
    public List<Column> getBoardColumns(String userId, String boardId) {
        // Load user
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Find board and return its columns
        return user.getBoards().stream()
                .filter(b -> b.getId().equals(boardId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Board not found"))
                .getColumns();
    }
}
