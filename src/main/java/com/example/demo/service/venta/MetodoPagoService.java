package com.example.demo.service.venta;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.entity.venta.MetodoPago;
import com.example.demo.repository.ventaa.MetodoPagoRepository;

@Service
public class MetodoPagoService {
    @Autowired
    private MetodoPagoRepository metodoPagoRepository;

    public List<MetodoPago> listarMetodos() {
        return metodoPagoRepository.findAll();
    }
}
