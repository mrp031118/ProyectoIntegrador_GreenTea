package com.example.demo.repository.lotes;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.demo.dto.KardexLoteProjection;
import com.example.demo.entity.insumos.Insumo;
import com.example.demo.entity.lotes.Lote;
import com.example.demo.entity.proveedores.Proveedor;

public interface LoteRepository extends JpaRepository<Lote, Long> {
    // Buscar por proveedor (entidad completa)
    List<Lote> findByProveedor(Proveedor proveedor);

    // Buscar por insumo (si también quieres filtrar por insumo)
    List<Lote> findByInsumo(Insumo insumo);

    // Buscar por proveedor e insumo
    List<Lote> findByProveedorAndInsumo(Proveedor proveedor, Insumo insumo);

    // Buscar por nombre de insumo (opcional, si lo quieres para búsqueda rápida)
    List<Lote> findByInsumo_NombreContainingIgnoreCase(String nombreInsumo);

    @Query(value = "SELECT lote_id AS loteId, insumo_id AS insumoId, insumo, " +
            "unidad_medida AS unidadMedida, proveedor, " +
            "costo_unitario AS costoUnitario, " +
            "cantidad_inicial AS cantidadInicial, " +
            "cantidad_usada AS cantidadUsada, " +
            "cantidad_disponible AS cantidadDisponible, " +
            "fecha_entrada AS fechaEntrada, " +
            "fecha_vencimiento AS fechaVencimiento " +
            "FROM kardex_lote_peps", nativeQuery = true)
    List<KardexLoteProjection> findAllKardexLotePeps();
}
