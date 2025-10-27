package com.example.demo.repository.ventaa;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.venta.DetalleVenta;

public interface DetalleVentaRepository extends JpaRepository<DetalleVenta, Long> {

}
