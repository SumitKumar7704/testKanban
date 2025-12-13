package com.example.kanbanboard.service;

import com.example.kanbanboard.model.User;
import com.example.kanbanboard.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

        // Admin roles: first user becomes admin, others are false
        if (userRepo.findByAdminTrue() == null) {
            user.setAdmin(true);
        } else {
            user.setAdmin(false);
        }

        // Encode password and save
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepo.save(user);
    }

    // Return token + userId + isAdmin for frontend
    public Map<String, String> login(String username, String rawPassword) {
        User user = userRepo.findByUsername(username);
        if (user == null) {
            throw new RuntimeException("Invalid credentials: user not found");
        }
        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new RuntimeException("Invalid credentials: bad password");
        }

        String token = jwtService.generateToken(user);

        Map<String, String> result = new HashMap<>();
        result.put("token", token);
        result.put("userId", user.getId());
        // Boolean to string
        result.put("isAdmin", user.getAdmin().toString());
        return result;
    }

    public User findByUsername(String username) {
        return userRepo.findByUsername(username);
    }

    // NEW: list all users (for admin dropdown)
    public List<User> findAllUsers() {
        return userRepo.findAll();
    }
}
