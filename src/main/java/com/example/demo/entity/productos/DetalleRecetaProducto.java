package com.example.demo.entity.productos;

import java.math.BigDecimal;

import com.example.demo.entity.insumos.Insumo;
import com.example.demo.entity.insumos.UnidadMedida;

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
@Table(name = "detalle_receta_producto")
public class DetalleRecetaProducto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "receta_producto_id")
    private RecetaProducto recetaProducto;

    @ManyToOne
    @JoinColumn(name = "insumo_id")
    private Insumo insumo;

    // cantidad tal como la define la receta (ej 10.00)
    @Column(precision = 8, scale = 2)
    private BigDecimal cantidad;

    @ManyToOne
    @JoinColumn(name = "unidad_medida_id")
    private UnidadMedida unidadMedida;
    
}
