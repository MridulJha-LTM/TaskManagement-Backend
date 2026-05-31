package com.taskmanager.taskstatus.dto;

import lombok.Data;

@Data
public class AuditLogRequest {
    private Long taskId;
    private Long userId;
    private String oldStatus;
    private String newStatus;
}
