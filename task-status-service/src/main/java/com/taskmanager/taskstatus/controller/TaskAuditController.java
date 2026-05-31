package com.taskmanager.taskstatus.controller;

import com.taskmanager.taskstatus.dto.AuditLogRequest;
import com.taskmanager.taskstatus.dto.TaskResponseDto;
import com.taskmanager.taskstatus.dto.TaskStatusUpdateRequest;
import com.taskmanager.taskstatus.entity.TaskAudit;
import com.taskmanager.taskstatus.repository.TaskAuditRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import java.time.LocalDateTime;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api/status")
public class TaskAuditController {
    @Autowired
    private TaskAuditRepository auditRepository;

    @Autowired
    private RestTemplate restTemplate;

    // ENDPOINT 1: Explicit Logger Endpoint (Triggered by Admin Actions from task-service)
    @PostMapping("/log")
    public ResponseEntity<Void> logAuditEntry(@RequestBody AuditLogRequest request) {
        TaskAudit audit = new TaskAudit();
        audit.setTaskId(request.getTaskId());
        audit.setAssignedUserId(request.getUserId());
        audit.setOldStatus(request.getOldStatus());
        audit.setNewStatus(request.getNewStatus());
        audit.setTimestamp(LocalDateTime.now());

        auditRepository.save(audit);
        return ResponseEntity.ok().build();
    }

    // ENDPOINT 2: User Action Endpoint (Triggered by standard User Dashboard to alter status)
    @PutMapping("/update-task-status")
    public ResponseEntity<String> processUserStatusUpdate(@RequestBody TaskStatusUpdateRequest request) {

        // A. Call task-service to fetch the active state of the task
        String taskServiceUrl = "http://localhost:8082/api/tasks/" + request.getTaskId();
        TaskResponseDto activeTask;
        try {
            activeTask = restTemplate.getForObject(taskServiceUrl, TaskResponseDto.class);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Error: Target task records could not be fetched.");
        }

        if (activeTask == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Error: Task does not exist.");
        }

        // B. SECURITY CHECK: Verify if the user trying to change the status is actually assigned to it
        if (activeTask.getAssignedUserId() == null || !activeTask.getAssignedUserId().equals(request.getUserId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access Denied: You cannot change status for a task not assigned to you.");
        }

        // C. WRITE TRANSACTION LOG TO AUDIT_DB
        TaskAudit audit = new TaskAudit();
        audit.setTaskId(request.getTaskId());
        audit.setAssignedUserId(request.getUserId());
        audit.setOldStatus(activeTask.getTaskStatus()); // Extracted current status dynamically
        audit.setNewStatus(request.getNewStatus());
        audit.setTimestamp(LocalDateTime.now());
        auditRepository.save(audit);

        // D. SYNC BACKWARD: Call task-service's internal route to permanently update task_db state
        String syncUrl = "http://localhost:8082/api/tasks/internal/update-status/"
                + request.getTaskId() + "?newStatus=" + request.getNewStatus();
        try {
            restTemplate.put(syncUrl, null);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Audit saved, but failed to sync state down to task-service.");
        }

        return ResponseEntity.ok("Task status altered and transaction audit saved successfully.");
    }
}
