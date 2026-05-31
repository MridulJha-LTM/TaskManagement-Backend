package com.taskmanager.taskstatus.dto;

import lombok.Data;

@Data
public class TaskStatusUpdateRequest {
    private Long taskId;
    private Long userId;
    private String newStatus;
}