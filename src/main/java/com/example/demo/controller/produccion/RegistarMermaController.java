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
    @PostMapping("/guardar")
    public String guardarMerma(
            @RequestParam("productoId") Long productoId,
            @RequestParam("cantidadMerma") double cantidadMerma,
            @RequestParam("motivo") String motivo,
            Model model) {

        try {

            User usuarioActual = obtenerUsuarioActual();
            if (usuarioActual == null) {
                model.addAttribute("tipo", "error");
                model.addAttribute("mensaje", "No se pudo obtener el usuario autenticado.");
                return cargarVista(model, usuarioActual);
            }

            Producto producto = productoRepository.findById(productoId)
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

            produccionService.registrarMermaProducto(producto, cantidadMerma, motivo, usuarioActual);

            model.addAttribute("tipo", "success");
            model.addAttribute("mensaje", "Merma registrada correctamente.");

        } catch (Exception ex) {
            ex.printStackTrace();
            model.addAttribute("tipo", "error");
            model.addAttribute("mensaje", "Error: " + ex.getMessage());
        }

        return cargarVista(model, obtenerUsuarioActual());
    }

    // CARGAR VISTA
    @GetMapping
    public String listarMermas(Model model) {

        User empleado = obtenerUsuarioActual();

        return cargarVista(model, empleado);
    }

    // MÉTODO UTILITARIO PARA CARGAR LISTAS Y VISTA
    private String cargarVista(Model model, User empleado) {

        if (empleado != null) {
            List<MermaDTO> mermas = mermaService.listarMermasPorEmpleado(empleado);
            model.addAttribute("mermas", mermas);
        }

        List<Producto> productos = productoRepository.findAll();
        model.addAttribute("productos", productos);

        return "empleado/produccion/registrarMerma";
    }

    // OBTENER USUARIO AUTENTICADO
    private User obtenerUsuarioActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal() instanceof String) {
            return null;
        }

        return ((CustomUserDetails) auth.getPrincipal()).getUser();
    }
}
