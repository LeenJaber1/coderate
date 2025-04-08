package com.coderate.backend.service;

import com.coderate.backend.exceptions.UserAlreadyExists;
import com.coderate.backend.model.User;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface UserService extends UserDetailsService {
    User getUserByEmail(String email) throws Exception;
    User getUserByUsername(String username) throws Exception;
    void createUser(String displayName, String username, String password, String email) throws Exception;
    void updateUser(String username, User user) throws Exception;
}
