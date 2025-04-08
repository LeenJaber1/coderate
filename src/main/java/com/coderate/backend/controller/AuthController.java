package com.coderate.backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
public class AuthController {
    @GetMapping("/")
    public String test(){
        return "Hello";
    }

    @GetMapping("/principal")
    public Principal principal(Principal user){
        return user;
    }
}
