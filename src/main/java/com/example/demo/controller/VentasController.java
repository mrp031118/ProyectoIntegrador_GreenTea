package com.example.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import ch.qos.logback.core.model.Model;

@Controller
@RequestMapping("/admin/ventas")
public class VentasController {

    @GetMapping()
    public String listarProductos(Model model) {
        return "admin/ventas"; 
    }
}
