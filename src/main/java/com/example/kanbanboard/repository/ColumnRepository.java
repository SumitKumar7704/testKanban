
package com.example.kanbanboard.repository;

import com.example.kanbanboard.model.Column;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ColumnRepository extends MongoRepository<Column, String> {
    List<Column> findByBoardId(String boardId);
}
