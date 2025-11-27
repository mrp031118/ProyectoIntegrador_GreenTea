package com.example.demo.controller.productos;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.demo.entity.productos.Producto;
import com.example.demo.repository.categorias.CategoriaProductoRepository;
import com.example.demo.repository.productos.UnidadConversionRepository;
import com.example.demo.service.productos.ProductoService;

@Controller
@RequestMapping("/admin/productos")
public class ProductoController {

    @Autowired
    private ProductoService productoService;

    @Autowired
    private CategoriaProductoRepository categoriaProductoRepository;

    @Autowired
    private UnidadConversionRepository unidadConversionRepository;

    // Mostrar lista
    @GetMapping
    public String listar(Model model) {
        model.addAttribute("productos", productoService.listarProductos());
        return "admin/producto/productosLista";
    }

    // Formulario nuevo
    @GetMapping("/nuevo")
    public String nuevoProducto(Model model) {
        model.addAttribute("producto", new Producto());
        model.addAttribute("categorias", categoriaProductoRepository.findAll());
        model.addAttribute("unidades", unidadConversionRepository.findAll());
        return "admin/producto/productosFormulario";
    }

    // Guardar producto nuevo
    @PostMapping("/guardar")
    public String guardar(@ModelAttribute Producto producto, Model model) {

        try {
            productoService.guardar(producto);
            model.addAttribute("tipo", "success");
            model.addAttribute("mensaje", "Producto registrado correctamente.");
        } catch (Exception e) {
            model.addAttribute("tipo", "error");
            model.addAttribute("mensaje", "Error al guardar producto: " + e.getMessage());
        }

        model.addAttribute("productos", productoService.listarProductos());
        return "admin/producto/productosLista";
    }

    // Formulario de edición
    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {

        try {
            model.addAttribute("producto", productoService.obtenerPorId(id));
            cargarListas(model);
        } catch (Exception e) {
            model.addAttribute("tipo", "error");
            model.addAttribute("mensaje", "Producto no encontrado.");
            model.addAttribute("productos", productoService.listarProductos());
            return "admin/producto/productosLista";
        }

        return "admin/producto/productosFormulario";
    }

    // Actualizar producto existente
    @PostMapping("/actualizar")
    public String actualizar(@ModelAttribute Producto producto, Model model) {

        try {
            productoService.guardar(producto);
            model.addAttribute("tipo", "success");
            model.addAttribute("mensaje", "Producto actualizado correctamente.");
        } catch (Exception e) {
            model.addAttribute("tipo", "error");
            model.addAttribute("mensaje", "Error al actualizar producto: " + e.getMessage());

            cargarListas(model);
            model.addAttribute("producto", producto);
            return "admin/producto/productosFormulario";
        }

        model.addAttribute("productos", productoService.listarProductos());
        return "admin/producto/productosLista";
    }

    // Eliminar producto
    @GetMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id, Model model) {

        try {
            productoService.eliminar(id);
            model.addAttribute("tipo", "success");
            model.addAttribute("mensaje", "Producto eliminado correctamente.");
        } catch (Exception e) {
            model.addAttribute("tipo", "error");
            model.addAttribute("mensaje", "No se pudo eliminar el producto: " + e.getMessage());
        }

        model.addAttribute("productos", productoService.listarProductos());
        return "admin/producto/productosLista";
    }

    private void cargarListas(Model model) {
        model.addAttribute("categorias", categoriaProductoRepository.findAll());
        model.addAttribute("unidades", unidadConversionRepository.findAll());
    }
}
