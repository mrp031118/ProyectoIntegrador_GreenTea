package com.example.demo.repository.movimientos;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.movimientos.TipoMovimientoKardex;

public interface TipoMovimientoRepository extends JpaRepository<TipoMovimientoKardex, Integer>{

}
