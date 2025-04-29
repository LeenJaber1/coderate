package com.coderate.backend.controller;

import com.coderate.backend.model.User;
import com.coderate.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/user")
@RestController
public class UserController {
    @Autowired
    private UserService userService;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/create")
    public ResponseEntity<?> createUser(@RequestParam("displayName") String displayName,
                                        @RequestParam("username") String username,
                                        @RequestParam("email") String email,
                                        @RequestParam("password") String password) throws Exception {
        String hashedPassword = passwordEncoder.encode(password);
        userService.createUser(displayName, username, hashedPassword, email);
        return ResponseEntity.ok("User created");
    }

    @GetMapping("/")
    public String test(){
        return "success";
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@AuthenticationPrincipal User loginUser) {
        return ResponseEntity.ok(loginUser);
    }
}
