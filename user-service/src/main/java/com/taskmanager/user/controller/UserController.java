package com.taskmanager.user.controller;

import com.taskmanager.user.dto.UserDropdownDto;
import com.taskmanager.user.dto.UserProfileDto;
import com.taskmanager.user.dto.UserRegisterDto;
import com.taskmanager.user.entity.User;
import com.taskmanager.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import java.util.List;
import java.util.stream.Collectors;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping
    public List<User> getAllUser(){
        return userRepository.findAll();
    }

    // 1. READ Profile
    @GetMapping("/profile/{id}")
    public ResponseEntity<User> getUserProfile(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(user);
    }

    // 2. UPDATE Profile
    @PutMapping("/profile/{id}")
    public ResponseEntity<User> updateProfile(@PathVariable Long id, @RequestBody UserProfileDto profileDto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setUsername(profileDto.getUsername());
        user.setEmail(profileDto.getEmail());

        return ResponseEntity.ok(userRepository.save(user));
    }

    // 3. DELETE Account (Triggers Task Cleanup via RestTemplate)
    @DeleteMapping("/profile/{id}")
    public ResponseEntity<Void> deleteAccount(@PathVariable Long id) {
        if (!userRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        // Call task-service to clear assignments for this user before deleting them
        String taskServiceUrl = "http://localhost:8082/api/tasks/internal/unassign-user/" + id;
        try {
            restTemplate.put(taskServiceUrl, null);
        } catch (Exception e) {
            System.out.println("Could not unassign tasks, task-service might be offline: " + e.getMessage());
        }

        userRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }

    // 4. DROPDOWN LIST: Used by Admin to assign tasks
    @GetMapping("/dropdown-list")
    public ResponseEntity<List<UserDropdownDto>> getUsersForDropdown() {
        List<UserDropdownDto> list = userRepository.findAll().stream()
                .filter(user -> "ROLE_USER".equals(user.getRole())) // Only assign to standard users
                .map(user -> new UserDropdownDto(user.getId(), user.getUsername()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    @PostMapping
    public ResponseEntity<?> registerUser(@RequestBody UserRegisterDto registerDto) {

        // 1. Validation: Check if username already exists
        if (userRepository.existsByUsername(registerDto.getUsername())) {
            return ResponseEntity.badRequest().body("Error: Username is already taken!");
        }

        // 2. Validation: Check if email already exists
        if (userRepository.existsByEmail(registerDto.getEmail())) {
            return ResponseEntity.badRequest().body("Error: Email is already in use!");
        }

        // 3. Fallback: Default to ROLE_USER if role is missing or invalid
        String assignedRole = registerDto.getRole();
        if (assignedRole == null || assignedRole.isEmpty()) {
            assignedRole = "ROLE_USER";
        }

        // 4. Build and encrypt the entity object
        User newUser = User.builder()
                .username(registerDto.getUsername())
                .email(registerDto.getEmail())
                .password(passwordEncoder.encode(registerDto.getPassword())) // Hashed security layer
                .role(assignedRole)
                .build();

        // 5. Save directly into user_db
        User savedUser = userRepository.save(newUser);

        return ResponseEntity.ok("User registered successfully with ID: " + savedUser.getId());
    }

}
