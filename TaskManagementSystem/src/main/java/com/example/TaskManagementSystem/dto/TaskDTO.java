package com.example.TaskManagementSystem.dto;

import com.example.TaskManagementSystem.model.Task;
import com.example.TaskManagementSystem.model.TaskPriority;
import com.example.TaskManagementSystem.model.TaskStatus;

public class TaskDTO {
    private Long taskId;
    private String title;
    private String description;
    private TaskStatus status;
    private TaskPriority priority;
    private String assignedToUsername;

    public TaskDTO() {}

    public TaskDTO(Task task) {
        this.taskId = task.getTaskId();
        this.title = task.getTitle();
        this.description = task.getDescription();
        this.status = task.getStatus();
        this.priority = task.getPriority();
        this.assignedToUsername = task.getAssignedTo().getUsername();
    }

    public String getAssignedToUsername() {
        return assignedToUsername;
    }

    public void setAssignedToUsername(String assignedToUsername) {
        this.assignedToUsername = assignedToUsername;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public TaskPriority getPriority() {
        return priority;
    }

    public void setPriority(TaskPriority priority) {
        this.priority = priority;
    }


    
}
