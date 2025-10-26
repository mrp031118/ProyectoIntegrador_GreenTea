package com.example.demo.service.usuarios;

import java.util.HashSet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.demo.entity.usuarios.User;
import com.example.demo.repository.usuarios.UserRepository;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public User registerNewUser(String username, String contra){
        User user = new User();
        user.setUsername(username);
        user.setContra(passwordEncoder.encode(contra));
        user.setRoles(new HashSet<>()); //asignar roles segun sea necesita
        return userRepository.save(user);
    }
}
