package com.example.TaskManagementSystem.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.TaskManagementSystem.model.User;

public interface UserRepository extends JpaRepository<User, Long> {

 Optional<User> findByUsername(String username);

}
