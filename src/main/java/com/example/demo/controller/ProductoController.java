package com.example.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import ch.qos.logback.core.model.Model;

@Controller
@RequestMapping("/admin/productos")
public class ProductoController {

    @GetMapping()
    public String listarProductos(Model model) {
        return "admin/productosLista"; 
    }

    // Mostrar formulario de nuevo producto
    @GetMapping("/nuevo")
    public String mostrarFormulario(Model model) {
        return "admin/productosFormulario"; 
    }
}
