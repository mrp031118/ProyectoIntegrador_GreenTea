package com.example.demo.controller;

import java.security.Principal;
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

import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.repository.RoleRepository;
import com.example.demo.repository.UserRepository;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.RequestParam;
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
    public String adminDashboard(Model model, Principal principal) {
        // Obtiene el username del usuario autenticado
        String username = principal.getName();

        // Busca en la BD al usuario por su username
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Concatenamos nombre + apellido
        String nombreCompleto = user.getNombre() + " " + user.getApellido();

        // Lo pasamos al modelo
        model.addAttribute("nombreCompleto", nombreCompleto);

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
        return "admin/usuariosAdmi";
    }

    // agregar nuevo usuario
    @PostMapping("/usuarios/agregar")
    public String agregarUsuario(@RequestParam String username,
            @RequestParam String contra,
            @RequestParam String nombre,
            @RequestParam String apellido,
            @RequestParam String telefono,
            @RequestParam String rol) {

        User user = new User();

        user.setUsername(username);
        user.setContra(passwordEncoder.encode(contra));
        user.setNombre(nombre);
        user.setApellido(apellido);
        user.setTelefono(telefono);
        user.setEnabled(true);

        Role roleEntity = roleRepository.findByNombre(rol)
                .orElseThrow(() -> new RuntimeException("Rol no encontrado: " + rol));
        user.setRoles(new HashSet<>());
        user.getRoles().add(roleEntity);

        userRepository.save(user);

        return "redirect:/admin/usuarios";
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
        return "admin/editarUsuario";
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
            return "admin/editarUsuario";
        }

        // Cargar el usuario original desde la BD
        User usuario = userRepository.findById(usuarioForm.getId())
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        // Actualizar campos
        usuario.setNombre(usuarioForm.getNombre());
        usuario.setApellido(usuarioForm.getApellido());
        usuario.setTelefono(usuarioForm.getTelefono());
        usuario.setUsername(usuarioForm.getUsername());

        //Obtener el rol seleccionado
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

    // Mostrar el formulario de cambio de contraseña
    @GetMapping("/usuarios/cambiarPassword/{id}")
    public String mostrarFormularioCambioContrasena(@PathVariable Long id, Model model) {
        User usuario = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        model.addAttribute("usuario", usuario);
        return "admin/cambiarContrasena"; // nueva plantilla HTML
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
    }

}
