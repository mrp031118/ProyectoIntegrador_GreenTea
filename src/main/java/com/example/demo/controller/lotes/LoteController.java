package com.example.demo.controller.lotes;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.demo.dto.KardexLoteProjection;
import com.example.demo.entity.insumos.Insumo;
import com.example.demo.entity.lotes.Lote;
import com.example.demo.entity.proveedores.Proveedor;
import com.example.demo.service.insumos.InsumoService;
import com.example.demo.service.lotes.KardexLoteService;
import com.example.demo.service.lotes.LoteService;
import com.example.demo.service.proveedor.ProveedorService;

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
    public String listar(Model model,
            @RequestParam(required = false) Long proveedorId,
            @RequestParam(required = false) Long insumoId,
            @RequestParam(required = false) String nombreInsumo) {

        Proveedor proveedor = proveedorId != null ? proveedorService.obtenerPorId(proveedorId) : null;
        Insumo insumo = insumoId != null ? insumoService.obtenerInsumosPorId(insumoId) : null;

        List<Lote> lotes = loteService.buscarLotes(proveedor, insumo, nombreInsumo);

        model.addAttribute("lotes", lotes);
        model.addAttribute("proveedores", proveedorService.listarProveedores());
        model.addAttribute("insumos", insumoService.listarInsumos());
        model.addAttribute("nombreInsumo", nombreInsumo);

        return "/admin/lotes/lotesLista";
    }

    // Nuevo lote
    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("lote", new Lote());
        model.addAttribute("insumos", insumoService.listarInsumos());
        model.addAttribute("proveedores", proveedorService.listarProveedores());
        return "admin/lotes/lotesFormulario";
    }

    // Guardar lote
    @PostMapping("/guardar")
    public String guardar(@ModelAttribute("lote") Lote lote, RedirectAttributes ra) {

        boolean esEdicion = lote.getLoteId() != null; // o el nombre exacto de tu ID

        // --- Validaciones ---
        if (lote.getInsumo() == null || lote.getInsumo().getInsumoId() == null) {
            ra.addFlashAttribute("mensaje", "Selecciona un insumo válido.");
            ra.addFlashAttribute("tipo", "error");
            return "redirect:/admin/lotes/nuevo";
        }

        Insumo ins = insumoService.obtenerInsumosPorId(lote.getInsumo().getInsumoId());
        lote.setInsumo(ins);

        if (lote.getProveedor() == null || lote.getProveedor().getId() == null) {
            ra.addFlashAttribute("mensaje", "Selecciona un proveedor válido.");
            ra.addFlashAttribute("tipo", "error");
            return "redirect:/admin/lotes/nuevo";
        }

        Proveedor prov = proveedorService.obtenerPorId(lote.getProveedor().getId());
        lote.setProveedor(prov);

        // --- Guardar ---
        try {
            loteService.guardarLote(lote);

            if (esEdicion) {
                ra.addFlashAttribute("mensaje", "Lote actualizado correctamente.");
            } else {
                ra.addFlashAttribute("mensaje", "Lote registrado exitosamente.");
            }

            ra.addFlashAttribute("tipo", "success");

        } catch (Exception e) {
            ra.addFlashAttribute("mensaje", "Error al guardar el lote: " + e.getMessage());
            ra.addFlashAttribute("tipo", "error");
        }

        return "redirect:/admin/lotes";
    }

    // Editar lote
    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model, RedirectAttributes ra) {
        try {
            Lote lote = loteService.obtenerPorId(id);

            model.addAttribute("lote", lote);
            model.addAttribute("insumos", insumoService.listarInsumos());
            model.addAttribute("proveedores", proveedorService.listarProveedores());

            return "admin/lotes/lotesFormulario";

        } catch (Exception e) {
            ra.addFlashAttribute("mensaje", "Error: " + e.getMessage());
            ra.addFlashAttribute("tipo", "error");
            return "redirect:/admin/lotes";
        }
    }

    // ELIMINAR LOTE
    @GetMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id, RedirectAttributes redirectAttributes) {

        try {
            loteService.eliminarLote(id);
            redirectAttributes.addFlashAttribute("mensaje", "Lote eliminado correctamente.");
            redirectAttributes.addFlashAttribute("tipo", "success");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensaje", "Error al eliminar lote: " + e.getMessage());
            redirectAttributes.addFlashAttribute("tipo", "error");
        }

        return "redirect:/admin/lotes";
    }

    // Ver Kardex de lotes
    @GetMapping("/kardex")
    public String kardex(Model model) {
        List<KardexLoteProjection> kardex = kardexLoteService.obtenerKardexLotes();
        model.addAttribute("kardex", kardex);
        return "/admin/lotes/kardexLoteLista";
    }
}
