package com.example.demo.service.movimientos;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.entity.movimientos.MovimientoProducto;
import com.example.demo.entity.produccion.Produccion;
import com.example.demo.entity.productos.Producto;
import com.example.demo.repository.movimientos.MovimientoProductoRepository;
import com.example.demo.repository.movimientos.TipoMovimientoRepository;
import com.example.demo.repository.ventaa.ProduccionRepository;
import com.example.demo.service.productos.ProductoService;

import jakarta.transaction.Transactional;

@Service
public class MovimientoProductoService {
    @Autowired
    private MovimientoProductoRepository movimientoProductoRepository;

    @Autowired
    private TipoMovimientoRepository tipoMovimientoRepository;

    @Autowired
    private ProduccionRepository produccionRepository;

    @Autowired
    private ProductoService productoService;

    // Registrar entrada de producto terminado
    public void registrarEntrada(
            Produccion produccion,
            Producto producto,
            double cantidadProducida,
            double costoUnitarioReal) {

        MovimientoProducto mp = new MovimientoProducto();

        mp.setProducto(producto);
        mp.setCantidad(cantidadProducida);
        mp.setCostoUnitario(costoUnitarioReal); // ✔ costo real por unidad
        mp.setFecha(LocalDateTime.now());
        mp.setTipoMovimientoId(tipoMovimientoRepository.getReferenceById(1)); // ✔ ENTRADA
        mp.setProduccion(produccion);
        mp.setEmpleado(produccion.getEmpleado());
        mp.setObservaciones("Producción registrada");

        movimientoProductoRepository.save(mp);
    }

    // Método para listar todos los movimientos con PEPS
    public List<MovimientoProducto> listarTodosMovimientosPEPS() {
        // Trae todos los movimientos ordenados por fecha ascendente
        List<MovimientoProducto> movimientos = movimientoProductoRepository.findAllByOrderByFechaAsc();

        double saldoCantidad = 0;
        double saldoValor = 0;

        for (MovimientoProducto mov : movimientos) {
            if (mov.getTipoMovimientoId().getId() == 1) { // Entrada
                saldoCantidad += mov.getCantidad();
                saldoValor += mov.getCantidad() * mov.getCostoUnitario();
            } else { // Salida
                saldoCantidad -= mov.getCantidad();
                saldoValor -= mov.getCantidad() * mov.getCostoUnitario();
            }
            mov.setSaldoCantidad(saldoCantidad);
            mov.setSaldoValor(saldoValor);
        }

        return movimientos;
    }

    public void descontarPorVenta(Producto producto, double cantidadVendida) {
        // Buscar producciones del producto ordenadas por fecha (PEPS)
        List<Produccion> producciones = produccionRepository
                .findByProductoOrderByFechaAsc(producto);

        double restante = cantidadVendida;

        for (Produccion prod : producciones) {
            if (restante <= 0)
                break;

            double saldo = prod.getSaldoActual().doubleValue();
            if (saldo <= 0)
                continue;

            double cantidadDesdeLote = Math.min(saldo, restante);

            // Descontar del lote
            prod.setSaldoActual(BigDecimal.valueOf(saldo - cantidadDesdeLote));
            produccionRepository.save(prod);

            // Registrar salida en MOVIMIENTOS_PRODUCTOS
            registrarSalidaProducto(prod, producto, cantidadDesdeLote);

            restante -= cantidadDesdeLote;
        }

        if (restante > 0) {
            System.out.println("⚠️ NO HAY SUFICIENTE STOCK DE PRODUCTO TERMINADO");
        }

        productoService.actualizarStockActual(producto);
    }

    public void registrarSalidaProducto(
            Produccion produccion,
            Producto producto,
            double cantidad) {
        MovimientoProducto mp = new MovimientoProducto();

        mp.setProducto(producto);
        mp.setFecha(LocalDateTime.now());
        mp.setTipoMovimientoId(tipoMovimientoRepository.getReferenceById(2)); // SALIDA
        mp.setCantidad(cantidad);
        mp.setCostoUnitario(produccion.getCostoUnitarioLote().doubleValue()); // costo del lote original
        mp.setProduccion(produccion);
        mp.setEmpleado(produccion.getEmpleado());
        mp.setObservaciones("Venta de producto terminado");

        movimientoProductoRepository.save(mp);
    }

    @Transactional
    public void descontarStockProductoElaborado(Producto producto, double cantidadMerma, String motivo) {

        System.out.println("➡️ descontarStockProductoElaborado producto=" + producto.getNombre() + " cantidadMerma="
                + cantidadMerma + " motivo=" + motivo);

        List<Produccion> producciones = produccionRepository
                .findByProductoOrderByFechaAsc(producto);

        double restante = cantidadMerma;

        // validar tipoMovimiento MERMA
        var tipoMerma = tipoMovimientoRepository.findById(4).orElse(null);
        if (tipoMerma == null) {
            throw new RuntimeException("Tipo movimiento MERMA (id=4) no existe en BD");
        }

        for (Produccion prod : producciones) {
            if (restante <= 0)
                break;
            double saldo = prod.getSaldoActual() == null ? 0 : prod.getSaldoActual().doubleValue();
            if (saldo <= 0)
                continue;

            double cantidadDesdeLote = Math.min(saldo, restante);

            // descontar del lote (PRODUCCION)
            prod.setSaldoActual(BigDecimal.valueOf(saldo - cantidadDesdeLote));
            produccionRepository.save(prod);

            // Registrar salida como MERMA
            MovimientoProducto mp = new MovimientoProducto();
            mp.setProducto(producto);
            mp.setFecha(LocalDateTime.now());
            mp.setTipoMovimientoId(tipoMerma);
            mp.setCantidad(cantidadDesdeLote);
            mp.setCostoUnitario(prod.getCostoUnitarioLote().doubleValue());
            mp.setProduccion(prod);
            mp.setEmpleado(prod.getEmpleado());
            mp.setObservaciones("Merma de producto elaborado" + motivo);
            System.out.println(" > Guardando MERMA producto=" + producto.getNombre() + " cantidad=" + cantidadDesdeLote
                    + " produccionId=" + prod.getProduccionId() + " motivo=" + motivo);

            movimientoProductoRepository.save(mp);

            System.out.println(" ✔ MERMA registrada en movimientos_productos id generado");

            restante -= cantidadDesdeLote;
        }

        if (restante > 0) {
            System.out.println("⚠️ NO HAY SUFICIENTE STOCK PARA LA MERMA DE PRODUCTO ELABORADO, falta=" + restante);
        }
        productoService.actualizarStockActual(producto);
    }
}
