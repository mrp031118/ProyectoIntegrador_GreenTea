package com.example.demo.service.venta;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Service;

import com.example.demo.dto.StockActualProjection;
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
import com.example.demo.repository.movimientos.MovimientoRepository;
import com.example.demo.repository.productos.RecetaProductoRepository;
import com.example.demo.repository.ventaa.VentaRepository;
import com.example.demo.service.insumos.ConversionUnidadService;
import com.example.demo.service.insumos.StockActualService;
import com.example.demo.service.movimientos.MovimientoService;

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
    private ConversionUnidadService conversionUnidadService;

    @Autowired
    private ProduccionService produccionService; // Para consultar stock si es necesario

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
            Double subtotal = pc.producto.getPrecio() * pc.cantidad;
            detalle.setTotal(BigDecimal.valueOf(subtotal));
            venta.getDetalles().add(detalle);

            totalVenta = totalVenta.add(BigDecimal.valueOf(pc.producto.getPrecio())
                    .multiply(BigDecimal.valueOf(pc.cantidad)));

            // 2️⃣ Obtener receta del producto
            RecetaProducto receta = recetaProductoRepository.findByProducto_Id(pc.producto.getId())
                    .orElse(null);
            if (receta == null)
                continue;

            String tipoControl = pc.producto.getCategoria().getTipoControl();

            if ("instantaneo".equalsIgnoreCase(tipoControl)) {
                // 🔹 Descuento directo de insumos usando FIFO por lotes
                for (DetalleRecetaProducto drp : receta.getDetalles()) {
                    BigDecimal cantidadRequerida = drp.getCantidad().multiply(BigDecimal.valueOf(pc.cantidad));

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

                    double restante = cantidadParaRestar.doubleValue();
                    List<Lote> lotes = drp.getInsumo().getLotes();
                    lotes.sort(Comparator.comparing(Lote::getFechaEntrada)); // FIFO

                    for (Lote lote : lotes) {
                        if (restante <= 0)
                            break;

                        double cantidadDesdeLote = Math.min(lote.getCantidad(), restante);

                        Movimiento mov = new Movimiento();
                        mov.setInsumo(drp.getInsumo());
                        mov.setCantidad(cantidadDesdeLote);
                        mov.setCostoUnitario(lote.getCostoUnitario()); // costo real del lote
                        mov.setTotal(lote.getCostoUnitario() * cantidadDesdeLote);
                        mov.setFecha(LocalDateTime.now());

                        TipoMovimientoKardex tipoSalida = new TipoMovimientoKardex();
                        tipoSalida.setId(2); // SALIDA
                        tipoSalida.setNombre("SALIDA");
                        mov.setTipoMovimiento(tipoSalida);
                        mov.setObservaciones("Venta producto instantáneo: " + pc.producto.getNombre());

                        movimientoRepository.save(mov);

                        // Restar del lote
                        lote.setCantidad(lote.getCantidad() - cantidadDesdeLote);
                        restante -= cantidadDesdeLote;
                    }
                }
            } else if ("elaborado".equalsIgnoreCase(tipoControl)) {
                // 🔹 Registrar producción del día y descontar insumos
                produccionService.registrarProduccion(pc.producto, pc.cantidad);
            }
        }

        venta.setTotal(totalVenta);
        ventaRepository.save(venta);
    }

}
