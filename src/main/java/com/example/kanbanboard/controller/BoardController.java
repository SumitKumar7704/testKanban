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

    @PostMapping
    public Board create(@RequestBody Board board) {
        return boardService.create(board);
    }

    @GetMapping("/user/{userId}")
    public List<Board> getBoards(@PathVariable String userId) {
        return boardService.getUserBoards(userId);
    }
}
