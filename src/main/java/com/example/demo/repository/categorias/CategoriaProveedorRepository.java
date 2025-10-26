package com.example.demo.repository.categorias;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.categorias.CategoriaProveedor;

public interface CategoriaProveedorRepository extends JpaRepository<CategoriaProveedor, Long>{
    
}
