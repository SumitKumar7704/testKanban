package com.example.kanbanboard.controller;

import com.example.kanbanboard.model.Column;
import com.example.kanbanboard.service.ColumnService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/columns")
@CrossOrigin("*")
public class ColumnController {

    @Autowired
    private ColumnService columnService;

    // Create a column in a board for a user
    @PostMapping
    public Column createColumn(
            @RequestParam String userId,
            @RequestParam String boardId,
            @RequestBody Column column
    ) {
        return columnService.create(userId, boardId, column);
    }

    // Get all columns for a board of a user
    @GetMapping
    public List<Column> getBoardColumns(
            @RequestParam String userId,
            @RequestParam String boardId
    ) {
        return columnService.getBoardColumns(userId, boardId);
    }
}
