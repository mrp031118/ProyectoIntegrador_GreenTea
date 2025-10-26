package com.example.demo.controller.cliente;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.demo.entity.cliente.Cliente;
import com.example.demo.service.cliente.ClienteService;


@Controller
@RequestMapping("/empleado/cliente")
public class ClienteController {

    @Autowired
     private ClienteService clienteService;

    //mostrar clientes
    @GetMapping()
    public String listarclientes(Model model) {
        model.addAttribute("clientes", clienteService.listarTodos());
        model.addAttribute("nuevoCliente", new Cliente());
        return "empleado/cliente/clientesLista"; 
    }

    //Guardar nuevo cliente
    @PostMapping("/guardar")
    public String guardarCliente(@ModelAttribute("nuevoCliente") Cliente cliente) {
        clienteService.guardar(cliente);
        return "redirect:/empleado/cliente";
    }

    //Editar cliente
    @GetMapping("/editar/{id}")
    public String editarCliente(@PathVariable Integer id, Model model) {
        Cliente cliente = clienteService.buscarPorId(id);
        model.addAttribute("cliente", cliente);
        return "empleado/cliente/clienteFormulario"; 
    }

    @PostMapping("/actualizar")
    public String actualizarCliente(@ModelAttribute Cliente cliente) {
        clienteService.guardar(cliente);
        return "redirect:/empleado/cliente";
    }

    //Eliminar cliente
    @GetMapping("/eliminar/{id}")
    public String eliminarCliente(@PathVariable Integer id) {
        clienteService.eliminar(id);
        return "redirect:/empleado/cliente";
    }    

}
