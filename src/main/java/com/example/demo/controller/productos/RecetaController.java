package com.example.demo.controller.productos;

import java.beans.PropertyEditorSupport;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.demo.entity.insumos.Insumo;
import com.example.demo.entity.insumos.UnidadMedida;
import com.example.demo.entity.productos.DetalleRecetaProducto;
import com.example.demo.entity.productos.Producto;
import com.example.demo.entity.productos.RecetaProducto;
import com.example.demo.service.insumos.InsumoService;
import com.example.demo.service.insumos.UnidadMedidaService;
import com.example.demo.service.productos.ProductoService;
import com.example.demo.service.productos.RecetaProductoService;

@Controller
@RequestMapping("/admin/recetas")
public class RecetaController {

    @Autowired
    private RecetaProductoService recetaService;
    @Autowired
    private ProductoService productoService;
    @Autowired
    private InsumoService insumoService;
    @Autowired
    private UnidadMedidaService unidadService;

    @GetMapping()
    public String listarProductos(Model model) {
        model.addAttribute("recetas", recetaService.listar());
        return "admin/producto/recetas";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        RecetaProducto receta = new RecetaProducto();
        // agregar un detalle vacío para el formulario
        receta.getDetalles().add(new DetalleRecetaProducto());
        model.addAttribute("receta", receta);
        model.addAttribute("productos", productoService.listarProductos());
        model.addAttribute("insumos", insumoService.listarInsumos());
        model.addAttribute("unidades", unidadService.listarUnidades());
        return "admin/producto/recetasFormulario";
    }

    // 🟠 EDITAR RECETA
    @GetMapping("/editar/{id}")
    public String editarReceta(@PathVariable("id") Long id, Model model) {
        RecetaProducto receta = recetaService.buscarPorId(id);
        if (receta == null) {
            return "redirect:/admin/receta/listar";
        }
        model.addAttribute("receta", receta);
        model.addAttribute("productos", productoService.listarProductos());
        model.addAttribute("insumos", insumoService.listarInsumos());
        model.addAttribute("unidades", unidadService.listarUnidades());
        return "admin/producto/recetasFormulario";
    }

    @PostMapping("/guardar")
    public String guardar(@ModelAttribute RecetaProducto receta, RedirectAttributes redirectAttributes) {
        try {
            recetaService.guardar(receta);
            redirectAttributes.addFlashAttribute("mensaje", "Receta guardada correctamente.");
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Error al guardar la receta: " + e.getMessage());
            return "redirect:/admin/recetas/nuevo";
        }
        return "redirect:/admin/recetas";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id) {
        recetaService.eliminar(id);
        return "redirect:/admin/recetas";
    }

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(Insumo.class, new PropertyEditorSupport() {
            @Override
            public void setAsText(String text) {
                if (text == null || text.isEmpty()) {
                    setValue(null);
                } else {
                    Insumo insumo = new Insumo();
                    insumo.setInsumoId(Long.valueOf(text));
                    setValue(insumo);
                }
            }
        });

        binder.registerCustomEditor(UnidadMedida.class, new PropertyEditorSupport() {
            @Override
            public void setAsText(String text) {
                if (text == null || text.isEmpty()) {
                    setValue(null);
                } else {
                    UnidadMedida u = new UnidadMedida();
                    u.setId(Integer.valueOf(text));
                    setValue(u);
                }
            }
        });

        binder.registerCustomEditor(Producto.class, new PropertyEditorSupport() {
            @Override
            public void setAsText(String text) {
                if (text == null || text.isEmpty()) {
                    setValue(null);
                } else {
                    Producto p = new Producto();
                    p.setId(Long.valueOf(text));
                    setValue(p);
                }
            }
        });
    }

}
