package com.example.demo.service.insumos;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.entity.insumos.ConversionUnidad;
import com.example.demo.entity.insumos.UnidadMedida;
import com.example.demo.repository.insumos.ConversionUnidadRepository;

@Service
public class ConversionUnidadService {

    @Autowired
    private ConversionUnidadRepository conversionUnidadRepository;

    /**
     * Convierte cantidad desde unidad origen hasta unidad destino.
     * Si no existe una conversión directa, puedes intentar la inversa.
     */
    public BigDecimal convertir(BigDecimal cantidad, UnidadMedida origen, UnidadMedida destino) {
        if (origen.getId().equals(destino.getId()))
            return cantidad;

        Optional<ConversionUnidad> direct = conversionUnidadRepository.findByUnidadOrigenAndUnidadDestino(origen,
                destino);
        if (direct.isPresent()) {
            return cantidad.multiply(direct.get().getFactorConversion());
        }
        // intentar inversa
        Optional<ConversionUnidad> inv = conversionUnidadRepository.findByUnidadOrigenAndUnidadDestino(destino, origen);
        if (inv.isPresent()) {
            // factor inverso = 1 / factor
            BigDecimal factorInv = BigDecimal.ONE.divide(inv.get().getFactorConversion(), 8, RoundingMode.HALF_UP);
            return cantidad.multiply(factorInv);
        }
        throw new IllegalStateException(
                "No existe factor de conversión entre " + origen.getNombre() + " y " + destino.getNombre());
    }

}
