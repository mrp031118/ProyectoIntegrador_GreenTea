package com.example.demo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.demo.dto.KardexLoteProjection;
import com.example.demo.entity.Insumo;
import com.example.demo.entity.Lote;
import com.example.demo.entity.Proveedor;
import com.example.demo.service.InsumoService;
import com.example.demo.service.KardexLoteService;
import com.example.demo.service.LoteService;
import com.example.demo.service.ProveedorService;

@Controller
@RequestMapping("/admin/lotes")
public class LoteController {
    @Autowired
    private LoteService loteService;

    @Autowired
    private InsumoService insumoService;

    @Autowired
    private ProveedorService proveedorService;

    @Autowired
    private KardexLoteService kardexLoteService;

    // Listar lotes
    @GetMapping
    public String listar(Model model) {
        model.addAttribute("lotes", loteService.listarLotes());
        return "/admin/lotesLista";
    }

    // Nuevo lote
    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("lote", new Lote());
        model.addAttribute("insumos", insumoService.listarInsumos());
        model.addAttribute("proveedores", proveedorService.listarProveedores());
        return "admin/lotesFormulario";
    }

    // Guardar lote
    @PostMapping("/guardar")
    public String guardar(@ModelAttribute("lote") Lote lote, RedirectAttributes ra) {
        // Validar insumo
        if (lote.getInsumo() != null && lote.getInsumo().getInsumoId() != null) {
            Insumo ins = insumoService.obtenerInsumosPorId(lote.getInsumo().getInsumoId());
            lote.setInsumo(ins);
        } else {
            ra.addFlashAttribute("error", "Selecciona un insumo válido.");
            return "redirect:/admin/lote/nuevo";
        }

        // Validar proveedor
        if (lote.getProveedor() != null && lote.getProveedor().getId() != null) {
            Proveedor prov = proveedorService.obtenerPorId(lote.getProveedor().getId());
            lote.setProveedor(prov);
        } else {
            ra.addFlashAttribute("error", "Selecciona un proveedor válido.");
            return "redirect:/admin/lote/nuevo";
        }

        // Inicializar cantidad disponible al crear
        if (lote.getLoteId() == null && lote.getCantidadDisponible() == null) {
            lote.setCantidadDisponible(lote.getCantidad());
        }

        // Guardar
        loteService.guardarLote(lote);
        ra.addFlashAttribute("success", "Lote guardado correctamente.");
        return "redirect:/admin/lotes";
    }

    // Editar lote
    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {
        Lote lote = loteService.obtenerPorId(id);
        model.addAttribute("lote", lote);
        model.addAttribute("insumos", insumoService.listarInsumos());
        model.addAttribute("proveedores", proveedorService.listarProveedores());
        return "admin/lotesFormulario";
    }

    // Eliminar lote
    @GetMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            loteService.eliminarLote(id);
            redirectAttributes.addAttribute("success", "Lote eliminado correctamente.");
        } catch (Exception e) {
            redirectAttributes.addAttribute("error", e.getMessage());
        }
        return "redirect:/admin/lotes";
    }

    // Ver Kardex de lotes
    @GetMapping("/kardex")
    public String kardex(Model model) {
        List<KardexLoteProjection> kardex = kardexLoteService.obtenerKardexLotes();
        model.addAttribute("kardex", kardex);
        return "/admin/kardexLoteLista";
    }
}
