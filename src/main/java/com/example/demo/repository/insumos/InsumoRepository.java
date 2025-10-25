package com.example.demo.repository.insumos;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.Insumo;
import com.example.demo.entity.UnidadMedida;

public interface InsumoRepository extends JpaRepository<Insumo, Long>{
    // búsqueda por nombre, ignorando mayúsculas
    List<Insumo> findByNombreContainingIgnoreCase(String nombre); 
    
    // Buscar insumos por unidad de medida y nombre
    List<Insumo> findByUnidadMedidaAndNombreContainingIgnoreCase(UnidadMedida unidadMedida, String nombre);

}
