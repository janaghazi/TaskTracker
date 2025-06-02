package com.example.TaskManagementSystem.controller.view;

import com.example.TaskManagementSystem.model.Task;
import com.example.TaskManagementSystem.model.TaskPriority;
import com.example.TaskManagementSystem.model.TaskStatus;
import com.example.TaskManagementSystem.model.User;
import com.example.TaskManagementSystem.service.TaskService;
import com.example.TaskManagementSystem.service.UserService;

import jakarta.validation.Valid;

import com.example.TaskManagementSystem.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/view/tasks")
public class TaskViewController {

    private final TaskService taskService;
    private final UserService userService;

    @Autowired
    public TaskViewController(TaskService taskService, UserService userService) {
        this.taskService = taskService;
        this.userService = userService;
    }

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userService.findByUsername(username);
    }

    @PreAuthorize("hasAnyRole('ADMIN')")
    @GetMapping
    public String viewTaskPages(Model model) {
        List<Task> tasks = taskService.getTasksByUserEntity(null); // null userId to get all tasks
        model.addAttribute("tasks", tasks);
        return "task-list";
    }

    @GetMapping("/my-tasks")
    public String viewMyTasks(Model model) {
        User currentUser = getCurrentUser();
        List<Task> tasks = taskService.getTasksByUserEntity(currentUser.getUserId());
        model.addAttribute("tasks", tasks);
        return "task-list";
    }

    @GetMapping("/new")
    public String showTaskForm(Model model) {
        model.addAttribute("task", new Task());
        model.addAttribute("users", userService.findAllUsers());
        model.addAttribute("statuses", TaskStatus.values());
        model.addAttribute("priorities", TaskPriority.values());
        return "task-form";
    }

    @PostMapping("/create")
    public String createTask(@Valid @ModelAttribute("task") Task task, BindingResult result,
            @RequestParam("userId") Long userId, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("users", userService.findAllUsers());
            model.addAttribute("statuses", TaskStatus.values());
            model.addAttribute("priorities", TaskPriority.values());
            return "task-form";
        }
        User assignedTo = userService.getUserEntityById(userId, getCurrentUser());
        task.setAssignedTo(assignedTo);
        taskService.createTaskEntity(task, getCurrentUser());
        return "redirect:/view/tasks";
    }

    @GetMapping("/{id}")
    public String viewTask(@PathVariable Long id, Model model) {
        Task task = taskService.getTaskEntityById(id, getCurrentUser());
        model.addAttribute("task", task);
        return "task-details";
    }

    @GetMapping("/{id}/edit")
    public String showEditTaskForm(@PathVariable Long id, Model model) {
        Task task = taskService.getTaskEntityById(id, getCurrentUser());
        model.addAttribute("task", task);
        model.addAttribute("statuses", TaskStatus.values());
        model.addAttribute("priorities", TaskPriority.values());
        return "task-edit-form";
    }

    @PostMapping("/{id}/update")
    public String updateTask(@PathVariable Long id, @Valid @ModelAttribute("task") Task task,
            BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("statuses", TaskStatus.values());
            model.addAttribute("priorities", TaskPriority.values());
            return "task-edit-form";
        }
        taskService.updateTaskEntity(id, task, getCurrentUser());
        return "redirect:/view/tasks";
    }
    

    @PostMapping("/{id}/delete")
    public String deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id, getCurrentUser());
        return "redirect:/view/tasks";
    }

    @GetMapping("/filter/results")
    public String filterTasks(@RequestParam(required = false) String title,
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(required = false) TaskPriority priority,
            Model model) {
        List<Task> tasks = taskService.getTasksByUserEntity(null)
                .stream()
                .filter(task -> title == null || task.getTitle().toLowerCase().contains(title.toLowerCase()))
                .filter(task -> status == null || task.getStatus().equals(status))
                .filter(task -> priority == null || task.getPriority().equals(priority))
                .collect(Collectors.toList());
        model.addAttribute("tasks", tasks);
        return "task-list";
    }
}
