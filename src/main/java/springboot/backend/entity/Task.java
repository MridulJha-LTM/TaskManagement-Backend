package springboot.backend.entity;

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
    private String taskStatus; // Open, In Progress, Done

    @Column(name = "assigned_user", nullable = false)
    private String assignedUser;

}
