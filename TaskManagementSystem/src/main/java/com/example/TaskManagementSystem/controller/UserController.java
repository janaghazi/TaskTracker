package com.example.TaskManagementSystem.controller;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.TaskManagementSystem.dto.UserDTO;
import com.example.TaskManagementSystem.exception.AccessDeniedException;
import com.example.TaskManagementSystem.exception.ResourceNotFoundException;
import com.example.TaskManagementSystem.model.Role;
import com.example.TaskManagementSystem.model.User;
import com.example.TaskManagementSystem.repository.UserRepository;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // helper method to get the current user
    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Logged-in user not found"));
    }

    // only an admin has authorization to see all taslks
    @PreAuthorize("hasAnyRole('ADMIN')")
    @GetMapping("")
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(UserDTO::new)
                .toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));

        User currentUser = getCurrentUser();
        if (currentUser.getRole().equals(Role.ADMIN)
                || currentUser.getUserId().equals(user.getUserId())) {
            return ResponseEntity.ok(new UserDTO(user));
        }

        throw new AccessDeniedException("You are not authorized to view this user.");
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody User user) {
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Username already exists.");
        }

        user.setPassword(new BCryptPasswordEncoder().encode(user.getPassword()));
        user.setRole(Role.USER); // default role
        return new ResponseEntity<>(new UserDTO(userRepository.save(user)), HttpStatus.CREATED);
    }
}
