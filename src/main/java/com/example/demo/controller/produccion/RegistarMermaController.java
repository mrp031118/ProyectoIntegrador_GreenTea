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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.demo.dto.MermaDTO;
import com.example.demo.entity.productos.Producto;
import com.example.demo.entity.usuarios.User;
import com.example.demo.repository.productos.ProductoRepository;
import com.example.demo.service.movimientos.MermaService;
import com.example.demo.service.movimientos.MovimientoService;
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

    @Autowired
    private MovimientoService movimientoService;

    // REGISTRAR MERMA
    @PostMapping("/guardar")
    public String guardarMerma(
            @RequestParam("productoId") Long productoId,
            @RequestParam("cantidadMerma") double cantidadMerma,
            @RequestParam("motivo") String motivo,
            RedirectAttributes redirectAttributes) {

        try {

            User usuarioActual = obtenerUsuarioActual();
            if (usuarioActual == null) {
                redirectAttributes.addFlashAttribute("tipo", "error");
                redirectAttributes.addFlashAttribute("mensaje",
                        "No se pudo obtener el usuario autenticado.");
                return "redirect:/empleado/registrarMerma";
            }

            // 1️⃣ OBTENER PRODUCTO
            Producto producto = productoRepository.findById(productoId)
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

            String tipo = producto.getCategoria().getTipoControl().trim().toUpperCase();

            // 2️⃣ VALIDAR QUE EL PRODUCTO TENGA RECETA
            if (!produccionService.tieneReceta(producto)) {
                redirectAttributes.addFlashAttribute("tipo", "error");
                redirectAttributes.addFlashAttribute("mensaje",
                        "Este producto no tiene receta. No se puede registrar merma.");
                return "redirect:/empleado/registrarMerma";
            }

            // 3️⃣ VALIDACIÓN SEGÚN TIPO DE PRODUCTO
            if (tipo.equals("ELABORADO")) {

                // STOCK DE PRODUCCIÓN
                double stockActual = produccionService.obtenerStockActualProducto(producto);

                if (stockActual <= 0) {
                    redirectAttributes.addFlashAttribute("tipo", "error");
                    redirectAttributes.addFlashAttribute("mensaje",
                            "Este producto elaborado no tiene stock en producción.");
                    return "redirect:/empleado/registrarMerma";
                }

                if (cantidadMerma > stockActual) {
                    redirectAttributes.addFlashAttribute("tipo", "error");
                    redirectAttributes.addFlashAttribute("mensaje",
                            "La merma (" + cantidadMerma + ") excede el stock disponible (" + stockActual + ").");
                    return "redirect:/empleado/registrarMerma";
                }

            } else if (tipo.equals("INSTANTANEO")) {

                // STOCK CALCULADO POR INSUMOS
                double unidadesPosibles = movimientoService.obtenerStockDisponiblePorReceta(producto);

                if (unidadesPosibles <= 0) {
                    redirectAttributes.addFlashAttribute("tipo", "error");
                    redirectAttributes.addFlashAttribute("mensaje",
                            "No hay insumos suficientes para registrar merma de este producto instantáneo.");
                    return "redirect:/empleado/registrarMerma";
                }

                if (cantidadMerma > unidadesPosibles) {
                    redirectAttributes.addFlashAttribute("tipo", "error");
                    redirectAttributes.addFlashAttribute("mensaje",
                            "La merma (" + cantidadMerma + ") supera lo disponible según insumos (" + unidadesPosibles
                                    + ").");
                    return "redirect:/empleado/registrarMerma";
                }
            }

            // 4️⃣ REGISTRAR MERMA EN SERVICIO CENTRAL
            produccionService.registrarMermaProducto(producto, cantidadMerma, motivo, usuarioActual);

            redirectAttributes.addFlashAttribute("tipo", "success");
            redirectAttributes.addFlashAttribute("mensaje", "Merma registrada correctamente.");

        } catch (Exception ex) {
            ex.printStackTrace();
            redirectAttributes.addFlashAttribute("tipo", "error");
            redirectAttributes.addFlashAttribute("mensaje", "Error: " + ex.getMessage());
        }

        return "redirect:/empleado/registrarMerma";
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
