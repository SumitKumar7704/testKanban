
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

    @PostMapping
    public Column create(@RequestBody Column column) {
        return columnService.create(column);
    }

    @GetMapping("/board/{boardId}")
    public List<Column> getColumns(@PathVariable String boardId) {
        return columnService.getBoardColumns(boardId);
    }
}
