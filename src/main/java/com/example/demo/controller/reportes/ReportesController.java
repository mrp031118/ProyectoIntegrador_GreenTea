package com.example.demo.controller.reportes;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.demo.service.DashboardAdminService;

@Controller
@RequestMapping("/admin/reportes")
public class ReportesController {

    @Autowired
    private DashboardAdminService dashboardService;

    @GetMapping
    public String reportesGenerales(@RequestParam(required = false) String fechaInicio,
            @RequestParam(required = false) String fechaFin,
            @RequestParam(required = false) String rango,
            Model model) {
        // Lógica para rangos predefinidos
        LocalDate hoy = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        if ("semanal".equals(rango)) {
            fechaInicio = hoy.minusDays(7).format(formatter);
            fechaFin = hoy.format(formatter);
        } else if ("quincenal".equals(rango)) {
            fechaInicio = hoy.minusDays(15).format(formatter);
            fechaFin = hoy.format(formatter);
        }

        // Cargar todos los reportes
        model.addAttribute("reporteStockCritico", dashboardService.getReporteStockCritico());
        model.addAttribute("reporteKardex", dashboardService.getReporteKardex(fechaInicio, fechaFin));
        model.addAttribute("reporteRentabilidad", dashboardService.getReporteRentabilidad(fechaInicio, fechaFin));
        model.addAttribute("reporteMermas", dashboardService.getReporteMermas(fechaInicio, fechaFin));
        model.addAttribute("fechaInicio", fechaInicio);
        model.addAttribute("fechaFin", fechaFin);

        return "admin/reportes"; // Vista única con pestañas
    }

}
