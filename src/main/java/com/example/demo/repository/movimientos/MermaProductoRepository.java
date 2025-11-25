package com.example.demo.repository.movimientos;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.produccion.MermaProducto;
import com.example.demo.entity.usuarios.User;

public interface MermaProductoRepository extends JpaRepository<MermaProducto, Long>{
    List<MermaProducto> findByEmpleado(User empleado);
}
