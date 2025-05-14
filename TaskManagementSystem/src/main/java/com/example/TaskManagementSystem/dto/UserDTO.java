package com.example.TaskManagementSystem.dto;

import com.example.TaskManagementSystem.model.Role;
import com.example.TaskManagementSystem.model.User;

public class UserDTO {
    private Long userId;
    private String username;
    private String email;
    private Role role;

    public UserDTO() {}

    public UserDTO(User user) {
        this.userId = user.getUserId();
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.role = user.getRole();
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

}
