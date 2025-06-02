package com.example.TaskManagementSystem.service;

import com.example.TaskManagementSystem.dto.UserDTO;
import com.example.TaskManagementSystem.exception.AccessDeniedException;
import com.example.TaskManagementSystem.exception.ResourceNotFoundException;
import com.example.TaskManagementSystem.model.Role;
import com.example.TaskManagementSystem.model.User;
import com.example.TaskManagementSystem.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
// service to handle the businnes logic
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
    }

    public List<User> findAllUsers() {
    return userRepository.findAll();
}

    // retrieve all users
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(UserDTO::new)
                .collect(Collectors.toList());
    }

    // get user by their id... REST
    public UserDTO getUserById(Long id, User currentUser) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));
        if (!currentUser.getRole().equals(Role.ADMIN) && !currentUser.getUserId().equals(user.getUserId())) {
            throw new AccessDeniedException("You are not authorized to view this user.");
        }
        return new UserDTO(user);
    }

    // get user by their id... controller
    public User getUserEntityById(Long id, User currentUser) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));
        if (!currentUser.getRole().equals(Role.ADMIN) && !currentUser.getUserId().equals(user.getUserId())) {
            throw new AccessDeniedException("You are not authorized to view this user.");
        }
        return user;
    }

    // Register User... REST
    public UserDTO registerUser(User user) {
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username already exists.");
        }
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already exists.");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(Role.USER);
        return new UserDTO(userRepository.save(user));
    }

    // Register User... Controller
    public User registerUserEntity(User user) {
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username already exists.");
        }
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already exists.");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(Role.USER);
        return userRepository.save(user);
    }
}
