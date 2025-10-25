package com.example.demo.entity;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "conversion_unidades")
public class ConversionUnidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "unidad_origen_id")
    private UnidadMedida unidadOrigen;

    @ManyToOne
    @JoinColumn(name = "unidad_destino_id")
    private UnidadMedida unidadDestino;

    @Column(name = "factor_conversion", precision = 18, scale = 6)
    private BigDecimal factorConversion;
    
}
