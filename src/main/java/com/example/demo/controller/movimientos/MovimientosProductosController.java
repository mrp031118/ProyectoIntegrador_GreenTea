package com.example.demo.controller.movimientos;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import ch.qos.logback.core.model.Model;

@Controller
@RequestMapping("/admin/movimientos/producto")
public class MovimientosProductosController {

    @GetMapping()
    public String listarProductos(Model model) {
        return "admin/movimientos/movimientosProductos"; 
    }
    
}
