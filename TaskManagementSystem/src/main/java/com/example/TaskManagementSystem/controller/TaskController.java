package com.example.TaskManagementSystem.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Sort;
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

import com.example.TaskManagementSystem.dto.TaskDTO;
import com.example.TaskManagementSystem.exception.AccessDeniedException;
import com.example.TaskManagementSystem.exception.ResourceNotFoundException;
import com.example.TaskManagementSystem.model.Role;
import com.example.TaskManagementSystem.model.Task;
import com.example.TaskManagementSystem.model.TaskPriority;
import com.example.TaskManagementSystem.model.TaskStatus;
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
    @GetMapping()
    public List<TaskDTO> getAllTasks() {
        return taskRepository.findAll()
                .stream()
                .map(TaskDTO::new)
                .toList();
    }

    @PostMapping("/create")
    public ResponseEntity<?> createTask(@Valid @RequestBody Task task, @RequestParam Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
        task.setAssignedTo(user);
        return new ResponseEntity<>(new TaskDTO(taskRepository.save(task)), HttpStatus.CREATED);
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

        return ResponseEntity.ok(new TaskDTO(task));
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
        return ResponseEntity.ok(new TaskDTO(taskRepository.save((task))));
    }

    @PreAuthorize("hasAnyRole('ADMIN')")
    @GetMapping("/user/{userId}")
    public List<TaskDTO> getTasksByUser(@PathVariable Long userId) {
        return taskRepository.findByAssignedToId(userId)
                .stream()
                .map(TaskDTO::new)
                .toList();
    }

    @GetMapping("/filter")
    public ResponseEntity<?> filterTasks(
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(required = false) TaskPriority priority,
            @RequestParam(required = false) String username,
            @RequestParam(defaultValue = "taskId") String sortBy,
            @RequestParam(defaultValue = "asc") String order
    ) {
        // Get current user
        User currentUser = getCurrentUser();

        // Prevent normal users from querying other users' tasks
        if (!currentUser.getRole().equals(Role.ADMIN)) {
            if (username != null && !username.equals(currentUser.getUsername())) {
                throw new AccessDeniedException("You are only allowed to view your own tasks.");
            }

            // If user didn’t specify username, force it to their own
            username = currentUser.getUsername();
        }

        // Sort setup
        Sort sort = order.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        // Filter logic
        List<Task> tasks;
        if (status != null && priority != null && username != null) {
            tasks = taskRepository.findByStatusAndPriorityAndAssignedToUsername(status, priority, username);
        } else if (status != null && priority != null) {
            tasks = taskRepository.findByStatusAndPriority(status, priority);
        } else if (status != null && username != null) {
            tasks = taskRepository.findByStatusAndAssignedToUsername(status, username);
        } else if (status != null) {
            tasks = taskRepository.findByStatus(status);
        } else if (priority != null) {
            tasks = taskRepository.findByPriority(priority);
        } else if (username != null) {
            tasks = taskRepository.findByAssignedToUsername(username);
        } else {
            tasks = taskRepository.findAll(sort);
        }

        return ResponseEntity.ok(tasks.stream().map(TaskDTO::new).toList());
    }

}
