package com.example.demo.controller.movimientos;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.demo.entity.movimientos.MovimientoProducto;
import com.example.demo.service.movimientos.MovimientoProductoService;

@Controller
public class MovimientoProductoController {

    @Autowired
    private MovimientoProductoService movimientoProductoService;

    @GetMapping("/admin/movimientos-productos")
    public String listarMovimientosProductos(
            @RequestParam(value = "producto", required = false) String productoFiltro,
            @RequestParam(value = "fecha", required = false) LocalDate fechaFiltro,
            Model model) {

        // Obtener todos los movimientos con PEPS
        List<MovimientoProducto> movimientos = movimientoProductoService.listarTodosMovimientosPEPS();

        // Aplicar filtros si se proporcionan
        if (productoFiltro != null && !productoFiltro.isEmpty()) {
            movimientos = movimientos.stream()
                    .filter(m -> m.getProducto().getNombre().toLowerCase().contains(productoFiltro.toLowerCase()))
                    .collect(Collectors.toList());
        }
        if (fechaFiltro != null) {
            movimientos = movimientos.stream()
                    .filter(m -> m.getFecha().toLocalDate().equals(fechaFiltro))
                    .collect(Collectors.toList());
        }

        // Pasar datos a la vista
        model.addAttribute("movimientos", movimientos);

        // Retornar la vista (asegúrate de que el HTML esté en
        // templates/admin/movimientos.html o similar)
        return "admin/movimientos/movimientosProductos";
    }

}
