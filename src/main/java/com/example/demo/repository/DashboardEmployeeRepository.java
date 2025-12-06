package com.example.demo.repository;

import java.util.List;
import java.util.Map;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import com.example.demo.entity.venta.Venta;

@Repository
@Transactional(readOnly = true)
public interface DashboardEmployeeRepository extends JpaRepository<Venta, Long> {

        // 1. Monto Total Vendido Hoy (filtrado por empleado_id)
        @Query(value = "SELECT COALESCE(SUM(total), 0) FROM ventas WHERE DATE(fecha) = CURDATE() AND empleado_id = :empleadoId", nativeQuery = true)
        Double getMontoTotalVendidoHoy(@Param("empleadoId") Long empleadoId);

        // 2. Número de Transacciones Hoy (conteo de ventas filtrado por empleado_id)
        @Query(value = "SELECT COUNT(*) FROM ventas WHERE DATE(fecha) = CURDATE() AND empleado_id = :empleadoId", nativeQuery = true)
        Long getNumeroTransaccionesHoy(@Param("empleadoId") Long empleadoId);

        @Query(value = "SELECT " +
                        "(SELECT COUNT(DISTINCT cliente_id) " +
                        " FROM ventas " +
                        " WHERE DATE(fecha) = CURDATE() " +
                        "   AND empleado_id = :empleadoId " +
                        "   AND cliente_id IS NOT NULL) " +
                        " + " +
                        "(SELECT COUNT(*) " +
                        " FROM ventas " +
                        " WHERE DATE(fecha) = CURDATE() " +
                        "   AND empleado_id = :empleadoId " +
                        "   AND cliente_id IS NULL)", nativeQuery = true)
        Long getClientesAtendidosHoy(@Param("empleadoId") Long empleadoId);

        @Query(value = """
                            SELECT COALESCE(SUM(cantidad), 0)
                            FROM merma_producto
                            WHERE DATE(CONVERT_TZ(fecha, '+00:00', '-05:00')) =
                                  DATE(CONVERT_TZ(NOW(), '+00:00', '-05:00'))
                              AND empleado_id = :empleadoId
                        """, nativeQuery = true)
        Double getTotalMermasHoy(@Param("empleadoId") Long empleadoId);

        // 5. Últimas 5 Ventas Registradas (filtrado por empleado_id)
        @Query(value = "SELECT id, total, fecha FROM ventas WHERE empleado_id = :empleadoId ORDER BY fecha DESC LIMIT 5", nativeQuery = true)
        List<Map<String, Object>> getUltimas5Ventas(@Param("empleadoId") Long empleadoId);

        // 6. Top 3 Productos del Día (productos más vendidos hoy por cantidad, filtrado
        // por empleado_id)
        @Query(value = """
                            SELECT p.nombre, SUM(d.cantidad) AS cantidad
                            FROM detalle_venta d
                            JOIN productos p ON d.producto_id = p.id
                            JOIN ventas v ON v.id = d.venta_id
                            WHERE DATE(v.fecha) = CURDATE() AND v.empleado_id = :empleadoId
                            GROUP BY p.nombre
                            ORDER BY cantidad DESC
                            LIMIT 3
                        """, nativeQuery = true)
        List<Map<String, Object>> getTop3ProductosHoy(@Param("empleadoId") Long empleadoId);
}
