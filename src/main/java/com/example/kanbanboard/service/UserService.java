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
        // Fail if username already exists (keep this check)
        if (userRepo.findByUsername(user.getUsername()) != null) {
            throw new RuntimeException("Username already exists");
        }

        // OPTIONAL but recommended: also prevent duplicate emails
        if (user.getEmail() != null && userRepo.findByEmail(user.getEmail()) != null) {
            throw new RuntimeException("Email already exists");
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
    // CHANGE: first argument is now email, not username
    public Map<String, String> login(String email, String rawPassword) {
        User user = userRepo.findByEmail(email);
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
        result.put("username", user.getUsername());
        return result;
    }

    // Still available if you use username elsewhere in the app
    public User findByUsername(String username) {
        return userRepo.findByUsername(username);
    }

    // List all users (for admin dropdown)
    public List<User> findAllUsers() {
        return userRepo.findAll();
    }
}
