
package com.example.kanbanboard.service;

import com.example.kanbanboard.model.Column;
import com.example.kanbanboard.repository.ColumnRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ColumnService {

    @Autowired
    private ColumnRepository columnRepo;

    public Column create(Column column) {
        return columnRepo.save(column);
    }

    public List<Column> getBoardColumns(String boardId) {
        return columnRepo.findByBoardId(boardId);
    }
}
