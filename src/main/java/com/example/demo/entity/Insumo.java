package com.example.demo.entity;

import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "insumos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"lotes", "movimientos", "unidadMedida"})
@EqualsAndHashCode(exclude = {"lotes", "movimientos", "unidadMedida"})
public class Insumo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long insumoId;

    @Column(nullable = false, length = 100)
    private String nombre;

    private Integer stockMinimo;


    // Relación con Unidad de Medida
    @ManyToOne
    @JoinColumn(name = "unidad_medida_id")
    private UnidadMedida unidadMedida;

    // Relación con Lote
    @OneToMany(mappedBy = "insumo", cascade = CascadeType.ALL)
    private List<Lote> lotes;

    // Relación con movimientos
    @OneToMany(mappedBy = "insumo", cascade = CascadeType.ALL)
    private List<Movimiento> movimientos;
}
