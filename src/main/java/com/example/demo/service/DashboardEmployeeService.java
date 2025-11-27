package com.example.demo.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.example.demo.repository.DashboardEmployeeRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DashboardEmployeeService {
    private final DashboardEmployeeRepository repo;

    // Métodos que llaman al repo con el empleadoId
    public Double getMontoTotalVendidoHoy(Long empleadoId) {
        return repo.getMontoTotalVendidoHoy(empleadoId);
    }

    public Long getNumeroTransaccionesHoy(Long empleadoId) {
        return repo.getNumeroTransaccionesHoy(empleadoId);
    }

    public Long getClientesAtendidosHoy(Long empleadoId) {
        return repo.getClientesAtendidosHoy(empleadoId);
    }

    public Double getTotalMermasHoy(Long empleadoId) {
        return repo.getTotalMermasHoy(empleadoId);
    }

    public List<Map<String, Object>> getUltimas5Ventas(Long empleadoId) {
        return repo.getUltimas5Ventas(empleadoId);
    }

    public List<Map<String, Object>> getTop3ProductosHoy(Long empleadoId) {
        return repo.getTop3ProductosHoy(empleadoId);
    }
}
