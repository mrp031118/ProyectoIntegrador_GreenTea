package com.example.demo.entity.produccion;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.example.demo.entity.productos.Producto;
import com.example.demo.entity.usuarios.User;

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
@Table(name = "produccion")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Produccion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long produccionId;

    @ManyToOne
    @JoinColumn(name = "producto_id")
    private Producto producto;

    @Column(nullable = false)
    private LocalDateTime fecha;

    @Column(name = "cantidad_elaborada", nullable = false, precision = 8, scale = 2)
    private BigDecimal cantidadElaborada;

    @Column(name = "cantidad_producida", nullable = false, precision = 8, scale = 2)
    private BigDecimal cantidadProducida;

    @Column(name = "saldo_actual", nullable = false, precision = 8, scale = 2)
    private BigDecimal saldoActual;

    @Column(name = "costo_unitario_lote", nullable = false, precision = 10, scale = 4)
    private BigDecimal costoUnitarioLote;

    @ManyToOne
    @JoinColumn(name = "empleado_id")
    private User empleado;

}
