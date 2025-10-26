package com.example.demo.repository.productos;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.productos.DetalleRecetaProducto;

public interface DetalleRecetaProductoRepository extends JpaRepository<DetalleRecetaProducto, Long>{

}
