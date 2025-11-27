package com.example.demo.controller.produccion;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.demo.entity.produccion.Produccion;
import com.example.demo.entity.productos.Producto;
import com.example.demo.entity.usuarios.User;
import com.example.demo.repository.productos.ProductoRepository;
import com.example.demo.repository.ventaa.ProduccionRepository;
import com.example.demo.service.usuarios.CustomUserDetails;
import com.example.demo.service.venta.ProduccionService;

@Controller
@RequestMapping("/empleado")
public class ProduccionDiariaController {

    @Autowired
    private ProduccionService produccionService;
    @Autowired
    private ProduccionRepository produccionRepository;
    @Autowired
    private ProductoRepository productoRepository;

    // Vista principal de producción
    @GetMapping("/produccion")
    public String listarProduccion(Model model) {

        try {
            // Obtener historial
            List<Produccion> producciones = produccionRepository.findAll();

            // Obtener productos ELABORADOS
            List<Producto> productos = productoRepository.findAll().stream()
                    .filter(p -> "ELABORADO".equalsIgnoreCase(p.getCategoria().getTipoControl()))
                    .collect(Collectors.toList());

            model.addAttribute("producciones", producciones);
            model.addAttribute("productos", productos);

        } catch (Exception e) {
            model.addAttribute("mensaje", "Error al cargar la producción.");
            model.addAttribute("tipo", "error");
        }

        return "empleado/produccion/produccionDiaria";
    }

    // Registrar producción (desde el modal)
    @PostMapping("/produccion/registrar")
    public String registrarProduccion(
            @RequestParam("productoId") Long productoId,
            @RequestParam("cantidadElaborada") double cantidadElaborada,
            RedirectAttributes redirectAttributes) {

        try {

            Producto producto = productoRepository.findById(productoId)
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

            User empleado = getCurrentUser();

            produccionService.registrarProduccion(producto, cantidadElaborada, empleado);

            redirectAttributes.addFlashAttribute("mensaje", "Producción registrada exitosamente.");
            redirectAttributes.addFlashAttribute("tipo", "success");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensaje", "Error al registrar producción: " + e.getMessage());
            redirectAttributes.addFlashAttribute("tipo", "error");
        }

        return "redirect:/empleado/produccion";
    }

    // Método auxiliar para obtener el usuario actual (ajusta según tu
    // implementación de autenticación)
    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Object principal = auth.getPrincipal();

        if (principal instanceof CustomUserDetails) {
            return ((CustomUserDetails) principal).getUser();
        } else {
            System.out.println("⚠ Principal NO es CustomUserDetails, es: " + principal.getClass());
            throw new IllegalStateException("Usuario no autenticado correctamente");
        }
    }

}
