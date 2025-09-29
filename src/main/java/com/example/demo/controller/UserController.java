package com.example.demo.controller;

import java.security.Principal;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;

@Controller
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/empleado/dashboard")
    public String userProfile() {

        return "empleado/dashboardEmple";
    }
    
    @GetMapping("/empleado/perfil")
    public String empleadoPerfil() {
        return "empleado/perfil"; 
    }
}
