package com.example.kanbanboard.service;

import com.example.kanbanboard.model.Board;
import com.example.kanbanboard.model.User;
import com.example.kanbanboard.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

        // Generate board id and add to user's boards list
        board.setId(UUID.randomUUID().toString());
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
}
