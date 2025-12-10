package com.example.kanbanboard.service;

import com.example.kanbanboard.model.User;
import com.example.kanbanboard.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    public User register(User user) {
        // Fail if username already exists
        if (userRepo.findByUsername(user.getUsername()) != null) {
            throw new RuntimeException("Username already exists");
        }

        // Encode password and save
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepo.save(user);
    }

    public String login(String username, String rawPassword) {
        User user = userRepo.findByUsername(username);

        if (user == null) {
            throw new RuntimeException("Invalid credentials: user not found");
        }

        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new RuntimeException("Invalid credentials: bad password");
        }

        // Return JWT token
        return jwtService.generateToken(user);
    }

    public User findByUsername(String username) {
        return userRepo.findByUsername(username);
    }
}
