package com.example.demo.controller.movimientos;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.demo.entity.lotes.Lote;
import com.example.demo.entity.movimientos.Movimiento;
import com.example.demo.service.insumos.InsumoService;
import com.example.demo.service.lotes.KardexLoteService;
import com.example.demo.service.lotes.LoteService;
import com.example.demo.service.movimientos.MovimientoService;
import com.example.demo.service.movimientos.TipoMovimientoService;

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
            Movimiento mov = new Movimiento();

            // Asociar insumo
            mov.setInsumo(insumoService.obtenerInsumosPorId(insumoId));

            // Tipo MERMA
            mov.setTipoMovimiento(tipoMovimientoService.obtenerPorId(4));

            // Asociar lote
            Lote lote = loteService.obtenerPorId(loteId);
            mov.setLote(lote);

            // Cantidad y costos
            mov.setCantidad(cantidad);
            mov.setCostoUnitario(lote.getCostoUnitario());
            mov.setTotal(cantidad * lote.getCostoUnitario());

            // Observaciones
            mov.setObservaciones(observaciones);

            // Fecha actual
            mov.setFecha(LocalDateTime.now());

            // Guardar
            movimientoService.registrarMovimiento(mov);

            redirectAttributes.addFlashAttribute("mensaje", "Merma registrada correctamente.");
            redirectAttributes.addFlashAttribute("tipo", "success");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensaje", "Error al registrar merma: " + e.getMessage());
            redirectAttributes.addFlashAttribute("tipo", "error");
        }

        return "redirect:/admin/movimientos/insumo";
    }
}
