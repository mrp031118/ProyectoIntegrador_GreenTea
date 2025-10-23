package com.example.demo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "unidad_medida")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UnidadMedida {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 50)
    private  String nombre;

    // Método para mostrar abreviaturas
    public String getAbreviatura() {
        if (nombre == null) return "";
        return switch (nombre.trim().toLowerCase()) {
            case "kilogramo" -> "kg";
            case "litro" -> "L";
            case "unidad" -> "unid";
            case "paquete" -> "pqt";
            default -> nombre; // En caso de que haya otras medidas
        };
    }
}
