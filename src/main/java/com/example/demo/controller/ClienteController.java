package com.example.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import ch.qos.logback.core.model.Model;

@Controller
@RequestMapping("/empleado/cliente")
public class ClienteController {

    @GetMapping()
    public String listarclientes(Model model) {
        return "empleado/clientesLista"; 
    }

    @GetMapping("/editar")
    public String editar(Model model) {
        return "empleado/clienteFormulario"; 
    }
}
