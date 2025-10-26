package com.example.demo.service.movimientos;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.entity.movimientos.TipoMovimientoKardex;
import com.example.demo.repository.movimientos.TipoMovimientoRepository;

@Service
public class TipoMovimientoService {
    @Autowired
    private TipoMovimientoRepository tipoMovimientoRepository;

    // Listar todos los tipos
    public List<TipoMovimientoKardex> listarTipos() {
        return tipoMovimientoRepository.findAll();
    }

    // Obtener por id
    public TipoMovimientoKardex obtenerPorId(Integer id) {
        return tipoMovimientoRepository.findById(id).orElse(null);
    }
}
