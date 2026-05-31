package com.taskmanager.task.entity; // बिल्कुल सही पैकेज पाथ है

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "task_table")
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "task_name", nullable = false)
    private String taskName;

    private String description;

    @Column(name = "task_status", nullable = false)
    private String taskStatus; // Open(Default)

    @Column(name = "assigned_user_id")
    private Long assignedUserId;
}
