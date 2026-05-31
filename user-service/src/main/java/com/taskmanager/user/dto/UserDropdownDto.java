package com.taskmanager.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserDropdownDto {
    private Long id;
    private String username;
}
