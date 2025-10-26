package com.example.demo.repository.productos;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.productos.RecetaProducto;

public interface RecetaProductoRepository extends JpaRepository<RecetaProducto, Long>{
    Optional<RecetaProducto> findByProducto_Id(Long productoId);
}
