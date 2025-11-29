package com.example.demo.test;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class BcryptTest {
    public static void main(String[] args) {
        String rawPassword = "admin"; 
        String hashedPassword = "$2b$12$Nl9OojOATVG4ReR0dnnKD.572YxDxsmF6yTMuYuCb6FmXKAfhe4lC";

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        boolean matches = encoder.matches(rawPassword, hashedPassword);

        System.out.println("Coincide? " + matches);
    }
}
