package com.example.demo.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.entity.UnidadMedida;
import com.example.demo.repository.UnidadMedidaRepository;

@Service
public class UnidadMedidaService {
    @Autowired
    private UnidadMedidaRepository unidadMedidaRepository;

    public List<UnidadMedida> listarUnidades(){
        return unidadMedidaRepository.findAll();
    }

    public UnidadMedida obtenerPorId(Long id){
        return unidadMedidaRepository.findById(id).orElse(null);
    }
}
