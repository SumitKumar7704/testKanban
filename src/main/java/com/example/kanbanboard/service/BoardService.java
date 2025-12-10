package com.example.kanbanboard.service;

import com.example.kanbanboard.model.Board;
import com.example.kanbanboard.repository.BoardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BoardService {

    @Autowired
    private BoardRepository boardRepo;

    public Board create(Board board) {
        return boardRepo.save(board);
    }

    public List<Board> getUserBoards(String ownerId) {
        return boardRepo.findByOwnerId(ownerId);
    }
}
