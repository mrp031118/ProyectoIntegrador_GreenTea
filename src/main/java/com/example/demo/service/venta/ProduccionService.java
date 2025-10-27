package com.example.demo.service.venta;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.entity.movimientos.Movimiento;
import com.example.demo.entity.movimientos.TipoMovimientoKardex;
import com.example.demo.entity.productos.DetalleRecetaProducto;
import com.example.demo.entity.productos.Producto;
import com.example.demo.entity.productos.RecetaProducto;
import com.example.demo.repository.movimientos.MovimientoRepository;
import com.example.demo.repository.productos.RecetaProductoRepository;

import jakarta.transaction.Transactional;

@Service
public class ProduccionService {
     @Autowired
    private MovimientoRepository movimientoRepository;

    @Autowired
    private RecetaProductoRepository recetaProductoRepository;

    @Transactional
    public void registrarProduccion(Producto producto, Double cantidad) {
        RecetaProducto receta = recetaProductoRepository.findByProducto_Id(producto.getId())
                .orElse(null);
        if (receta == null) return;

        for (DetalleRecetaProducto drp : receta.getDetalles()) {
            double cantidadRequerida = drp.getCantidad().doubleValue() * cantidad;

            // ⚙️ Registrar descuento de insumo en movimientos (PEPS)
            Movimiento mov = new Movimiento();
            mov.setInsumo(drp.getInsumo());
            mov.setCantidad(cantidadRequerida);
            mov.setFecha(LocalDateTime.now());
            TipoMovimientoKardex tipoSalida = new TipoMovimientoKardex();
            tipoSalida.setId(2); // SALIDA
            tipoSalida.setNombre("SALIDA");
            mov.setTipoMovimiento(tipoSalida);
            mov.setObservaciones("Producción de producto elaborado: " + producto.getNombre());
            movimientoRepository.save(mov);
        }
    }
}
