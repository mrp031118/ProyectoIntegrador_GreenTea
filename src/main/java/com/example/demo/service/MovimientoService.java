package com.example.demo.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.entity.Movimiento;
import com.example.demo.repository.MovimientoRepository;

@Service
public class MovimientoService {

    @Autowired
    private MovimientoRepository movimientoRepository;

    public MovimientoService(MovimientoRepository movimientoRepository) {
        this.movimientoRepository = movimientoRepository;
    }

    public Movimiento registrarMovimiento(Movimiento movimiento) {
        return movimientoRepository.save(movimiento);
    }

    public void eliminarMovimiento(Long id) {
        movimientoRepository.deleteById(id);
    }

    public List<Movimiento> listarMovimientos() {
        return movimientoRepository.findAll();
    }
}
