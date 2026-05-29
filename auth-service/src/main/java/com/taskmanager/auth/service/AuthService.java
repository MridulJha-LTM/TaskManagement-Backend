package com.taskmanager.auth.service;

import com.taskmanager.auth.dto.AuthRequest;
import com.taskmanager.auth.dto.AuthResponse;
import com.taskmanager.auth.dto.RegisterRequest;
import com.taskmanager.auth.model.Role;
import com.taskmanager.auth.model.User;
import com.taskmanager.auth.repository.UserRepository;
import com.taskmanager.auth.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;


@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public String registerUser(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists!");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists!");
        }

        Role assignedRole = "ADMIN".equalsIgnoreCase(request.getRole()) ? Role.ROLE_ADMIN : Role.ROLE_USER;

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword())) // BCrypt storage
                .role(assignedRole)
                .build();

        userRepository.save(user);
        return "User registered successfully!";
    }

    public AuthResponse authenticate(AuthRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found!"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid credentials!");
        }

        String token = jwtService.generateToken(user.getUsername(), user.getRole().name(), user.getId());
        return new AuthResponse(token, user.getUsername(), user.getRole().name(), user.getId());
    }

    // 💡 Task Management System Mapping Endpoint Logic
    public List<User> getAllRegisteredUsers() {
        return userRepository.findAll();
    }
}
