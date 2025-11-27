package com.example.demo.controller.insumos;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.demo.entity.insumos.Insumo;
import com.example.demo.entity.insumos.UnidadMedida;
import com.example.demo.service.insumos.InsumoService;
import com.example.demo.service.insumos.UnidadMedidaService;
import com.example.demo.service.proveedor.ProveedorService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequestMapping("/admin/insumos")
public class InsumoController {

    @Autowired
    private InsumoService insumoService;

    @Autowired
    private ProveedorService proveedorService;

    @Autowired
    private UnidadMedidaService unidadMedidaService;

    // listar todos los insumos
    @GetMapping
    public String listar(Model model,
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) Long unidadMedidaId) {

        UnidadMedida unidadMedida = unidadMedidaId != null ? unidadMedidaService.obtenerPorId(unidadMedidaId) : null;

        // Solo filtra por nombre y unidad de medida
        List<Insumo> insumos = insumoService.buscarInsumos(nombre != null ? nombre : "", unidadMedida);

        model.addAttribute("insumos", insumos);
        model.addAttribute("unidades", unidadMedidaService.listarUnidades());

        return "/admin/insumos/insumosLista";
    }

    // mostrar formulario de creacion
    @GetMapping("/nuevo")
    public String mostrarFormulario(Model model) {
        model.addAttribute("insumo", new Insumo());
        model.addAttribute("proveedores", proveedorService.listarProveedores());
        model.addAttribute("unidades", unidadMedidaService.listarUnidades());

        return "/admin/insumos/insumosFormulario";
    }

    // guardar o actualizar insumo
    @PostMapping("/guardar")
    public String guardar(@ModelAttribute Insumo insumo, RedirectAttributes redirectAttributes) {

        boolean esEdicion = (insumo.getInsumoId() != null); // verificar si viene con ID

        try {
            insumoService.guardarInsumo(insumo);

            if (esEdicion) {
                redirectAttributes.addFlashAttribute("mensaje", "Insumo actualizado correctamente.");
            } else {
                redirectAttributes.addFlashAttribute("mensaje", "Insumo registrado exitosamente.");
            }

            redirectAttributes.addFlashAttribute("tipo", "success");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensaje", "Error al guardar el insumo.");
            redirectAttributes.addFlashAttribute("tipo", "error");
        }
        return "redirect:/admin/insumos";
    }

    // mostrar formulario de edicion
    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {

        try {
            Insumo insumo = insumoService.obtenerInsumosPorId(id);

            if (insumo == null) {
                redirectAttributes.addFlashAttribute("mensaje", "El insumo no existe o fue eliminado.");
                redirectAttributes.addFlashAttribute("tipo", "error");
                return "redirect:/admin/insumos";
            }

            model.addAttribute("insumo", insumo);
            model.addAttribute("proveedores", proveedorService.listarProveedores());
            model.addAttribute("unidades", unidadMedidaService.listarUnidades());

            return "/admin/insumos/insumosFormulario";

        } catch (Exception e) {

            redirectAttributes.addFlashAttribute("mensaje",
                    "Ocurrió un error al cargar la información: " + e.getMessage());
            redirectAttributes.addFlashAttribute("tipo", "error");

            return "redirect:/admin/insumos";
        }
    }

    // Eliminar insumo
    @GetMapping("/eliminar/{id}")
    public String eliminarInsumo(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            insumoService.eliminarInsumo(id);
            redirectAttributes.addFlashAttribute("mensaje", "Insumo eliminado correctamente.");
            redirectAttributes.addFlashAttribute("tipo", "success");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensaje", "Error al eliminar el insumo: " + e.getMessage());
            redirectAttributes.addFlashAttribute("tipo", "error");
        }
        return "redirect:/admin/insumos";
    }

}
