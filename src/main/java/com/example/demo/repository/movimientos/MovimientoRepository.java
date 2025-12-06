package com.example.demo.repository.movimientos;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.demo.entity.movimientos.Movimiento;

public interface MovimientoRepository extends JpaRepository<Movimiento, Long> {

        // Método para encontrar el último movimiento de un insumo, ordenado por fecha
        // descendente
        Optional<Movimiento> findTopByInsumo_InsumoIdOrderByFechaDesc(Long insumoId);

        @Query("SELECT COALESCE(SUM(m.cantidad), 0) " +
                        "FROM Movimiento m " +
                        "WHERE m.insumo.insumoId = :id " +
                        "AND m.tipoMovimiento.id = 1")
        Double sumEntradas(@Param("id") int id);

        @Query("SELECT COALESCE(SUM(m.cantidad), 0) " +
                        "FROM Movimiento m " +
                        "WHERE m.insumo.insumoId = :id " +
                        "AND m.tipoMovimiento.id IN (2,4)")
        Double sumSalidas(@Param("id") int id);

        List<Movimiento> findByInsumo_InsumoId(Long insumoId);

}
