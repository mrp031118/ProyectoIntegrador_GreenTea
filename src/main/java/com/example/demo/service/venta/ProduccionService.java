package com.example.demo.service.venta;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.entity.lotes.Lote;
import com.example.demo.entity.produccion.Produccion;
import com.example.demo.entity.productos.DetalleRecetaProducto;
import com.example.demo.entity.productos.Producto;
import com.example.demo.entity.productos.RecetaProducto;
import com.example.demo.entity.usuarios.User;
import com.example.demo.repository.lotes.LoteRepository;
import com.example.demo.repository.productos.RecetaProductoRepository;
import com.example.demo.repository.ventaa.ProduccionRepository;
import com.example.demo.service.movimientos.MermaProductoService;
import com.example.demo.service.movimientos.MovimientoProductoService;
import com.example.demo.service.movimientos.MovimientoService;
import com.example.demo.service.productos.ProductoService;

import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;

@Service
public class ProduccionService {
    @Autowired
    private ProduccionRepository produccionRepository;

    @Autowired
    private MovimientoProductoService movimientoProductoService;

    @Autowired
    private MermaProductoService mermaProductoService;

    @Autowired
    private MovimientoService movimientoInsumoService;

    @Autowired
    private ProductoService productoService;

    @Autowired
    private LoteRepository loteRepository;

    @Autowired
    private RecetaProductoRepository recetaProductoRepository;

    private static final int PORCIONES_POR_UNIDAD = 10; // 🔹 Valor fijo para fraccionables

    @PostConstruct
    public void init() {
        System.out.println("🟩 ProduccionService CARGADO por Spring");
    }

    @Transactional
    public void registrarProduccion(Producto producto, double cantidadElaborada, User empleado) {
        System.out.println("➡️ INICIO registrarProduccion()");
        // 🔹 NUEVA VALIDACIÓN: Verificar fechas de vencimiento de lotes antes de
        // proceder
        RecetaProducto receta = recetaProductoRepository.findByProducto_Id(producto.getId()).orElse(null);
        if (receta == null) {
            throw new RuntimeException("El producto '" + producto.getNombre()
                    + "' no tiene una receta asociada. No se puede registrar producción.");
        }
        for (DetalleRecetaProducto drp : receta.getDetalles()) {
            // Verificar si hay lotes NO vencidos para este insumo
            List<Lote> lotesDisponibles = loteRepository.findByInsumo_InsumoIdAndFechaVencimientoGreaterThanEqual(
                    drp.getInsumo().getInsumoId(), LocalDateTime.now().toLocalDate()); // Asumiendo fecha_vencimiento es
                                                                                       // LocalDate
            if (lotesDisponibles.isEmpty()) {
                throw new RuntimeException("El insumo '" + drp.getInsumo().getNombre()
                        + "' solo tiene lotes vencidos o ninguno disponible. No se puede registrar producción.");
            }
        }
        // Si pasa la validación, proceder con el resto del método
        System.out.println("Producto recibido: " + producto);

        double cantidadProducida = producto.getFraccionable()
                ? cantidadElaborada * PORCIONES_POR_UNIDAD
                : cantidadElaborada;

        System.out.println("Cantidad producida final: " + cantidadProducida);

        Produccion produccion = new Produccion();
        produccion.setProducto(producto);
        produccion.setFecha(LocalDateTime.now());
        produccion.setEmpleado(empleado);

        System.out.println("Empleado: " + empleado.getId());

        try {
            double costoLote = movimientoInsumoService.calcularCostoPorReceta(producto, cantidadElaborada);
            System.out.println("Costo lote: " + costoLote);

            produccion.setCostoUnitarioLote(BigDecimal.valueOf(costoLote / cantidadProducida));
            produccion.setCantidadElaborada(BigDecimal.valueOf(cantidadElaborada));
            produccion.setCantidadProducida(BigDecimal.valueOf(cantidadProducida));
            produccion.setSaldoActual(BigDecimal.valueOf(cantidadProducida));

            produccionRepository.save(produccion);
            // Actualizar stockActual del producto
            productoService.actualizarStockActual(producto);
            System.out.println("✅ PRODUCCIÓN GUARDADA CON ID: " + produccion.getProduccionId());

            double factorReceta = cantidadElaborada;

            System.out.println("Factor receta: " + factorReceta);

            movimientoInsumoService.descontarInsumosPorProduccion(producto, factorReceta, produccion);
            System.out.println("➡️ Insumos descontados");

            movimientoProductoService.registrarEntrada(
                    produccion,
                    producto,
                    cantidadProducida,
                    costoLote / cantidadProducida);

            System.out.println("➡️ Movimiento de producto registrado");

        } catch (Exception ex) {
            System.out.println("❌ ERROR EN registrarProduccion()");
            ex.printStackTrace();
            throw ex; // <-- IMPORTANTE, para que rollback funcione
        }

        System.out.println("✔️ FIN registrarProduccion()");
    }

    // En ProduccionService.java
    @Transactional
    public void registrarMermaProducto(Producto producto, double cantidadMerma, String motivo, User empleado) {
        System.out.println("➡️ Iniciando registrarMermaProducto producto="
                + (producto == null ? "NULL" : producto.getNombre()) + " cantidadMerma=" + cantidadMerma + " motivo="
                + motivo);

        if (producto == null) {
            throw new RuntimeException("Producto nulo al intentar registrar merma");
        }

        if (cantidadMerma <= 0) {
            throw new RuntimeException("La cantidad de merma debe ser mayor a cero.");
        }

        String tipoControl = producto.getCategoria() == null ? "" : producto.getCategoria().getTipoControl();
        if (tipoControl == null)
            tipoControl = "";

        tipoControl = tipoControl.trim().toUpperCase();
        System.out.println("Tipo control detectado: " + tipoControl);

        switch (tipoControl) {
            case "ELABORADO":
                movimientoProductoService.descontarStockProductoElaborado(producto, cantidadMerma, motivo);

                break;
            case "INSTANTANEO":
                movimientoInsumoService.descontarInsumosPorRecetaMerma(producto, cantidadMerma, motivo);

                mermaProductoService.registrarMermaProducto(producto, cantidadMerma, motivo, empleado);
                break;
            default:
                throw new RuntimeException("El tipo de control del producto no es válido: " + tipoControl);
        }

        System.out.println("✔️ registrarMermaProducto finalizado para producto=" + producto.getNombre());

    }

    public boolean tieneReceta(Producto producto) {
        return recetaProductoRepository.existsByProducto(producto);
    }

    public double obtenerStockActualProducto(Producto producto) {
        return produccionRepository.sumSaldoActualByProductoId(producto.getId());
    }

    public double obtenerStockDisponibleInstantaneo(Producto producto) {

        RecetaProducto receta = recetaProductoRepository
                .findByProducto_Id(producto.getId())
                .orElse(null);

        if (receta == null)
            return 0;

        double minimo = Double.MAX_VALUE;

        for (DetalleRecetaProducto det : receta.getDetalles()) {

            double stockInsumo = movimientoInsumoService.obtenerStockActualInsumo(
                    det.getInsumo().getInsumoId());

            if (stockInsumo <= 0)
                return 0;

            // Cantidad requerida por unidad (BigDecimal → double)
            double cantidadNecesaria = det.getCantidad().doubleValue();

            double unidades = stockInsumo / cantidadNecesaria;

            minimo = Math.min(minimo, unidades);
        }

        return minimo == Double.MAX_VALUE ? 0 : minimo;
    }

}
