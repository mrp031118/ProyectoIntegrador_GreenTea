package com.example.demo.entity.productos;

import java.util.ArrayList;
import java.util.List;

import com.example.demo.entity.categorias.CategoriaProducto;
import com.example.demo.entity.insumos.UnidadConversion;

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
import lombok.ToString;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "productos")
public class Producto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;

    private Double precio;

    private Boolean fraccionable;

    @ManyToOne
    @JoinColumn(name = "categoria_id")
    private CategoriaProducto categoria;

    @ManyToOne
    @JoinColumn(name = "unidad_conversion_id")
    private UnidadConversion unidadConversion;

    @Column(name = "stock_actual")
    private Double stockActual;

    @Column(name = "stock_minimo")
    private Double stockMinimo;

    @OneToMany(mappedBy = "producto", cascade = CascadeType.ALL)
    @ToString.Exclude
    private List<RecetaProducto> recetas = new ArrayList<>();
}
