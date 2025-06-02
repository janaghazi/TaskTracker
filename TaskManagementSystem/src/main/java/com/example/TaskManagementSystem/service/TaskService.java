package com.example.TaskManagementSystem.service;

import com.example.TaskManagementSystem.dto.TaskDTO;
import com.example.TaskManagementSystem.exception.AccessDeniedException;
import com.example.TaskManagementSystem.exception.ResourceNotFoundException;
import com.example.TaskManagementSystem.model.Task;
import com.example.TaskManagementSystem.model.TaskPriority;
import com.example.TaskManagementSystem.model.TaskStatus;
import com.example.TaskManagementSystem.model.User;
import com.example.TaskManagementSystem.model.Role;
import com.example.TaskManagementSystem.repository.TaskRepository;
import com.example.TaskManagementSystem.repository.UserRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;


@Service
// service to handle the businnes logic
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    public TaskService(TaskRepository taskRepository, UserRepository userRepository) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
    }

    // retrieving all tasks
    public List<TaskDTO> getAllTasks() {
        return taskRepository.findAll()
                .stream()
                .map(TaskDTO::new)
                .collect(Collectors.toList());
    }

    // creating a new task... REST
      public TaskDTO createTask(Task task, User currentUser) {
        User assignedTo = userRepository.findById(task.getAssignedTo().getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + task.getAssignedTo().getUserId()));
        task.setAssignedTo(assignedTo);
        return new TaskDTO(taskRepository.save(task));
    }

    // creating a new task... controller
    public Task createTaskEntity(Task task, User currentUser) {
        User assignedTo = userRepository.findById(task.getAssignedTo().getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + task.getAssignedTo().getUserId()));
        task.setAssignedTo(assignedTo);
        return taskRepository.save(task);
    }

    // get task by ID.... REst Controllers
    public TaskDTO getTaskById(Long id, User currentUser) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with ID: " + id));
        if (!currentUser.getRole().equals(Role.ADMIN) && !task.getAssignedTo().getUserId().equals(currentUser.getUserId())) {
            throw new AccessDeniedException("You are not authorized to view this task.");
        }
        return new TaskDTO(task);
    }

    // get task by ID....  Controllers
    public Task getTaskEntityById(Long id, User currentUser) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with ID: " + id));
        if (!currentUser.getRole().equals(Role.ADMIN) && !task.getAssignedTo().getUserId().equals(currentUser.getUserId())) {
            throw new AccessDeniedException("You are not authorized to view this task.");
        }
        return task;
    }

    // delete task..... for all controllers
    public void deleteTask(Long id, User currentUser) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with ID: " + id));
        if (!currentUser.getRole().equals(Role.ADMIN) && !task.getAssignedTo().getUserId().equals(currentUser.getUserId())) {
            throw new AccessDeniedException("You cannot delete this task.");
        }
        taskRepository.deleteById(id);
    }

    // update certain task..... REST controllers
    public TaskDTO updateTask(Long id, Task updatedTask, User currentUser) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with ID: " + id));
        if (!currentUser.getRole().equals(Role.ADMIN) && !task.getAssignedTo().getUserId().equals(currentUser.getUserId())) {
            throw new AccessDeniedException("You are not authorized to update this task.");
        }
        task.setTitle(updatedTask.getTitle());
        task.setDescription(updatedTask.getDescription());
        task.setStatus(updatedTask.getStatus());
        task.setPriority(updatedTask.getPriority());
        return new TaskDTO(taskRepository.save(task));
    }

    // update certain task.... Controllers
    public Task updateTaskEntity(Long id, Task updatedTask, User currentUser) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with ID: " + id));
        if (!currentUser.getRole().equals(Role.ADMIN) && !task.getAssignedTo().getUserId().equals(currentUser.getUserId())) {
            throw new AccessDeniedException("You are not authorized to update this task.");
        }
        task.setTitle(updatedTask.getTitle());
        task.setDescription(updatedTask.getDescription());
        task.setStatus(updatedTask.getStatus());
        task.setPriority(updatedTask.getPriority());
        return taskRepository.save(task);
    }


    // get all tasks of a certain user.... REST controllers
    public List<TaskDTO> getTasksByUser(Long userId) {
        return taskRepository.findByAssignedToUserId(userId)
                .stream()
                .map(TaskDTO::new)
                .collect(Collectors.toList());
    }

    // get all tasks of a certain user... controllers
    public List<Task> getTasksByUserEntity(Long userId) {
        return taskRepository.findByAssignedToUserId(userId);
    }

    // filter task logic.... for REST controlles
    public List<TaskDTO> filterTasks(TaskStatus status, TaskPriority priority, String username, String sortBy, String order, User currentUser) {
        if (!currentUser.getRole().equals(Role.ADMIN)) {
            if (username != null && !username.equals(currentUser.getUsername())) {
                throw new AccessDeniedException("You are only allowed to view your own tasks.");
            }
            username = currentUser.getUsername();
        }

        Sort sort = order.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
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

        return tasks.stream().map(TaskDTO::new).collect(Collectors.toList());
    }

    // filter task logic.... for controlles
    public List<Task> filterTasksEntity(TaskStatus status, TaskPriority priority, String username, User currentUser) {
        if (!currentUser.getRole().equals(Role.ADMIN)) {
            if (username != null && !username.equals(currentUser.getUsername())) {
                throw new AccessDeniedException("You are only allowed to view your own tasks.");
            }
            username = currentUser.getUsername();
        }

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
            tasks = taskRepository.findAll();
        }

        return tasks;
    }
}