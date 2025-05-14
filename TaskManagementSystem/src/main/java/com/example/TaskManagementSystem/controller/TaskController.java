package com.example.TaskManagementSystem.controller;

import java.util.List;
import java.util.Optional;

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

import com.example.TaskManagementSystem.exception.AccessDeniedException;
import com.example.TaskManagementSystem.exception.ResourceNotFoundException;
import com.example.TaskManagementSystem.model.Role;
import com.example.TaskManagementSystem.model.Task;
import com.example.TaskManagementSystem.model.User;
import com.example.TaskManagementSystem.repository.TaskRepository;
import com.example.TaskManagementSystem.repository.UserRepository;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/tasks")
public class TaskController {

    // Constructer dependency injection
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    public TaskController(TaskRepository taskRepository, UserRepository userRepository) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
    }

    // helper method to get current user
    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username).orElseThrow(()
                -> new UsernameNotFoundException("Logged-in user not found"));
    }

    // only an admin has access to all tasks
    @PreAuthorize("hasAnyRole('ADMIN')")
    @GetMapping("/allTask")
    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }

    @PostMapping("/create")
    public ResponseEntity<?> createTask(@Valid @RequestBody Task task, @RequestParam Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
        task.setAssignedTo(user);
        return new ResponseEntity<>(taskRepository.save(task), HttpStatus.CREATED);
    }


    @GetMapping("/{id}")
    public ResponseEntity<?> getTaskById(@PathVariable Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with ID: " + id));

        User currentUser = getCurrentUser();
        if (!currentUser.getRole().equals(Role.ADMIN)
                && !task.getAssignedTo().getUserId().equals(currentUser.getUserId())) {
            throw new AccessDeniedException("You are not authorized to view this task.");
        }

        return ResponseEntity.ok(task);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTask(@PathVariable Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with ID: " + id));

        User currentUser = getCurrentUser();
        if (!currentUser.getRole().equals(Role.ADMIN)
                && !task.getAssignedTo().getUserId().equals(currentUser.getUserId())) {
            throw new AccessDeniedException("You cannot delete this task.");
        }

        taskRepository.deleteById(id);
        return ResponseEntity.ok("Task deleted successfully.");
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateTask(@PathVariable Long id, @RequestBody Task updatedTask) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with ID: " + id));

        User currentUser = getCurrentUser();
        if (!currentUser.getRole().equals(Role.ADMIN)
                && !task.getAssignedTo().getUserId().equals(currentUser.getUserId())) {
            throw new AccessDeniedException("You cannot update this task.");
        }

        task.setTitle(updatedTask.getTitle());
        task.setDescription(updatedTask.getDescription());
        task.setStatus(updatedTask.getStatus());
        return ResponseEntity.ok(taskRepository.save(task));
    }

    @PreAuthorize("hasAnyRole('ADMIN')")
    @GetMapping("/{userId}")
    public List<Task> getTasksByUser(@PathVariable Long userId) {
        return taskRepository.findByAssignedToId(userId);
    }

}
