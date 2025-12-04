package com.example.demo.controller;

import java.security.SecureRandom;
import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.demo.entity.usuarios.Role;
import com.example.demo.entity.usuarios.User;
import com.example.demo.repository.usuarios.RoleRepository;
import com.example.demo.repository.usuarios.UserRepository;
import com.example.demo.service.DashboardAdminService;
import com.example.demo.service.usuarios.CustomUserDetails;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private DashboardAdminService dashboardService;

    @GetMapping("/dashboard")
    public String adminDashboard(Model model) {

        model.addAttribute("ventasHoy", dashboardService.getVentasHoy());
        model.addAttribute("costoHoy", dashboardService.getCostoVentasHoy());
        model.addAttribute("margenHoy", dashboardService.getMargenBrutoHoy());
        model.addAttribute("stockCritico", dashboardService.getStockCritico());

        model.addAttribute("topProductos", dashboardService.getTopProductos());
        model.addAttribute("ventasSemana", dashboardService.getVentasUltimaSemana());
        model.addAttribute("vencimientos", dashboardService.getProximosVencimientos());

        return "admin/dashboardAdmi";
    }

    @GetMapping("/settings")
    public String adminSettings() {
        return "admin/settingsAdmi";
    }

    // Mostrar lista de usuarios
    @GetMapping("/usuarios")
    public String listarUsuarios(Model model) {
        model.addAttribute("usuarios", userRepository.findAll());
        model.addAttribute("roles", roleRepository.findAll());

        // Recuperar flash attributes para mostrar modales
        if (model.containsAttribute("showPasswordModal")) {
            model.addAttribute("showPasswordModal", model.getAttribute("showPasswordModal"));
            model.addAttribute("generatedUsername", model.getAttribute("generatedUsername"));
            model.addAttribute("generatedPassword", model.getAttribute("generatedPassword"));
        }
        if (model.containsAttribute("showErrorModal")) {
            model.addAttribute("showErrorModal", model.getAttribute("showErrorModal"));
            model.addAttribute("errorMessage", model.getAttribute("errorMessage"));
        }
        if (model.containsAttribute("successMessage")) {
            model.addAttribute("successMessage", model.getAttribute("successMessage"));
        }

        return "admin/usuario/usuariosAdmi";
    }

    // agregar nuevo usuario
    @PostMapping("/usuarios/agregar")
    public String agregarUsuario(@RequestParam String nombre,
            @RequestParam String apellido,
            @RequestParam String telefono,
            @RequestParam String rol,
            RedirectAttributes redirectAttributes) {

        try {

            if (nombre == null || nombre.trim().isEmpty()) {
                throw new RuntimeException("El nombre es requerido.");
            }
            if (apellido == null || apellido.trim().isEmpty()) {
                throw new RuntimeException("El apellido es requerido.");
            }

            // Generar username
            String[] nombreParts = nombre.trim().split("\\s+");
            String[] apellidoParts = apellido.trim().split("\\s+");

            String primerNombre = normalize(nombreParts[0]);

            StringBuilder apellidoShort = new StringBuilder();
            for (String ap : apellidoParts) {
                String apNorm = normalize(ap);
                apellidoShort.append(apNorm.length() >= 3 ? apNorm.substring(0, 3) : apNorm);
            }

            String baseUsername = primerNombre + "." + apellidoShort.toString() + "@gtcoffee.com";
            String username = baseUsername;

            int counter = 1;
            while (userRepository.findByUsername(username).isPresent()) {
                username = primerNombre + "." + apellidoShort.toString() + counter + "@gtcoffee.com";
                counter++;
            }

            // Generar contraseña
            String generatedPassword = generateRandomPassword(8);

            // Crear usuario
            User user = new User();
            user.setUsername(username);
            user.setContra(passwordEncoder.encode(generatedPassword));
            user.setNombre(nombre);
            user.setApellido(apellido);
            user.setTelefono(telefono);
            user.setEnabled(true);

            Role roleEntity = roleRepository.findByNombre(rol)
                    .orElseThrow(() -> new RuntimeException("Rol no encontrado: " + rol));

            Set<Role> roles = new HashSet<>();
            roles.add(roleEntity);
            user.setRoles(roles);

            userRepository.save(user);

            // Modal de creación con contraseña
            redirectAttributes.addFlashAttribute("showPasswordModal", true);
            redirectAttributes.addFlashAttribute("generatedUsername", username);
            redirectAttributes.addFlashAttribute("generatedPassword", generatedPassword);

            redirectAttributes.addFlashAttribute("mensaje", "Usuario creado correctamente.");
            redirectAttributes.addFlashAttribute("tipo", "success");

        } catch (Exception e) {

            redirectAttributes.addFlashAttribute("showErrorModal", true);
            redirectAttributes.addFlashAttribute("errorMessage", "Error al crear el usuario: " + e.getMessage());

            redirectAttributes.addFlashAttribute("mensaje", "Error al crear usuario.");
            redirectAttributes.addFlashAttribute("tipo", "error");
        }

        return "redirect:/admin/usuarios";
    }

    // Método auxiliar para generar contraseña aleatoria
    private String generateRandomPassword(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    // normalizar caracteres
    private String normalize(String input) {
        if (input == null)
            return "";
        // Convertir a minúsculas
        String normalized = input.toLowerCase()
                // Reemplazar caracteres con tilde
                .replaceAll("[áàäâ]", "a")
                .replaceAll("[éèëê]", "e")
                .replaceAll("[íìïî]", "i")
                .replaceAll("[óòöô]", "o")
                .replaceAll("[úùüû]", "u")
                // Reemplazar ñ por n
                .replaceAll("ñ", "n")
                // Eliminar caracteres no alfanuméricos
                .replaceAll("[^a-z0-9]", "");
        return normalized;
    }

    // Activar / desactivar usuario
    @PostMapping("/usuarios/toggleEstado/{id}")
    public String toggleEstado(@PathVariable Long id, RedirectAttributes redirectAttributes) {

        // Obtener usuario autenticado
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails loggedUser = (CustomUserDetails) auth.getPrincipal();
        Long loggedUserId = loggedUser.getUser().getId();

        // Evitar desactivar usuario logueado
        if (id.equals(loggedUserId)) {
            redirectAttributes.addFlashAttribute("mensaje", "No puedes desactivar tu propio usuario.");
            redirectAttributes.addFlashAttribute("tipo", "error");
            return "redirect:/admin/usuarios";
        }

        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        user.setEnabled(!user.isEnabled());
        userRepository.save(user);

        redirectAttributes.addFlashAttribute("mensaje",
                user.isEnabled() ? "Usuario activado correctamente." : "Usuario desactivado correctamente.");
        redirectAttributes.addFlashAttribute("tipo", "success");

        return "redirect:/admin/usuarios";
    }

    // Eliminar usuario
    @PostMapping("/usuarios/eliminar/{id}")
    public String eliminarUsuario(@PathVariable Long id, RedirectAttributes redirectAttributes) {

        userRepository.deleteById(id);

        redirectAttributes.addFlashAttribute("mensaje", "Usuario eliminado correctamente.");
        redirectAttributes.addFlashAttribute("tipo", "success");

        return "redirect:/admin/usuarios";
    }

    // Editar Usuario
    @GetMapping("/usuarios/editar/{id}")
    public String editarUsuarioForm(@PathVariable Long id, Model model) {

        User usuario = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        model.addAttribute("usuario", usuario);
        model.addAttribute("roles", roleRepository.findAll());

        return "admin/usuario/editarUsuario";
    }

    @PostMapping("/usuarios/editar")
    public String guardarUsuarioEditado(
            @Valid @ModelAttribute("usuario") User usuarioForm,
            BindingResult result,
            @RequestParam("rol") String rolNombre,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            model.addAttribute("roles", roleRepository.findAll());
            return "admin/usuario/editarUsuario";
        }

        User usuario = userRepository.findById(usuarioForm.getId())
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        usuario.setNombre(usuarioForm.getNombre());
        usuario.setApellido(usuarioForm.getApellido());
        usuario.setTelefono(usuarioForm.getTelefono());

        Role rolSeleccionado = roleRepository.findByNombre(rolNombre)
                .orElseThrow(() -> new IllegalArgumentException("Rol no encontrado"));

        Set<Role> roles = new HashSet<>();
        roles.add(rolSeleccionado);
        usuario.setRoles(roles);

        userRepository.save(usuario);

        redirectAttributes.addFlashAttribute("mensaje", "Usuario actualizado correctamente.");
        redirectAttributes.addFlashAttribute("tipo", "success");

        return "redirect:/admin/usuarios";
    }

}
