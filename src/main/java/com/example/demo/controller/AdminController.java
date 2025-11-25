package com.example.demo.controller;

import java.security.SecureRandom;
import java.util.HashSet;
import java.util.Set;

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

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminController(UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/dashboard")
    public String adminDashboard() {
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
            // Validar que nombre y apellido no estén vacíos o solo espacios
            if (nombre == null || nombre.trim().isEmpty()) {
                throw new RuntimeException("El nombre es requerido.");
            }
            if (apellido == null || apellido.trim().isEmpty()) {
                throw new RuntimeException("El apellido es requerido.");
            }

            // Generar username automáticamente (mejorado: primer nombre + primeros 3 chars
            // de cada apellido)
            String[] nombreParts = nombre.trim().split("\\s+");
            String[] apellidoParts = apellido.trim().split("\\s+");

            // Normalizar primer nombre
            String primerNombre = normalize(nombreParts[0]);

            // Normalizar y acortar apellidos
            StringBuilder apellidoShort = new StringBuilder();
            for (String ap : apellidoParts) {
                String apNorm = normalize(ap);
                if (apNorm.length() >= 3) {
                    apellidoShort.append(apNorm.substring(0, 3));
                } else {
                    apellidoShort.append(apNorm);
                }
            }

            // Crear username base
            String baseUsername = primerNombre + "." + apellidoShort.toString() + "@gtcoffee.com";

            // Evitar duplicados
            String username = baseUsername;
            int counter = 1;
            while (userRepository.findByUsername(username).isPresent()) {
                username = primerNombre + "." + apellidoShort.toString() + counter + "@gtcoffee.com";
                counter++;
            }

            // Generar contraseña aleatoria (8 caracteres alfanuméricos con símbolos)
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
            user.setRoles(new HashSet<>());
            user.getRoles().add(roleEntity);

            userRepository.save(user);

            // Agregar flash attributes para mostrar modal de contraseña
            redirectAttributes.addFlashAttribute("showPasswordModal", true);
            redirectAttributes.addFlashAttribute("generatedUsername", username);
            redirectAttributes.addFlashAttribute("generatedPassword", generatedPassword);

        } catch (Exception e) {
            // Agregar flash attributes para mostrar modal de error
            redirectAttributes.addFlashAttribute("showErrorModal", true);
            redirectAttributes.addFlashAttribute("errorMessage", "Error al crear el usuario: " + e.getMessage());
        }

        // Redirigir a GET /admin/usuarios (PRG)
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

    @PostMapping("/usuarios/toggleEstado/{id}")
    public String toggleEstado(@PathVariable Long id) {
        User user = userRepository.findById(id).orElseThrow();
        user.setEnabled(!user.isEnabled());
        userRepository.save(user);

        return "redirect:/admin/usuarios";
    }

    // Eliminar usuario
    @PostMapping("/usuarios/eliminar/{id}")
    public String eliminarUsuario(@PathVariable Long id) {
        userRepository.deleteById(id);
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
            Model model) {

        if (result.hasErrors()) {
            // Si hay errores de validación, recargar el formulario con los roles
            model.addAttribute("roles", roleRepository.findAll());
            return "admin/usuario/editarUsuario";
        }

        // Cargar el usuario original desde la BD
        User usuario = userRepository.findById(usuarioForm.getId())
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        // Actualizar campos
        usuario.setNombre(usuarioForm.getNombre());
        usuario.setApellido(usuarioForm.getApellido());
        usuario.setTelefono(usuarioForm.getTelefono());

        // Obtener el rol seleccionado
        Role rolSeleccionado = roleRepository.findByNombre(rolNombre)
                .orElseThrow(() -> new IllegalArgumentException("Rol no encontrado"));

        // Asignar rol en un Set mutable
        Set<Role> roles = new HashSet<>();
        roles.add(rolSeleccionado);
        usuario.setRoles(roles);

        // Guardar usuario actualizado
        userRepository.save(usuario);

        return "redirect:/admin/usuarios";
    }

    /* 
    // Mostrar el formulario de cambio de contraseña
    @GetMapping("/usuarios/cambiarPassword/{id}")
    public String mostrarFormularioCambioContrasena(@PathVariable Long id, Model model) {
        User usuario = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        model.addAttribute("usuario", usuario);
        return "admin/usuario/cambiarContrasena"; // nueva plantilla HTML
    }

    // Guardar la nueva contraseña
    @PostMapping("/usuarios/cambiarPassword")
    public String guardarNuevaContrasena(@RequestParam Long id,
            @RequestParam String nuevaContrasena) {
        User usuario = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        // Encriptar con BCrypt
        String passEncriptada = passwordEncoder.encode(nuevaContrasena);
        usuario.setContra(passEncriptada);

        userRepository.save(usuario);
        return "redirect:/admin/usuarios";
    }*/

}
