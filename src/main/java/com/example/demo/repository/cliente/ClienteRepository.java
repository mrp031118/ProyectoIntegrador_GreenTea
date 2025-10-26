package com.example.demo.repository.cliente;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.cliente.Cliente;

public interface ClienteRepository extends JpaRepository<Cliente, Integer>{

}
