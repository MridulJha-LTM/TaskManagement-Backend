package com.taskmanager.task.controller;

import com.taskmanager.task.dto.AuditLogDto;
import com.taskmanager.task.entity.Task;
import com.taskmanager.task.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private RestTemplate restTemplate;

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping
    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping
    public Task createTask(@RequestBody Task task) {
        if (task.getAssignedUserId() == null || task.getAssignedUserId() == 0) {
            task.setAssignedUserId(null);
        }
        Task savedTask = taskRepository.save(task);

        try {
            String auditUrl = "http://localhost:8083/api/status/log";
            AuditLogDto auditPayload = new AuditLogDto(
                    savedTask.getId(),
                    savedTask.getAssignedUserId(),
                    "None",
                    savedTask.getTaskStatus()
            );

            restTemplate.postForEntity(auditUrl, auditPayload, Void.class);
            System.out.println(">>> SUCCESS: Task creation audit sent to Port 8083.");
        } catch (Exception e) {
            System.err.println(">>> ERROR: Audit logging failed: " + e.getMessage());
            e.printStackTrace();
        }

        return savedTask;

    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<Task> updateTask(@PathVariable Long id, @RequestBody Task taskDetails) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found with id: " + id));

        String oldStatus = task.getTaskStatus();

        task.setTaskStatus(taskDetails.getTaskStatus());
        task.setTaskName(taskDetails.getTaskName());
        task.setDescription(taskDetails.getDescription());

        if (taskDetails.getAssignedUserId() == null || taskDetails.getAssignedUserId() == 0) {
            task.setAssignedUserId(null);
        } else {
            task.setAssignedUserId(taskDetails.getAssignedUserId());
        }

        Task updatedTask = taskRepository.save(task);

        try {
            String auditUrl = "http://localhost:8083/api/status/log";
            AuditLogDto auditPayload = new AuditLogDto(
                    updatedTask.getId(),
                    updatedTask.getAssignedUserId(),
                    oldStatus,
                    updatedTask.getTaskStatus()
            );

            restTemplate.postForEntity(auditUrl, auditPayload, Void.class);
            System.out.println(">>> SUCCESS: Task modification audit sent to Port 8083.");
        } catch (Exception e) {
            System.err.println(">>> ERROR: Task update audit tracking failed: " + e.getMessage());
            e.printStackTrace();
        }

        return ResponseEntity.ok(updatedTask);

    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found with id: " + id));

        taskRepository.delete(task);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/internal/update-status/{id}")
    public ResponseEntity<Void> updateStatusInternal(@PathVariable Long id, @RequestParam String newStatus) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found"));
        task.setTaskStatus(newStatus);
        taskRepository.save(task);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Task> getTaskById(@PathVariable Long id){
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found"));
        return ResponseEntity.ok(task);
    }
}
