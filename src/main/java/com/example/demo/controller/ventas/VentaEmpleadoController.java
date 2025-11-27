package com.example.demo.controller.ventas;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.demo.entity.cliente.Cliente;
import com.example.demo.entity.productos.Producto;
import com.example.demo.entity.usuarios.User;
import com.example.demo.entity.venta.MetodoPago;
import com.example.demo.entity.venta.Venta;
import com.example.demo.repository.ventaa.MetodoPagoRepository;
import com.example.demo.repository.ventaa.VentaRepository;
import com.example.demo.service.cliente.ClienteService;
import com.example.demo.service.productos.ProductoService;
import com.example.demo.service.usuarios.CustomUserDetails;
import com.example.demo.service.venta.MetodoPagoService;
import com.example.demo.service.venta.VentaService;

import org.springframework.security.core.Authentication;

@Controller
@RequestMapping("/empleado/ventas")
public class VentaEmpleadoController {

    @Autowired
    private VentaService ventaService;

    @Autowired
    private VentaRepository ventaRepository;
    @Autowired
    private ProductoService productoService;
    @Autowired
    private MetodoPagoRepository metodoPagoRepository;
    @Autowired
    private MetodoPagoService metodoPagoService;
    @Autowired
    private ClienteService clienteService;

    @GetMapping("/registrar")
    public String mostrarFormulario(Model model) {
        // Obtener usuario logueado
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
        User empleado = userDetails.getUser();

        // Productos y métodos de pago
        List<Producto> productos = productoService.listarProductos();
        List<MetodoPago> metodosPago = metodoPagoService.listarMetodos();
        List<Cliente> clientes = clienteService.listarTodos(); // Traer clientes de BD

        model.addAttribute("productos", productos);
        model.addAttribute("metodosPago", metodosPago);
        model.addAttribute("empleado", empleado);
        model.addAttribute("clientes", clientes);
        return "empleado/venta/ventas";
    }

    @PostMapping("/registrar")
    public String procesarVenta( // Cambia a String para redirigir
            @RequestParam Long empleadoId,
            @RequestParam(required = false) Integer clienteId,
            @RequestParam(required = false) String clienteNombre,
            @RequestParam Long metodoPagoId,
            @RequestParam List<Long> productoIds,
            @RequestParam List<Double> cantidades,
            RedirectAttributes redirectAttributes) {

        try {
            MetodoPago metodoPago = metodoPagoRepository.findById(metodoPagoId).orElse(null);

            List<VentaService.ProductoCantidad> lista = new java.util.ArrayList<>();
            for (int i = 0; i < productoIds.size(); i++) {
                Producto p = productoService.obtenerPorId(productoIds.get(i));
                if (p != null) {
                    lista.add(new VentaService.ProductoCantidad(p, cantidades.get(i)));
                }
            }

            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
            User empleado = userDetails.getUser();

            // Registrar venta
            ventaService.registrarVenta(
                    empleado,
                    (clienteId != null ? clienteId : null),
                    (clienteNombre != null && !clienteNombre.isBlank() ? clienteNombre : null),
                    metodoPago,
                    lista);

            // Obtener la venta guardada
            Venta ventaGuardada = ventaRepository.findTopByOrderByIdDesc();

            // Generar PDF y guardarlo temporalmente (o pasarlo como base64)
            byte[] pdfBytes = ventaService.generarComprobantePDF(ventaGuardada);
            String pdfBase64 = java.util.Base64.getEncoder().encodeToString(pdfBytes);

            // Pasar el PDF como atributo para descarga automática
            redirectAttributes.addFlashAttribute("pdfBase64", pdfBase64);
            redirectAttributes.addFlashAttribute("ventaId", ventaGuardada.getId());
            redirectAttributes.addFlashAttribute("tipo", "success");
            redirectAttributes.addFlashAttribute("mensaje",
                    "Venta registrada exitosamente. Descargando comprobante...");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("tipo", "error");
            redirectAttributes.addFlashAttribute("mensaje", "Error: " + e.getMessage());
        }

        return "redirect:/empleado/ventas/registrar";
    }
}
