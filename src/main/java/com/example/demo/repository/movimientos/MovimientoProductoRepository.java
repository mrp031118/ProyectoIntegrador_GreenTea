package com.example.demo.repository.movimientos;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.movimientos.MovimientoProducto;
import com.example.demo.entity.movimientos.TipoMovimientoKardex;
import com.example.demo.entity.usuarios.User;

public interface MovimientoProductoRepository extends JpaRepository<MovimientoProducto, Long> {
    List<MovimientoProducto> findByProductoIdOrderByFechaAsc(Long productoId);

    List<MovimientoProducto> findAllByOrderByFechaAsc();

    List<MovimientoProducto> findAllByTipoMovimientoId(TipoMovimientoKardex tipoMovimiento);

    // Nuevo método para buscar mermas por empleado y tipo MERMA (id=4)
    List<MovimientoProducto> findByEmpleadoAndTipoMovimientoId_Id(User empleado, Integer tipoId);

}
