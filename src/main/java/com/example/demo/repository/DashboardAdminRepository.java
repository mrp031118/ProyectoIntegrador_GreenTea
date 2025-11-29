package com.example.demo.repository;

import java.util.List;
import java.util.Map;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.entity.venta.Venta;

import org.springframework.stereotype.Repository;

@Repository
@Transactional(readOnly = true)
public interface DashboardAdminRepository extends JpaRepository<Venta, Long> {

    // 1. Total ventas hoy
    @Query(value = "SELECT COALESCE(SUM(total), 0) FROM ventas WHERE DATE(fecha) = CURDATE()", nativeQuery = true)
    Double getVentasHoy();

    // 2. Costo de ventas hoy (COGS)
    @Query(value = """
                SELECT COALESCE(SUM(total), 0)
                FROM movimientos
                WHERE tipo_movimiento_id IN (2,4)
                AND DATE(fecha) = CURDATE()
            """, nativeQuery = true)
    Double getCostoVentasHoy();

    // 3. Insumos en stock crítico
    @Query(value = """
                SELECT i.nombre AS nombre, v.stock_actual AS stock_actual
                FROM vista_stock_actual_insumo v
                JOIN insumos i ON v.insumo_id = i.insumo_id
                WHERE v.stock_actual < i.stock_minimo
            """, nativeQuery = true)
    List<Map<String, Object>> getStockCritico();

    // 4. Top 5 productos más vendidos
    @Query(value = """
                SELECT p.nombre, SUM(d.cantidad) AS cantidad
                FROM detalle_venta d
                JOIN productos p ON d.producto_id = p.id  -- Corregido: p.id (de productos)
                JOIN ventas v ON v.id = d.venta_id
                WHERE v.fecha >= (CURDATE() - INTERVAL 7 DAY)
                GROUP BY p.nombre
                ORDER BY cantidad DESC
                LIMIT 5
            """, nativeQuery = true)
    List<Map<String, Object>> getTopProductos();

    // 5. Ventas de la ultima semana
    @Query(value = """
                SELECT DATE(fecha) AS fecha, SUM(total) AS total
                FROM ventas
                WHERE fecha >= (CURDATE() - INTERVAL 7 DAY)
                GROUP BY DATE(fecha)
                ORDER BY fecha ASC
            """, nativeQuery = true)
    List<Map<String, Object>> getVentasUltimaSemana();

    // 6. Próximos vencimientos
    @Query(value = """
                SELECT l.lote_id, i.nombre AS insumo, l.fecha_vencimiento
                FROM lotes l
                JOIN insumos i ON l.insumo_id = i.insumo_id
                ORDER BY l.fecha_vencimiento ASC
                LIMIT 5
            """, nativeQuery = true)
    List<Map<String, Object>> getProximosVencimientos();

    // DESDE AQUI ES PARA REPORTES

    // 7. Reporte de Inventario y Stock Crítico
    @Query(value = """
                SELECT i.nombre AS nombre_insumo, v.stock_actual, i.stock_minimo
                FROM vista_stock_actual_insumo v
                JOIN insumos i ON v.insumo_id = i.insumo_id
                WHERE v.stock_actual < i.stock_minimo
                ORDER BY v.stock_actual ASC
            """, nativeQuery = true)
    List<Map<String, Object>> getReporteStockCritico();

    // 8. Reporte de Kardex y Costo de Inventario (PEPS) - Usando la vista
    // kardex_lote_peps
    @Query(value = """
                SELECT lote_id, insumo, fecha_entrada, fecha_vencimiento, costo_unitario, cantidad_inicial, cantidad_usada, cantidad_disponible
                FROM kardex_lote_peps
                WHERE (:fechaInicio IS NULL OR fecha_entrada >= :fechaInicio)
                  AND (:fechaFin IS NULL OR fecha_entrada <= :fechaFin)
                ORDER BY insumo_id ASC, fecha_entrada ASC
            """, nativeQuery = true)
    List<Map<String, Object>> getReporteKardex(@Param("fechaInicio") String fechaInicio,
            @Param("fechaFin") String fechaFin);

    // Rentabilidad por producto (usando tipo_control y movimientos)
    @Query(value = """
            SELECT
                p.nombre AS producto,
                p.precio AS precio_venta,

                -- Costo unitario calculado según el tipo de control
                CASE
                    WHEN cp.tipo_control = 'ELABORADO' THEN (
                        -- Para elaborados tomamos el costo_unitario promedio de producción
                        SELECT COALESCE(AVG(mp.costo_unitario), 0)
                        FROM movimientos_productos mp
                        WHERE mp.producto_id = p.id
                          AND mp.tipo_movimiento_id = 1  -- ENTRADA por producción
                    )
                    WHEN cp.tipo_control = 'INSTANTANEO' THEN (
                        -- Para instantáneos usamos los movimientos de insumos PEPS de las ventas
                        SELECT
                            COALESCE(SUM(m.total), 0) / NULLIF(SUM(dv.cantidad), 0)
                        FROM detalle_venta dv
                        JOIN ventas v ON v.id = dv.venta_id
                        JOIN movimientos m
                            ON m.tipo_movimiento_id = 2
                           AND m.observaciones LIKE CONCAT('Venta producto instantáneo: ', p.nombre, '%')
                        WHERE dv.producto_id = p.id
                          AND (:fechaInicio IS NULL OR v.fecha >= :fechaInicio)
                          AND (:fechaFin IS NULL OR v.fecha <= :fechaFin)
                    )
                    ELSE 0
                END AS costo_unitario,

                -- Cantidad vendida en el rango
                COALESCE((
                    SELECT SUM(dv.cantidad)
                    FROM detalle_venta dv
                    JOIN ventas v2 ON v2.id = dv.venta_id
                    WHERE dv.producto_id = p.id
                      AND (:fechaInicio IS NULL OR v2.fecha >= :fechaInicio)
                      AND (:fechaFin IS NULL OR v2.fecha <= :fechaFin)
                ), 0) AS cantidad_vendida

            FROM productos p
            LEFT JOIN categorias_productos cp ON cp.id = p.categoria_id
            ORDER BY p.nombre ASC
            """, nativeQuery = true)
    List<Object[]> getRentabilidadProductos(@Param("fechaInicio") String fechaInicio,
            @Param("fechaFin") String fechaFin);

    // Merma por producto
    @Query(value = """
                SELECT
                    p.nombre AS producto,
                    SUM(mp.cantidad) AS cantidad_total
                FROM merma_producto mp
                JOIN productos p ON mp.producto_id = p.id
                WHERE (:fechaInicio IS NULL OR mp.fecha >= :fechaInicio)
                  AND (:fechaFin IS NULL OR mp.fecha <= :fechaFin)
                GROUP BY mp.producto_id
                ORDER BY p.nombre ASC
            """, nativeQuery = true)
    List<Object[]> getMermasPorProducto(@Param("fechaInicio") String fechaInicio, @Param("fechaFin") String fechaFin);
}
