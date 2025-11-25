package com.example.demo.entity.produccion;

import java.time.LocalDateTime;

import com.example.demo.entity.productos.Producto;
import com.example.demo.entity.usuarios.User;

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
@Table(name = "merma_producto")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MermaProducto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;
    private double cantidad; // Cantidad total mermada del producto 
    private LocalDateTime fecha;
    private String motivo;
    @ManyToOne
    @JoinColumn(name = "empleado_id", nullable = false)
    private User empleado;
}
