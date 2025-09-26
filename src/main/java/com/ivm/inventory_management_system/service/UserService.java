package com.ivm.inventory_management_system.service;

import com.ivm.inventory_management_system.entity.User;
import com.ivm.inventory_management_system.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User registerUser(User user) {
        if(userRepository.findByUsername(user.getUsername()).isPresent()) {
            throw new RuntimeException("Username: " +user.getUsername()+" is already taken. Please choose another one.");
        }
        return userRepository.save(user);
    }

    public User updateUser(User user) {
        return userRepository.save(user);
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<User> findUserByName(String username) {
        return userRepository.findByUsername(username);
    }

}
