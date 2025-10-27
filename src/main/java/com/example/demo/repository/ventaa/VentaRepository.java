package com.example.demo.repository.ventaa;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.venta.Venta;

public interface VentaRepository extends JpaRepository<Venta, Long> {

}
