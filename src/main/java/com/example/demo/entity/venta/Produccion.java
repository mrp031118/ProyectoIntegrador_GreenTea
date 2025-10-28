package com.example.demo.entity.venta;

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

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "produccion")
public class Produccion {
    // Entidad que registra la producción de un producto, incluyendo cantidad, costo y empleado responsable.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "produccion_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @Column(name = "fecha")
    private LocalDateTime fechaRegistro;

    @Column(name = "cantidad_producida")
    private Double cantidad;

    @Column(name = "costo_unitario_lote")
    private Double costoUnitarioLote;

    @ManyToOne
    @JoinColumn(name = "empleado_id")
    private User empleado;

}
