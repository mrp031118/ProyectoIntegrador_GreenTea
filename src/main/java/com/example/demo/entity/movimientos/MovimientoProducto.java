package com.example.demo.entity.movimientos;

import java.time.LocalDateTime;

import com.example.demo.entity.productos.Producto;
import com.example.demo.entity.usuarios.User;
import com.example.demo.entity.produccion.Produccion;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "movimientos_productos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MovimientoProducto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "producto_id")
    private Producto producto;

    @Column(nullable = false)
    private LocalDateTime fecha;

    @ManyToOne(optional = false)
    @JoinColumn(name = "tipo_movimiento_id", nullable = false)
    private TipoMovimientoKardex tipoMovimientoId; // 1=ENTRADA, 2=SALIDA

    @Column(nullable = false)
    private Double cantidad;

    @Column(name = "costo_unitario", nullable = false)
    private Double costoUnitario;

    @ManyToOne
    @JoinColumn(name = "produccion_id")
    private Produccion produccion;

    @ManyToOne
    @JoinColumn(name = "empleado_id")
    private User empleado;

    @Column(length = 255)
    private String observaciones;

    // Agregar en MovimientoProducto.java
    @Transient
    private Double saldoCantidad;

    @Transient
    private Double saldoValor;
}
