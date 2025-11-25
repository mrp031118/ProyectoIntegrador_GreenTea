package com.example.demo.service.venta;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.example.demo.dto.KardexLoteProjection;
import com.example.demo.entity.cliente.Cliente;
import com.example.demo.entity.insumos.UnidadMedida;
import com.example.demo.entity.lotes.Lote;
import com.example.demo.entity.movimientos.Movimiento;
import com.example.demo.entity.movimientos.TipoMovimientoKardex;
import com.example.demo.entity.productos.DetalleRecetaProducto;
import com.example.demo.entity.productos.Producto;
import com.example.demo.entity.productos.RecetaProducto;
import com.example.demo.entity.usuarios.User;
import com.example.demo.entity.venta.DetalleVenta;
import com.example.demo.entity.venta.MetodoPago;
import com.example.demo.entity.venta.Venta;
import com.example.demo.repository.cliente.ClienteRepository;
import com.example.demo.repository.lotes.LoteRepository;
import com.example.demo.repository.movimientos.MovimientoRepository;
import com.example.demo.repository.productos.RecetaProductoRepository;
import com.example.demo.repository.ventaa.VentaRepository;
import com.example.demo.service.insumos.ConversionUnidadService;
import com.example.demo.service.movimientos.MovimientoProductoService;
import com.example.demo.service.usuarios.CustomUserDetails;

import jakarta.transaction.Transactional;

@Service
public class VentaService {

    @Autowired
    private VentaRepository ventaRepository;
    @Autowired
    private RecetaProductoRepository recetaProductoRepository;

    @Autowired
    private MovimientoRepository movimientoRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private LoteRepository loteRepository;
    @Autowired
    private ConversionUnidadService conversionUnidadService;

    @Autowired
    private MovimientoProductoService movimientoProductoService; // Para consultar stock si es necesario

    // Método para listar todas las ventas
    public List<Venta> listarTodasVentas() {
        return ventaRepository.findAll();
    }

    // DTO interno para pasar producto + cantidad
    public static class ProductoCantidad {
        public Producto producto;
        public Double cantidad;

        public ProductoCantidad(Producto producto, Double cantidad) {
            this.producto = producto;
            this.cantidad = cantidad;
        }
    }

    @Transactional
    public void registrarVenta(User empleado, Integer clienteId, String clienteNombre, MetodoPago metodoPago,
            List<ProductoCantidad> lista) {

        Venta venta = new Venta();
        venta.setEmpleado(empleado);

        // Buscar cliente por ID
        Cliente clienteObj = null;
        if (clienteId != null) {
            clienteObj = clienteRepository.findById(clienteId).orElse(null);
        }
        venta.setCliente(clienteObj);
        venta.setMetodoPago(metodoPago);
        venta.setFecha(LocalDateTime.now());

        BigDecimal totalVenta = BigDecimal.ZERO;

        for (ProductoCantidad pc : lista) {

            // 1️⃣ Crear detalle de venta
            DetalleVenta detalle = new DetalleVenta();
            detalle.setProducto(pc.producto);
            detalle.setCantidad(pc.cantidad);
            detalle.setPrecioUnidad(BigDecimal.valueOf(pc.producto.getPrecio()));
            detalle.setVenta(venta);
            BigDecimal subtotal = BigDecimal.valueOf(pc.producto.getPrecio())
                    .multiply(BigDecimal.valueOf(pc.cantidad));
            detalle.setTotal(subtotal);
            venta.getDetalles().add(detalle);
            totalVenta = totalVenta.add(subtotal);

            // 2️⃣ Obtener receta del producto
            RecetaProducto receta = recetaProductoRepository.findByProducto_Id(pc.producto.getId())
                    .orElse(null);
            if (receta == null)
                continue;

            String tipoControl = pc.producto.getCategoria().getTipoControl();

            if ("instantaneo".equalsIgnoreCase(tipoControl)) {
                // 🔹 Descuento de insumos usando FIFO con vista kardex
                for (DetalleRecetaProducto drp : receta.getDetalles()) {
                    BigDecimal cantidadRequerida = drp.getCantidad()
                            .multiply(BigDecimal.valueOf(pc.cantidad));

                    // Convertir unidades si es necesario
                    UnidadMedida unidadReceta = drp.getUnidadMedida();
                    UnidadMedida unidadStock = drp.getInsumo().getUnidadMedida();
                    BigDecimal cantidadParaRestar = cantidadRequerida;

                    if (!unidadReceta.getId().equals(unidadStock.getId())) {
                        try {
                            cantidadParaRestar = conversionUnidadService.convertir(cantidadRequerida, unidadReceta,
                                    unidadStock);
                        } catch (Exception e) {
                            cantidadParaRestar = BigDecimal.ZERO;
                        }
                    }

                    BigDecimal restante = cantidadParaRestar;

                    // 🔹 Obtener lotes disponibles desde la vista Kardex
                    List<KardexLoteProjection> lotesDisponibles = loteRepository
                            .findAllKardexLotePeps().stream()
                            .filter(k -> k.getInsumoId().equals(drp.getInsumo().getInsumoId())
                                    && k.getCantidadDisponible() > 0)
                            .sorted((a, b) -> a.getFechaEntrada().compareTo(b.getFechaEntrada()))
                            .toList();

                    for (KardexLoteProjection loteKardex : lotesDisponibles) {
                        if (restante.compareTo(BigDecimal.ZERO) <= 0)
                            break;

                        BigDecimal cantidadDisponible = BigDecimal.valueOf(loteKardex.getCantidadDisponible());
                        BigDecimal cantidadDesdeLote = cantidadDisponible.min(restante);

                        // Registrar movimiento
                        Movimiento mov = new Movimiento();
                        mov.setInsumo(drp.getInsumo());
                        mov.setCantidad(cantidadDesdeLote.doubleValue());
                        mov.setCostoUnitario(loteKardex.getCostoUnitario());
                        mov.setTotal(cantidadDesdeLote.multiply(BigDecimal.valueOf(loteKardex.getCostoUnitario()))
                                .doubleValue());
                        mov.setFecha(LocalDateTime.now());
                        TipoMovimientoKardex tipoSalida = new TipoMovimientoKardex();
                        tipoSalida.setId(2);
                        tipoSalida.setNombre("SALIDA");
                        mov.setTipoMovimiento(tipoSalida);

                        // Asociar lote real
                        Lote loteReal = loteRepository.findById(loteKardex.getLoteId()).orElseThrow();
                        mov.setLote(loteReal);
                        mov.setObservaciones("Venta producto instantáneo: " + pc.producto.getNombre());
                        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                        CustomUserDetails cud = (CustomUserDetails) auth.getPrincipal();
                        User usuarioActual = cud.getUser();
                        mov.setUsuario(usuarioActual);
                        movimientoRepository.save(mov);

                        // Reducir restante
                        restante = restante.subtract(cantidadDesdeLote);
                    }

                    if (restante.compareTo(BigDecimal.ZERO) > 0) {
                        System.out.println("⚠️ Insumo " + drp.getInsumo().getNombre()
                                + " no tiene suficiente stock. Falta: " + restante + " " + unidadStock.getNombre());
                    }
                }
            } else if ("elaborado".equalsIgnoreCase(tipoControl)) {
                movimientoProductoService.descontarPorVenta(detalle.getProducto(), detalle.getCantidad());
            }
        }

        venta.setTotal(totalVenta);
        ventaRepository.save(venta);
        System.out.println("✅ Venta registrada con total: " + totalVenta);
    }

}
