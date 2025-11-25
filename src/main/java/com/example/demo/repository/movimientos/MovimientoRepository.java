package com.example.demo.repository.movimientos;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.movimientos.Movimiento;

public interface MovimientoRepository extends JpaRepository<Movimiento, Long> {

    // Método para encontrar el último movimiento de un insumo, ordenado por fecha
    // descendente
    Optional<Movimiento> findTopByInsumo_InsumoIdOrderByFechaDesc(Long insumoId);

}
