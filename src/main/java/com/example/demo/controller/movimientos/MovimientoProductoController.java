package com.example.demo.controller.movimientos;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.demo.entity.movimientos.MovimientoProducto;
import com.example.demo.service.movimientos.MovimientoProductoService;

@Controller
public class MovimientoProductoController {

    @Autowired
    private MovimientoProductoService movimientoProductoService;

    @GetMapping("/admin/movimientos/movimientos-productos")
    public String listarMovimientosProductos(
            @RequestParam(value = "fecha", required = false) LocalDate fechaFiltro,
            Model model,
            RedirectAttributes redirectAttributes) {

        try {
            // Obtener todos los movimientos PEPS
            List<MovimientoProducto> movimientos = movimientoProductoService.listarTodosMovimientosPEPS();

            // Filtro por fecha
            if (fechaFiltro != null) {
                movimientos = movimientos.stream()
                        .filter(m -> m.getFecha().toLocalDate().equals(fechaFiltro))
                        .collect(Collectors.toList());
            }

            // Enviar datos
            model.addAttribute("movimientos", movimientos);

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensaje", "Error al cargar movimientos de productos.");
            redirectAttributes.addFlashAttribute("tipo", "error");
            return "redirect:/admin/movimientos/movimientos-productos";
        }

        return "admin/movimientos/movimientosProductos";
    }

}
