package com.example.demo.entity;

import java.time.LocalDate;
import java.util.ArrayList;
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
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "recetas_producto")
public class RecetaProducto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // referencia al producto final
    @ManyToOne
    @JoinColumn(name = "producto_id")
    private Producto producto;

    @Column(name = "nombre_receta", length = 100)
    private String nombreReceta;

    @Column(name = "fecha_creacion")
    private LocalDate fechaCreacion;

    @OneToMany(mappedBy = "recetaProducto", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetalleRecetaProducto> detalles = new ArrayList<>();

    // helpers
    public void addDetalle(DetalleRecetaProducto d) {
        d.setRecetaProducto(this);
        this.detalles.add(d);
    }

    public void removeDetalle(DetalleRecetaProducto d) {
        d.setRecetaProducto(null);
        this.detalles.remove(d);
    }
    
}
