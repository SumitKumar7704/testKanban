package com.example.kanbanboard.controller;

import com.example.kanbanboard.dto.LoginRequest;
import com.example.kanbanboard.model.User;
import com.example.kanbanboard.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@CrossOrigin("*")
public class AuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public User register(@RequestBody User user) {
        // User contains: username, email, password, admin flag, etc.
        return userService.register(user);
    }

    @PostMapping("/login")
    public Map<String, String> login(@RequestBody LoginRequest request) {
        // Email-based login
        return userService.login(request.getEmail(), request.getPassword());
    }
}
