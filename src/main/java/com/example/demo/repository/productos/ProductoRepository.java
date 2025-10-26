package com.example.demo.repository.productos;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.productos.Producto;

public interface ProductoRepository extends JpaRepository<Producto, Long> {

}
