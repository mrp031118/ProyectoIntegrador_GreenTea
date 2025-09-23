package com.example.demo.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;


@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private final UserDetailsService userDetailsService;

    public SecurityConfig(UserDetailsService userDetailsService){
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain (HttpSecurity http) throws Exception{
        http
            .csrf(csrf -> csrf.disable()) //desactiva CSRF al usar API REST
            .authorizeRequests(auth -> auth
                .requestMatchers("/login","/prueba-login", "/public/**").permitAll()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/empleado/**").hasRole("USER")
                .anyRequest().authenticated()
                )
                .formLogin(form -> form
                    .loginPage("/login") // URL GET para mostrar formulario
                    .loginProcessingUrl("/perform_login") // URL POST para procesar login
                    .defaultSuccessUrl("/home", true) // si el login es exitoso, va al home
                    .failureHandler((request, response, exception) -> {
                        String errorMessage = "Credenciales inválidas"; //mensaje generico
                        if (exception instanceof org.springframework.security.authentication.DisabledException) {
                            errorMessage = "Usuario Desactivado";
                        }
                        //Guarda el mensaje en la sesión (flash attribute)
                        request.getSession().setAttribute("errorMsg", errorMessage);
                        //redirige con un parametro seguro
                        response.sendRedirect("/login?error=true");

                    })                    
                    .permitAll())
                .logout(logout -> logout.permitAll());
            
        return http.build();
    }
    
}
