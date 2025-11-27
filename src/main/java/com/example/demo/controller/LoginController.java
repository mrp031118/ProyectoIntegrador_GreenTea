package com.example.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpServletRequest;

@Controller
public class LoginController {

    // verificar si las credenciales son correctas o el usuario este validado
    @GetMapping("/login")
    public String login(@RequestParam(value = "error", required = false) String error,
            Model model,
            HttpServletRequest request) {

        if (error != null) {
            // Recuperar mensaje guardado por Spring Security o por un filtro personalizado
            String errorMsg = (String) request.getSession().getAttribute("errorMsg");

            // Evita null
            if (errorMsg == null) {
                errorMsg = "Credenciales inválidas";
            }

            model.addAttribute("errorMsg", errorMsg);

            // Limpiar la sesión para que no quede guardado
            request.getSession().removeAttribute("errorMsg");
        }

        return "login";
    }

}
