package com.example.demo.controller.cliente;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.demo.entity.cliente.Cliente;
import com.example.demo.service.cliente.ClienteService;

@Controller
@RequestMapping("/empleado/cliente")
public class ClienteController {

    @Autowired
    private ClienteService clienteService;

    // mostrar clientes
    @GetMapping()
    public String listarclientes(Model model) {
        model.addAttribute("clientes", clienteService.listarTodos());
        model.addAttribute("nuevoCliente", new Cliente());
        return "empleado/cliente/clientesLista";
    }

    // Guardar nuevo cliente
    @PostMapping("/guardar")
    public String guardarCliente(@ModelAttribute("nuevoCliente") Cliente cliente,
            RedirectAttributes redirectAttributes) {
        try {
            clienteService.guardar(cliente);
            redirectAttributes.addFlashAttribute("mensaje", "Cliente registrado correctamente");
            redirectAttributes.addFlashAttribute("tipo", "success");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensaje", "Error al registrar el cliente");
            redirectAttributes.addFlashAttribute("tipo", "error");
        }
        return "redirect:/empleado/cliente";
    }

    // Editar cliente
    @GetMapping("/editar/{id}")
    public String editarCliente(@PathVariable Integer id, Model model) {
        Cliente cliente = clienteService.buscarPorId(id);
        model.addAttribute("cliente", cliente);
        return "empleado/cliente/clienteFormulario";
    }

    @PostMapping("/actualizar")
    public String actualizarCliente(@ModelAttribute Cliente cliente,
            RedirectAttributes redirectAttributes) {
        try {
            clienteService.guardar(cliente);
            redirectAttributes.addFlashAttribute("mensaje", "Cliente actualizado correctamente");
            redirectAttributes.addFlashAttribute("tipo", "success");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensaje", "Error al actualizar el cliente");
            redirectAttributes.addFlashAttribute("tipo", "error");
        }
        return "redirect:/empleado/cliente";
    }

    // Eliminar cliente
    @GetMapping("/eliminar/{id}")
    public String eliminarCliente(@PathVariable Integer id,
            RedirectAttributes redirectAttributes) {
        try {
            clienteService.eliminar(id);
            redirectAttributes.addFlashAttribute("mensaje", "Cliente eliminado correctamente");
            redirectAttributes.addFlashAttribute("tipo", "success");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensaje", "Error al eliminar el cliente");
            redirectAttributes.addFlashAttribute("tipo", "error");
        }
        return "redirect:/empleado/cliente";
    }

}
