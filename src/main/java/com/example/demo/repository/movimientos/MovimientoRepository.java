package com.example.demo.repository.movimientos;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.movimientos.Movimiento;

public interface MovimientoRepository extends JpaRepository<Movimiento,Long>{


}
