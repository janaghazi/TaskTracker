package com.example.TaskManagementSystem.controller;

import com.example.TaskManagementSystem.dto.TaskDTO;
import com.example.TaskManagementSystem.exception.ResourceNotFoundException;
import com.example.TaskManagementSystem.model.Task;
import com.example.TaskManagementSystem.model.TaskPriority;
import com.example.TaskManagementSystem.model.TaskStatus;
import com.example.TaskManagementSystem.model.User;
import com.example.TaskManagementSystem.service.TaskService;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.TaskManagementSystem.service.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/tasks")
public class TaskController {

    private final TaskService taskService;
    private final UserService userService;


    public TaskController(TaskService taskService, UserService userService) {
        this.taskService = taskService;
        this.userService = userService;
    }

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return  userService.findByUsername(username);
    }

    @PreAuthorize("hasAnyRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<TaskDTO>> getAllTasks() {
        return ResponseEntity.ok(taskService.getAllTasks());
    }

    @PostMapping("/create")
      public ResponseEntity<TaskDTO> createTask(@Valid @RequestBody Task task, @RequestParam Long userId) {
        User assignedTo = userService.getUserEntityById(userId, getCurrentUser());
        task.setAssignedTo(assignedTo);
        TaskDTO createdTask = taskService.createTask(task, getCurrentUser());
        return new ResponseEntity<>(createdTask, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskDTO> getTaskById(@PathVariable Long id) {
        return ResponseEntity.ok(taskService.getTaskById(id, getCurrentUser()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id, getCurrentUser());
        return ResponseEntity.ok("Task deleted successfully.");
    }

    @PutMapping("/{id}")
    public ResponseEntity<TaskDTO> updateTask(@PathVariable Long id, @RequestBody Task updatedTask) {
        return ResponseEntity.ok(taskService.updateTask(id, updatedTask, getCurrentUser()));
    }

    @PreAuthorize("hasAnyRole('ADMIN')")
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<TaskDTO>> getTasksByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(taskService.getTasksByUser(userId));
    }

    @GetMapping("/filter")
    public ResponseEntity<List<TaskDTO>> filterTasks(
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(required = false) TaskPriority priority,
            @RequestParam(required = false) String username,
            @RequestParam(defaultValue = "taskId") String sortBy,
            @RequestParam(defaultValue = "asc") String order) {
        return ResponseEntity.ok(taskService.filterTasks(status, priority, username, sortBy, order, getCurrentUser()));
    }
}
