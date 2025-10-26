package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DisponibilidadDTO {

    private String insumo;
    private Double cantidadRequerida;
    private Double cantidadDisponible;
    private Boolean disponible;
}
