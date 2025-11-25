package com.example.demo.controller.produccion;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.demo.dto.MermaDTO;
import com.example.demo.entity.productos.Producto;
import com.example.demo.entity.usuarios.User;
import com.example.demo.repository.productos.ProductoRepository;
import com.example.demo.service.movimientos.MermaService;
import com.example.demo.service.usuarios.CustomUserDetails;
import com.example.demo.service.venta.ProduccionService;


@Controller
@RequestMapping("/empleado/registrarMerma")
public class RegistarMermaController {

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private ProduccionService produccionService;

    @Autowired
    private MermaService mermaService;

    // REGISTRAR MERMA
    // En RegistarMermaController.java
    @PostMapping("/guardar")
    public String guardarMerma(
            @RequestParam("productoId") Long productoId,
            @RequestParam("cantidadMerma") double cantidadMerma,
            @RequestParam("motivo") String motivo, // Ya lo tienes
            Model model) {

        try {
            // Obtener producto
            Producto producto = productoRepository.findById(productoId)
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

            // Obtener empleado logueado
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()
                    || authentication.getPrincipal() instanceof String) {
                throw new RuntimeException("Empleado no autenticado.");
            }
            CustomUserDetails cud = (CustomUserDetails) authentication.getPrincipal();
            User usuarioActual = cud.getUser();

            produccionService.registrarMermaProducto(producto, cantidadMerma, motivo, usuarioActual);

            model.addAttribute("success", "Merma registrada correctamente.");
        } catch (Exception ex) {
            model.addAttribute("error", "Error: " + ex.getMessage());
            ex.printStackTrace(); // Para depurar
        }
        return "redirect:/empleado/registrarMerma";
    }

    @GetMapping
    public String listarMermas(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails cud = (CustomUserDetails) auth.getPrincipal();
        User empleado = cud.getUser();
        List<MermaDTO> mermas = mermaService.listarMermasPorEmpleado(empleado);
        model.addAttribute("mermas", mermas);
        // 2️⃣ Listar todos los productos para el select
        List<Producto> productos = productoRepository.findAll();
        model.addAttribute("productos", productos);
        return "empleado/produccion/registrarMerma";
    }

}
