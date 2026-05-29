package com.taskmanager.auth.controller;

import com.taskmanager.auth.dto.AuthRequest;
import com.taskmanager.auth.dto.AuthResponse;
import com.taskmanager.auth.dto.RegisterRequest;
import com.taskmanager.auth.model.User;
import com.taskmanager.auth.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.registerUser(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {
        return ResponseEntity.ok(authService.authenticate(request));
    }

    // 💡 Frontend User Dropdown parsing pipeline check
    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(authService.getAllRegisteredUsers());
    }
}
