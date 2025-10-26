package com.example.demo.repository.insumos;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.insumos.ConversionUnidad;
import com.example.demo.entity.insumos.UnidadMedida;

public interface ConversionUnidadRepository extends JpaRepository<ConversionUnidad, Long>{
     Optional<ConversionUnidad> findByUnidadOrigenAndUnidadDestino(UnidadMedida origen, UnidadMedida destino);
}
