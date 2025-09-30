package com.example.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import ch.qos.logback.core.model.Model;

@Controller
@RequestMapping("/admin/recetas")
public class RecetaController {

    @GetMapping()
    public String listarProductos(Model model) {
        return "admin/recetas"; 
    }

    @GetMapping("/nuevo")
    public String nuevaReceta(Model model) {
        return "admin/recetasFormulario"; 
    }

}
