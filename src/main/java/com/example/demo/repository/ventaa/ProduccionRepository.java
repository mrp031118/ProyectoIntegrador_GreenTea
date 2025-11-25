package com.example.demo.repository.ventaa;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.demo.entity.produccion.Produccion;
import com.example.demo.entity.productos.Producto;

public interface ProduccionRepository extends JpaRepository<Produccion, Long> {
    List<Produccion> findByProductoOrderByFechaAsc(Producto producto);

    // Suma de saldoActual por producto
    @Query("SELECT COALESCE(SUM(p.saldoActual), 0) FROM Produccion p WHERE p.producto.id = :productoId")
    Double sumSaldoActualByProducto(@Param("productoId") Long productoId);
}
