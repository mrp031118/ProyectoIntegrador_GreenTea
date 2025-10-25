package com.example.demo.controller.ventas;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import ch.qos.logback.core.model.Model;

@Controller
@RequestMapping("/empleado/ventas")
public class VentaEmpleadoController {

    @GetMapping()
    public String listarProductos(Model model) {
        return "empleado/venta/ventas"; 
    }

}
