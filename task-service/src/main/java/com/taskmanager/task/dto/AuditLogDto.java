package com.taskmanager.task.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogDto {
    private Long taskId;
    private Long userId;
    private String oldStatus;
    private String newStatus;
}
