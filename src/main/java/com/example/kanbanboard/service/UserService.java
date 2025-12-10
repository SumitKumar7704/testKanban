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
        if (userRepo.findByUsername(user.getUsername()) != null) {
            System.out.println("DEBUG: Username already exists = " + user.getUsername());
            return user; // just return without throwing for now
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        User saved = userRepo.save(user);
        System.out.println("DEBUG: Registered user id = " + saved.getId());
        return saved;
    }

    public String login(String username, String rawPassword) {
        System.out.println("DEBUG: login called with username = " + username);

        User user = userRepo.findByUsername(username);
        if (user == null) {
            System.out.println("DEBUG: user not found for username = " + username);
            return "user-not-found";
        }

        System.out.println("DEBUG: stored encoded password = " + user.getPassword());
        boolean matches = passwordEncoder.matches(rawPassword, user.getPassword());
        System.out.println("DEBUG: password matches = " + matches);

        if (!matches) {
            System.out.println("DEBUG: bad password for username = " + username);
            return "bad-password";
        }

        String token = jwtService.generateToken(user);
        System.out.println("DEBUG: generated token = " + token);
        return token;
    }

    public User findByUsername(String username) {
        return userRepo.findByUsername(username);
    }
}
