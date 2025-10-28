package com.example.demo.controller.movimientos;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

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
    public String listarMovimientos(Model model) {
        List<Movimiento> movimientos = movimientoService.listarMovimientosPEPS();
        model.addAttribute("movimientos", movimientos);
        return "/admin/movimientos/movimientosLista";
    }

    // Mostrar formulario de nuevo producto
    @GetMapping("/merma")
    public String mostrarFormularioMerma(Model model) {
        model.addAttribute("insumos", insumoService.listarInsumos());
        model.addAttribute("lotes", kardexLoteService.obtenerKardexLotes()); // Vista KardexLoteProjection
        return "admin/movimientos/movimientoFormulario";
    }

    @PostMapping("/guardar-merma")
    public String guardarMerma(
            @RequestParam("insumoId") Long insumoId,
            @RequestParam("loteId") Long loteId,
            @RequestParam("cantidad") Double cantidad,
            @RequestParam(value = "observaciones", required = false) String observaciones,
            Model model) {

        try {
            Movimiento mov = new Movimiento();

            // Asociar insumo
            mov.setInsumo(insumoService.obtenerInsumosPorId(insumoId));

            // Tipo de movimiento MERMA (id = 4)
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

            // Guardar en la BD
            movimientoService.registrarMovimiento(mov);

            model.addAttribute("exito", "Merma registrada correctamente.");

        } catch (Exception e) {
            model.addAttribute("error", "Ocurrió un error al registrar la merma.");
            e.printStackTrace();
        }

        return "redirect:/admin/movimientos/insumo";
    }

}
