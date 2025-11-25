package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.demo.entity.usuarios.User;
import com.example.demo.repository.usuarios.UserRepository;

@Controller
@RequestMapping("/user")
public class PassController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/cambiarPassword")
    public String guardarNuevaPassword(@RequestParam String contrasenaActual,
            @RequestParam String nuevaContrasena,
            @RequestParam String confirmarContrasena,
            RedirectAttributes redirectAttrs) {
        // Declarar auth antes del try
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        try {
            String username = auth.getName();
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            // Validaciones de contraseña
            if (!passwordEncoder.matches(contrasenaActual, user.getContra())) {
                throw new RuntimeException("La contraseña actual es incorrecta.");
            }
            if (!nuevaContrasena.equals(confirmarContrasena)) {
                throw new RuntimeException("Las nuevas contraseñas no coinciden.");
            }
            if (nuevaContrasena.length() < 8) {
                throw new RuntimeException("La nueva contraseña debe tener al menos 8 caracteres.");
            }

            user.setContra(passwordEncoder.encode(nuevaContrasena));
            userRepository.save(user);

            // Mensaje de éxito
            redirectAttrs.addFlashAttribute("showPasswordModal", true);
            redirectAttrs.addFlashAttribute("passwordModalMessage", "Contraseña cambiada exitosamente.");
            redirectAttrs.addFlashAttribute("isSuccess", true);

        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("showPasswordModal", true);
            redirectAttrs.addFlashAttribute("passwordModalMessage", "Error: " + e.getMessage());
            redirectAttrs.addFlashAttribute("isSuccess", false);
        }

        // Redirigir según el rol
        if (auth.getAuthorities().stream().anyMatch(r -> r.getAuthority().equals("ROLE_ADMIN"))) {
            return "redirect:/admin/dashboard";
        } else {
            return "redirect:/empleado/dashboard";
        }
    }

}
