package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.demo.dto.KardexLoteProjection;
import com.example.demo.entity.Lote;

public interface LoteRepository extends JpaRepository<Lote, Long>{
    @Query(value = "SELECT lote_id AS loteId, insumo_id AS insumoId, insumo, unidad_medida AS unidadMedida, " +
                   "proveedor, costo_unitario AS costoUnitario, cantidad_inicial AS cantidadInicial, " +
                   "cantidad_usada AS cantidadUsada, cantidad_disponible AS cantidadDisponible " +
                   "FROM kardex_lote_peps",
           nativeQuery = true)
    List<KardexLoteProjection> findAllKardexLotePeps();
}
