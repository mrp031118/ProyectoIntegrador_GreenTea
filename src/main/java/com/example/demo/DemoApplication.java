package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
public class DemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}
	/* 
	@Bean
    public CommandLineRunner demo(PasswordEncoder passwordEncoder) {
        
        return args -> {
            // Tu contraseña en texto plano
            String password = "dayhana";

            // Encriptarla
            String encodedPassword = passwordEncoder.encode(password);

            // Mostrar en consola
            System.out.println("Contraseña encriptada: " + encodedPassword);
        };
    }*/

}
