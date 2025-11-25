package com.example.demo.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class MermaDTO {
    private String productoNombre;
    private double cantidad;
    private LocalDateTime fecha;
    private String motivo;
    private String empleadoNombre;
}
