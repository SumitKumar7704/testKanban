package com.example.kanbanboard.controller;

import com.example.kanbanboard.model.Board;
import com.example.kanbanboard.service.BoardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/boards")
@CrossOrigin("*")
public class BoardController {

    @Autowired
    private BoardService boardService;

    // Create board for a given user
    @PostMapping
    public Board createBoard(@RequestParam String userId, @RequestBody Board board) {
        return boardService.create(userId, board);
    }


    // Get all boards for a given user
    @GetMapping("/user/{userId}")
    public List<Board> getUserBoards(@PathVariable String userId) {
        return boardService.getUserBoards(userId);
    }
}
