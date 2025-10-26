package com.example.demo.repository.insumos;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.demo.dto.StockActualProjection;
import com.example.demo.entity.insumos.Insumo;

public interface StockActualRepository extends JpaRepository<Insumo, Long>{
    @Query(value = "SELECT insumo_id AS insumoId, insumo, unidad_medida AS unidadMedida, " +
            "stock_actual AS stockActual " +
            "FROM vista_stock_actual_insumo", nativeQuery = true)
    List<StockActualProjection> findAllStockActual();
}
