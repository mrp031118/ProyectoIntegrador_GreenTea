package com.example.demo.controller.movimientos;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.demo.entity.lotes.Lote;
import com.example.demo.entity.movimientos.Movimiento;
import com.example.demo.entity.usuarios.User;
import com.example.demo.service.insumos.InsumoService;
import com.example.demo.service.lotes.KardexLoteService;
import com.example.demo.service.lotes.LoteService;
import com.example.demo.service.movimientos.MovimientoService;
import com.example.demo.service.movimientos.TipoMovimientoService;
import com.example.demo.service.usuarios.CustomUserDetails;

@Controller
@RequestMapping("/admin/movimientos/insumo")
public class MovimientoController {

    @Autowired
    private MovimientoService movimientoService;
    @Autowired
    private TipoMovimientoService tipoMovimientoService;
    @Autowired
    private LoteService loteService;
    @Autowired
    private KardexLoteService kardexLoteService;
    @Autowired
    private InsumoService insumoService;

    @GetMapping()
    public String listarMovimientos(@RequestParam(value = "fecha", required = false) LocalDate fechaFiltro, Model model,
            RedirectAttributes redirectAttributes) {
        try {
            List<Movimiento> movimientos = movimientoService.listarMovimientosPEPS();
            // Filtro por fecha
            if (fechaFiltro != null) {
                movimientos = movimientos.stream()
                        .filter(m -> m.getFecha().toLocalDate().equals(fechaFiltro))
                        .collect(Collectors.toList());
            }
            model.addAttribute("movimientos", movimientos);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensaje", "Error al cargar los movimientos: " + e.getMessage());
            redirectAttributes.addFlashAttribute("tipo", "error");
            return "redirect:/admin/movimientos/insumo";
        }

        return "/admin/movimientos/movimientosLista";
    }

    // Mostrar formulario de nuevo producto
    @GetMapping("/merma")
    public String mostrarFormularioMerma(Model model, RedirectAttributes redirectAttributes) {
        try {
            model.addAttribute("insumos", insumoService.listarInsumos());
            model.addAttribute("lotes", kardexLoteService.obtenerKardexLotes());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensaje", "Error al cargar el formulario de merma.");
            redirectAttributes.addFlashAttribute("tipo", "error");
            return "redirect:/admin/movimientos/insumo";
        }

        return "admin/movimientos/movimientoFormulario";
    }

    @PostMapping("/guardar-merma")
    public String guardarMerma(
            @RequestParam("insumoId") Long insumoId,
            @RequestParam("loteId") Long loteId,
            @RequestParam("cantidad") Double cantidad,
            @RequestParam(value = "observaciones", required = false) String observaciones,
            RedirectAttributes redirectAttributes) {

        try {

            // 1. VALIDAR CONTRA STOCK TOTAL PEPS
            double stockTotal = kardexLoteService.obtenerKardexLotes().stream()
                    .filter(k -> k.getInsumoId().equals(insumoId))
                    .mapToDouble(k -> k.getCantidadDisponible().doubleValue())
                    .sum();

            if (cantidad > stockTotal) {
                redirectAttributes.addFlashAttribute("mensaje",
                        "La merma ingresada (" + cantidad +
                                ") excede el stock total disponible del insumo (" + stockTotal + ").");
                redirectAttributes.addFlashAttribute("tipo", "error");
                return "redirect:/admin/movimientos/insumo/merma";
            }

            // 2. CONTINUAR REGISTRO NORMAL
            Movimiento mov = new Movimiento();

            User usuarioActual = obtenerUsuarioActual();
            if (usuarioActual == null) {
                redirectAttributes.addFlashAttribute("mensaje", "No se pudo identificar al usuario autenticado.");
                redirectAttributes.addFlashAttribute("tipo", "error");
                return "redirect:/admin/movimientos/insumo";
            }

            mov.setUsuario(usuarioActual);
            mov.setInsumo(insumoService.obtenerInsumosPorId(insumoId));
            mov.setTipoMovimiento(tipoMovimientoService.obtenerPorId(4));

            Lote lote = loteService.obtenerPorId(loteId);
            mov.setLote(lote);

            mov.setCantidad(cantidad);
            mov.setCostoUnitario(lote.getCostoUnitario());
            mov.setTotal(cantidad * lote.getCostoUnitario());
            mov.setObservaciones(observaciones);
            mov.setFecha(LocalDateTime.now());

            movimientoService.registrarMovimiento(mov);

            redirectAttributes.addFlashAttribute("mensaje", "Merma registrada correctamente.");
            redirectAttributes.addFlashAttribute("tipo", "success");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensaje", "Error al registrar merma: " + e.getMessage());
            redirectAttributes.addFlashAttribute("tipo", "error");
        }

        return "redirect:/admin/movimientos/insumo";
    }

    // OBTENER USUARIO AUTENTICADO
    private User obtenerUsuarioActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal() instanceof String) {
            return null;
        }

        return ((CustomUserDetails) auth.getPrincipal()).getUser();
    }
}
