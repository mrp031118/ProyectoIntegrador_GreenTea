package com.example.demo.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.demo.repository.DashboardAdminRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DashboardAdminService {

    private final DashboardAdminRepository repo;

    public Double getVentasHoy() {
        
        return repo.getVentasHoy();
    }

    public Double getCostoVentasHoy() {
        return repo.getCostoVentasHoy();
    }

    public Double getMargenBrutoHoy() {
        return getVentasHoy() - getCostoVentasHoy();
    }

    public List<Map<String, Object>> getStockCritico() { // Cambiado a List<Map<String, Object>>
        return repo.getStockCritico();
    }

    public List<Map<String, Object>> getTopProductos() {
        return repo.getTopProductos();
    }

    public List<Map<String, Object>> getVentasUltimaSemana() {
        return repo.getVentasUltimaSemana();
    }

    public List<Map<String, Object>> getProximosVencimientos() {
        return repo.getProximosVencimientos();
    }

    // PARTE PARA REPORTES
    public List<Map<String, Object>> getReporteStockCritico() {
        return repo.getReporteStockCritico();
    }

    public List<Map<String, Object>> getReporteKardex(String fechaInicio, String fechaFin) {
        return repo.getReporteKardex(fechaInicio, fechaFin);
    }

    public List<Map<String, Object>> getReporteRentabilidad(String fechaInicio, String fechaFin) {
        return repo.getRentabilidadProductos(fechaInicio, fechaFin).stream().map(r -> {
            double precioVenta = r[1] != null ? ((Number) r[1]).doubleValue() : 0;
            double costoUnitario = r[2] != null ? ((Number) r[2]).doubleValue() : 0;
            int cantidad = r[3] != null ? ((Number) r[3]).intValue() : 0;
            double margenUnitario = precioVenta - costoUnitario;
            double margenTotal = margenUnitario * cantidad;
            double rentabilidadPorcentaje = 0;
            if (precioVenta > 0) {
                rentabilidadPorcentaje = (margenUnitario / precioVenta) * 100;
            }

            return Map.of(
                    "producto", r[0],
                    "precio_venta", precioVenta,
                    "costo_unitario", costoUnitario,
                    "margen_unitario", margenUnitario,
                    "margen_total", margenTotal,
                    "cantidad_vendida", cantidad,
                    "rentabilidad_porcentaje", rentabilidadPorcentaje);
        }).collect(Collectors.toList());
    }

    public List<Map<String, Object>> getReporteMermas(String fechaInicio, String fechaFin) {
        return repo.getMermasPorProducto(fechaInicio, fechaFin).stream().map(r -> Map.of(
                "producto", r[0],
                "cantidad_total", r[1] != null ? ((Number) r[1]).doubleValue() : 0)).collect(Collectors.toList());
    }

}
