package com.example.demo.controller.ventas;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.demo.entity.productos.Producto;
import com.example.demo.repository.productos.ProductoRepository;
import com.example.demo.service.productos.VerificarDisponibilidadService;
import com.example.demo.service.productos.VerificarDisponibilidadService.ResultadoDisponibilidad;

@Controller
@RequestMapping("/empleado/disponibilidad")
public class VerificarDisponibilidadController {

    @Autowired
    private VerificarDisponibilidadService verificarDisponibilidadService;

    @Autowired
    private ProductoRepository productoRepository;

    @GetMapping
    public String mostrarPagina(Model model) {
        // lista de productos para el select
        List<Producto> productos = productoRepository.findAll();
        model.addAttribute("productos", productos);
        model.addAttribute("resultados", null);
        return "empleado/venta/verificarDisponibilidad";
    }

    @PostMapping("/consultar")
    public String consultarDisponibilidad(
            @RequestParam("productoId") Long productoId,
            @RequestParam("cantidad") Double cantidad,
            Model model) {

        List<ResultadoDisponibilidad> resultados = verificarDisponibilidadService.verificarDisponibilidad(productoId,
                cantidad);

        // lista de productos para el select
        List<Producto> productos = productoRepository.findAll();
        model.addAttribute("productos", productos);

        // Obtener el producto para mostrar su nombre en los modales
        Producto producto = productoRepository.findById(productoId).orElse(null);
        model.addAttribute("nombreProducto", producto != null ? producto.getNombre() : "Desconocido");
        model.addAttribute("cantidadSolicitada", cantidad);

        if (resultados == null) {
            model.addAttribute("resultados", null);
            model.addAttribute("sinReceta", true);
            return "empleado/venta/verificarDisponibilidad";
        }

        model.addAttribute("resultados", resultados);
        return "empleado/venta/verificarDisponibilidad";
    }
}
