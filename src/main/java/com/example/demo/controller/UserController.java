package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.demo.entity.usuarios.User;
import com.example.demo.repository.usuarios.UserRepository;
import com.example.demo.service.DashboardEmployeeService;
import com.example.demo.service.usuarios.CustomUserDetails;

@Controller
public class UserController {

    private final DashboardEmployeeService employeeDashboardService;

    @Autowired
    public UserController(DashboardEmployeeService employeeDashboardService) {
        this.employeeDashboardService = employeeDashboardService;
    }

    @GetMapping("/empleado/dashboard")
    public String userProfile(Model model) {

        // Obtener el usuario autenticado
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails cud = (CustomUserDetails) auth.getPrincipal();
        User usuarioActual = cud.getUser();
        Long empleadoId = usuarioActual.getId();

        // Enviar datos a la vista
        model.addAttribute("montoTotalVendido", employeeDashboardService.getMontoTotalVendidoHoy(empleadoId));
        model.addAttribute("numeroTransacciones", employeeDashboardService.getNumeroTransaccionesHoy(empleadoId));
        model.addAttribute("clientesAtendidos", employeeDashboardService.getClientesAtendidosHoy(empleadoId));
        model.addAttribute("totalMermas", employeeDashboardService.getTotalMermasHoy(empleadoId));
        model.addAttribute("ultimasVentas", employeeDashboardService.getUltimas5Ventas(empleadoId));
        model.addAttribute("topProductos", employeeDashboardService.getTop3ProductosHoy(empleadoId));

        return "empleado/dashboardEmple"; // Tu vista final
    }

}
