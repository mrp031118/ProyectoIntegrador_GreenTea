package com.example.demo.entity.movimientos;

import com.example.demo.entity.insumos.Insumo;
import com.example.demo.entity.lotes.Lote;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "movimientos")
@ToString(exclude = { "insumo", "lote", "tipoMovimiento" })
@EqualsAndHashCode(exclude = { "insumo", "lote", "tipoMovimiento" })
public class Movimiento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // autoincremental según MySQL
    private Long movimientoId;

    @ManyToOne
    @JoinColumn(name = "insumo_id", nullable = false)
    private Insumo insumo;

    @ManyToOne
    @JoinColumn(name = "tipo_movimiento_id", nullable = false)
    private TipoMovimientoKardex tipoMovimiento;

    private java.util.Date fecha;
    private Double cantidad;
    private Double costoUnitario;
    private Double total;

    @ManyToOne
    @JoinColumn(name = "lote_id")
    private Lote lote;

    private String observaciones;

    @Transient
    private Double saldoCantidad;

    @Transient
    private Double saldoValor;
}
