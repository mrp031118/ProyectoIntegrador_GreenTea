package com.example.demo.controller.ventas;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.demo.entity.venta.Venta;
import com.example.demo.service.venta.VentaService;


@Controller
@RequestMapping("/admin/ventas")
public class VentasController {

    @Autowired
    private VentaService ventaService;

    @GetMapping
    public String listarVentas(Model model) {
        List<Venta> ventas = ventaService.listarTodasVentas();
        model.addAttribute("ventas", ventas);
        return "admin/ventas/ventas"; 
    }
}
