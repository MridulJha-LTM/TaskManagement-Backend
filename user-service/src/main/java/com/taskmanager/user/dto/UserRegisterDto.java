package com.taskmanager.user.dto;

import lombok.Data;

@Data
public class UserRegisterDto {
    private String username;
    private String email;
    private String password;
    private String role; // Expects "ROLE_USER" or "ROLE_ADMIN"
}
