package com.example.demo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.demo.entity.Insumo;
import com.example.demo.entity.UnidadMedida;
import com.example.demo.service.InsumoService;
import com.example.demo.service.ProveedorService;
import com.example.demo.service.UnidadMedidaService;

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

        return "/admin/insumosLista";
    }

    // mostrar formulario de creacion
    @GetMapping("/nuevo")
    public String mostrarFormulario(Model model) {
        model.addAttribute("insumo", new Insumo());
        model.addAttribute("proveedores", proveedorService.listarProveedores());
        model.addAttribute("unidades", unidadMedidaService.listarUnidades());

        return "/admin/insumosFormulario";
    }

    // guardar o actualizar insumo
    @PostMapping("guardar")
    public String guardar(@ModelAttribute Insumo insumo, Model model) {

        insumoService.guardarInsumo(insumo);
        return "redirect:/admin/insumos";
    }

    // mostrar formulario de edicion
    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {
        Insumo insumo = insumoService.obtenerInsumosPorId(id);

        model.addAttribute("insumo", insumo);
        model.addAttribute("proveedores", proveedorService.listarProveedores());
        model.addAttribute("unidades", unidadMedidaService.listarUnidades());

        return "/admin/insumosFormulario";
    }

    // Eliminar insumo
    @GetMapping("/eliminar/{id}")
    public String eliminarInsumo(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            insumoService.eliminarInsumo(id);
            redirectAttributes.addFlashAttribute("exito", "Insumo eliminado correctamente.");
            return "redirect:/admin/insumos";
        } catch (Exception e) {
            // Mensaje de error en la misma vista
            model.addAttribute("error", e.getMessage());

            // Recargar lista de insumos y filtros
            List<Insumo> insumos = insumoService.listarInsumos();
            model.addAttribute("insumos", insumos);
            model.addAttribute("proveedores", proveedorService.listarProveedores());
            model.addAttribute("unidades", unidadMedidaService.listarUnidades());

            return "/admin/insumosLista"; // se mantiene en la misma vista para mostrar el error
        }
    }

}
