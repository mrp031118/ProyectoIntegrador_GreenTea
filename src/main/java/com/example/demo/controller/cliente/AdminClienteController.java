package com.example.demo.controller.cliente;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.demo.service.cliente.ClienteService;

@Controller
@RequestMapping("/admin/cliente")
public class AdminClienteController {
@Autowired
    private ClienteService clienteService;

    @GetMapping
    public String listarClientesAdmin(Model model) {
        model.addAttribute("clientes", clienteService.listarTodos());
        return "admin/cliente/clientesAdmin";
    }

    @PostMapping("/eliminar/{id}")
    public String eliminarCliente(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            clienteService.eliminar(id);
            redirectAttributes.addFlashAttribute("mensaje", "Cliente eliminado correctamente");
            redirectAttributes.addFlashAttribute("tipo", "success");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensaje", "Error al eliminar el cliente");
            redirectAttributes.addFlashAttribute("tipo", "error");
        }
        return "redirect:/admin/cliente";
    }
}
