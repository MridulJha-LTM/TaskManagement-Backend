package com.taskmanager.taskstatus.dto;

import lombok.Data;

@Data
public class TaskResponseDto {
    private Long id;
    private String taskStatus;
    private Long assignedUserId;
}
