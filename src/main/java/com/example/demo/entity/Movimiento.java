package com.example.demo.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "movimientos")
@ToString(exclude = {"insumo", "lote", "tipoMovimiento"})
@EqualsAndHashCode(exclude = {"insumo", "lote", "tipoMovimiento"})
public class Movimiento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long movimientoId;

    //Relación con Insumo
    @ManyToOne
    @JoinColumn(name = "insumo_id")
    private Insumo insumo;

    // Relación con Tipos de movimientos de Kardex
    @ManyToOne
    @JoinColumn(name = "tipo_movimiento_id")
    private TipoMovimientoKardex tipoMovimiento;

    private LocalDateTime fecha;

    private BigDecimal cantidad;
    private BigDecimal costoUnitario;
    private BigDecimal total;

    // Relación con Lote
    @ManyToOne
    @JoinColumn(name = "lote_id")
    private Lote lote;

    private String observaciones;
}
