package com.example.demo.dto;

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
}
