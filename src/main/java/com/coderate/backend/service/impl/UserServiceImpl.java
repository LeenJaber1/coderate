package com.coderate.backend.service.impl;

import com.coderate.backend.exceptions.UserAlreadyExists;
import com.coderate.backend.model.User;
import com.coderate.backend.repository.UserRepository;
import com.coderate.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserRepository userRepository;


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return this.userRepository.findByUsernameOrEmail(username, username).orElseThrow(() -> new UsernameNotFoundException("User doesn't exist"));
    }

    @Override
    public User getUserByEmail(String email) throws Exception {
        return this.userRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("User doesn't exist"));
    }

    @Override
    public User getUserByUsername(String username) {
        return this.userRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException("user doesn't exist"));
    }

    @Override
    public void createUser(String displayName, String username, String password, String email) throws UserAlreadyExists {
        if (userRepository.findByEmail(email).isPresent() || userRepository.findByUsername(username).isPresent()) {
            throw new UserAlreadyExists("user already exists");
        }
        //do this method before creating user
        //String hashedPassword = passwordEncoder.encode(password);
        User user = new User(displayName, username, password, email);
        userRepository.save(user);
    }

    @Override
    public void createUser(String displayName, String username, String email) {
        User user = new User(displayName, username, email);
        userRepository.save(user);
    }

    @Override
    public void updateUser(String username, User user) throws UserAlreadyExists {
        User actualUser = this.getUserByUsername(username);
        if (user.getDisplayName() != null && !actualUser.getDisplayName().equals(user.getDisplayName())) {
            actualUser.setDisplayName(user.getDisplayName());
        }
        if (user.getUsername() != null && !actualUser.getUsername().equals(user.getUsername())) {
            if (userRepository.findByUsername(user.getUsername()).isPresent()) {
                throw new UserAlreadyExists("username already exists");
            }
            actualUser.setUsername(user.getUsername());
        }
        if (user.getEmail() != null && !actualUser.getEmail().equals(user.getEmail())) {
            if (userRepository.findByEmail(user.getEmail()).isPresent()) {
                throw new UserAlreadyExists("email already exists");
            }
            actualUser.setEmail(user.getEmail());
        }
        userRepository.save(actualUser);
    }
}
