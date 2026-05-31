package com.taskmanager.taskstatus.repository;

import com.taskmanager.taskstatus.entity.TaskAudit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TaskAuditRepository extends JpaRepository<TaskAudit, Long> {
}
