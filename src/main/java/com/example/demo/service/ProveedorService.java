package com.example.demo.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.entity.Proveedor;
import com.example.demo.repository.ProveedorRepository;

@Service
public class ProveedorService {
    @Autowired
    private ProveedorRepository proveedorRepository;

    public List<Proveedor> listarProveedores(){
        return proveedorRepository.findAll();
    }

    public Proveedor obtenerPorId(Long id){
        return proveedorRepository.findById(id).orElse(null);
    }
}
