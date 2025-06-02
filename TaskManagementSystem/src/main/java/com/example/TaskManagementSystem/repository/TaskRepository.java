package com.example.TaskManagementSystem.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.TaskManagementSystem.model.Task;
import com.example.TaskManagementSystem.model.TaskPriority;
import com.example.TaskManagementSystem.model.TaskStatus;

public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByAssignedToUserId(Long userId);

    List<Task> findByStatus(TaskStatus status);

    List<Task> findByPriority(TaskPriority priority);

    List<Task> findByAssignedToUsername(String username);

    List<Task> findByStatusAndPriority(TaskStatus status, TaskPriority priority);

    List<Task> findByStatusAndAssignedToUsername(TaskStatus status, String username);

    List<Task> findByStatusAndPriorityAndAssignedToUsername(TaskStatus status, TaskPriority priority, String username);

}
