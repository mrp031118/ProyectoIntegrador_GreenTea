package com.example.demo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.demo.entity.Movimiento;
import com.example.demo.repository.MovimientoRepository;
import com.example.demo.service.MovimientoService;


@Controller
@RequestMapping("/admin/movimientos/insumo")
public class MovimientoController {

    @Autowired
    private MovimientoService movimientoService;

    @GetMapping()
    public String listarMovimientos(Model model) {
        List<Movimiento> movimientos = movimientoService.listarMovimientosPEPS();
        model.addAttribute("movimientos", movimientos);
        return "/admin/movimientosLista";
    }

    // Mostrar formulario de nuevo producto
    @GetMapping("/merma")
    public String guardarMerma(Model model) {
        return "admin/movimientoFormulario";
    }

}
