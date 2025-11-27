package com.example.demo.controller.proveedor;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.demo.entity.proveedores.Proveedor;
import com.example.demo.repository.categorias.CategoriaProveedorRepository;
import com.example.demo.repository.proveedor.ProveedorRepository;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequestMapping("/admin/proveedores")
public class ProveedorController {

    @Autowired
    private ProveedorRepository proveedorRepository;

    @Autowired
    private CategoriaProveedorRepository categoriaProveedorRepository;

    // Listar proveedores
    @GetMapping
    public String listarProveedores(@RequestParam(required = false) String categoriaNombre, Model model) {
        List<Proveedor> proveedores;

        if (categoriaNombre != null && !categoriaNombre.isEmpty()) {
            // Busca por nombre de categoría
            proveedores = proveedorRepository.findByCategoriaNombre(categoriaNombre);
        } else {
            proveedores = proveedorRepository.findAll();
        }

        model.addAttribute("proveedores", proveedores);
        model.addAttribute("categorias", categoriaProveedorRepository.findAll());
        model.addAttribute("categoriaSeleccionada", categoriaNombre);

        return "admin/proveedores/proveedoresLista";
    }

    // Formularios para agregar proveedor
    @GetMapping("/agregar")
    public String mostrarFormularioAgregar(Model model) {
        model.addAttribute("proveedor", new Proveedor());
        model.addAttribute("categorias", categoriaProveedorRepository.findAll());

        return "admin/proveedores/proveedorForm";
    }

    // Guardar nuevo proveedor
    @PostMapping("/agregar")
    public String guardarProveedor(@Valid @ModelAttribute("proveedor") Proveedor proveedor,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            model.addAttribute("categorias", categoriaProveedorRepository.findAll());
            return "admin/proveedores/proveedorForm";
        }

        proveedorRepository.save(proveedor);

        redirectAttributes.addFlashAttribute("mensaje", "Proveedor registrado exitosamente.");
        redirectAttributes.addFlashAttribute("tipo", "success");

        return "redirect:/admin/proveedores";
    }

    // Formulario para editar proveedor
    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditar(@PathVariable Long id, Model model) {
        Proveedor proveedor = proveedorRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Proveedor no encontrado"));
        model.addAttribute("proveedor", proveedor);
        model.addAttribute("categorias", categoriaProveedorRepository.findAll());
        return "admin/proveedores/proveedorForm";
    }

    // Guardar canbios de proveedor editado
    @PostMapping("/editar")
    public String guardarProveedorEditado(@ModelAttribute Proveedor proveedor,
            RedirectAttributes redirectAttributes) {

        proveedorRepository.save(proveedor);

        redirectAttributes.addFlashAttribute("mensaje", "Proveedor actualizado correctamente.");
        redirectAttributes.addFlashAttribute("tipo", "success");

        return "redirect:/admin/proveedores";
    }

    // Eliminar proveedor
    @PostMapping("/eliminar/{id}")
    public String EliminarProveedor(@PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        proveedorRepository.deleteById(id);

        redirectAttributes.addFlashAttribute("mensaje", "Proveedor eliminado correctamente.");
        redirectAttributes.addFlashAttribute("tipo", "success");

        return "redirect:/admin/proveedores";
    }

}
