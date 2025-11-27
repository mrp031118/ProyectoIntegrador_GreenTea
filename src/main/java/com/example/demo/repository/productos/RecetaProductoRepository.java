package com.example.demo.repository.productos;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.demo.entity.productos.RecetaProducto;

public interface RecetaProductoRepository extends JpaRepository<RecetaProducto, Long> {
    Optional<RecetaProducto> findByProducto_Id(Long productoId);

    @Query(value = """
            SELECT r.*
            FROM recetas_producto r
            INNER JOIN productos p ON r.producto_id = p.id
            WHERE LOWER(p.nombre) LIKE LOWER(CONCAT('%', :nombreProducto, '%'))
            """, nativeQuery = true)
    List<RecetaProducto> buscarPorNombreProducto(@Param("nombreProducto") String nombreProducto);

}
