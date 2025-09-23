package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.Proveedor;

public interface ProveedorRepository extends JpaRepository<Proveedor, Long>{
    List<Proveedor> findByCategoriaNombre(String nombre);

}
