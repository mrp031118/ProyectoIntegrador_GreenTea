package com.example.demo.entity;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.CascadeType;
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
@Table(name = "lotes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"insumo", "proveedor", "movimientos"})
@EqualsAndHashCode(exclude = {"insumo", "proveedor", "movimientos"})
public class Lote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long loteId;

    @ManyToOne
    @JoinColumn(name = "insumo_id", nullable = false)
    private Insumo insumo;

    // Relación con proveedor
    @ManyToOne
    @JoinColumn(name = "proveedor_id")
    private Proveedor proveedor;

    // Se inserta la fecha automáticamente
    private LocalDateTime fechaEntrada = LocalDateTime.now();

    private Double cantidad;

    private Double cantidadDisponible;

    private Double costoUnitario;

    private String observaciones;

    // Relación con Movimientos
    @OneToMany(mappedBy = "lote", cascade = CascadeType.ALL)
    private List<Movimiento> movimientos;

}
