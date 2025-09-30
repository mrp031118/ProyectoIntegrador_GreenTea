package com.example.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import ch.qos.logback.core.model.Model;

@Controller
@RequestMapping("/empleado/reportes")
public class ReportesEmpleadoController {

    @GetMapping()
    public String listarProductos(Model model) {
        return "admin/Reportes"; 
    }

}
