package com.example.demo.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

// interfaz que va con la vista creada en la BD para mostrar datos del lote
public interface KardexLoteProjection {
    Long getLoteId();
    Long getInsumoId();
    String getInsumo();
    String getUnidadMedida();
    String getProveedor();
    Double getCostoUnitario();
    Integer getCantidadInicial();
    Integer getCantidadUsada();
    Integer getCantidadDisponible();
    LocalDateTime getFechaEntrada();
    LocalDate getFechaVencimiento();
}
