package com.example.kanbanboard.controller;

import com.example.kanbanboard.model.User;
import com.example.kanbanboard.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RequestMapping("/test")
public class TestController {

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/user")
    public User createTestUser() {
        User testUser = new User("Sumit", "sumit@gmail.com", "member", 3);
        return userRepository.save(testUser);
    }

    @GetMapping("/users")
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

}
