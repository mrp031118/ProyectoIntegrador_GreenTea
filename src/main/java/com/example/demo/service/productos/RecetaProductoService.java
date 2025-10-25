package com.example.demo.service.productos;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.entity.RecetaProducto;
import com.example.demo.repository.productos.RecetaProductoRepository;

@Service
public class RecetaProductoService {

    @Autowired
    private RecetaProductoRepository recetaProductoRepository;

    public List<RecetaProducto> listar() {
        return recetaProductoRepository.findAll();
    }

    public RecetaProducto guardar(RecetaProducto receta) {
        // IMPORTANTE: asegurar relación bidireccional: setReceta en cada detalle
        if (receta.getDetalles() != null) {
            receta.getDetalles().forEach(d -> d.setRecetaProducto(receta));
        }
        if (receta.getFechaCreacion() == null)
            receta.setFechaCreacion(LocalDate.now());
        return recetaProductoRepository.save(receta);
    }

    public RecetaProducto buscarPorId(Long id) {
        return recetaProductoRepository.findById(id).orElse(null);
    }

    public void eliminar(Long id) {
        recetaProductoRepository.deleteById(id);
    }

}
