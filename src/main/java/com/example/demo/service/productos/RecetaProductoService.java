package com.example.demo.service.productos;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.entity.productos.DetalleRecetaProducto;
import com.example.demo.entity.productos.RecetaProducto;
import com.example.demo.repository.productos.RecetaProductoRepository;

import jakarta.transaction.Transactional;

@Service
public class RecetaProductoService {

    @Autowired
    private RecetaProductoRepository recetaProductoRepository;

    public List<RecetaProducto> listar() {
        return recetaProductoRepository.findAll();
    }

    public RecetaProducto guardar(RecetaProducto receta) {
        // ✅ Eliminar filas vacías antes de guardar
        if (receta.getDetalles() != null) {
            receta.getDetalles().removeIf(d -> d.getInsumo() == null ||
                    d.getCantidad() == null ||
                    d.getUnidadMedida() == null);
            receta.getDetalles().forEach(d -> d.setRecetaProducto(receta));
        }

        // ✅ Si no tiene fecha de creación, asignar
        if (receta.getFechaCreacion() == null) {
            receta.setFechaCreacion(LocalDateTime.now());
        }

        return recetaProductoRepository.save(receta);
    }

    @Transactional
    public RecetaProducto actualizar(Long id, RecetaProducto recetaActualizada, List<Long> idsAEliminar) {
        RecetaProducto existente = recetaProductoRepository.findById(id).orElse(null);
        if (existente == null) {
            return null;
        }

        // 🟢 Actualiza datos principales
        existente.setNombreReceta(recetaActualizada.getNombreReceta());
        existente.setProducto(recetaActualizada.getProducto());

        // 🟠 Eliminar detalles que el usuario quitó del formulario
        if (idsAEliminar != null && !idsAEliminar.isEmpty()) {
            existente.getDetalles().removeIf(d -> idsAEliminar.contains(d.getId()));
        }

        // 🟣 IDs actuales (para saber cuáles actualizar)
        List<Long> idsNuevos = recetaActualizada.getDetalles().stream()
                .map(DetalleRecetaProducto::getId)
                .collect(Collectors.toList());

        // 🔄 Actualizar o agregar nuevos detalles
        for (DetalleRecetaProducto nuevo : recetaActualizada.getDetalles()) {
            nuevo.setRecetaProducto(existente);

            if (nuevo.getId() == null) {
                // nuevo detalle
                existente.getDetalles().add(nuevo);
            } else {
                // actualizar existente
                existente.getDetalles().stream()
                        .filter(d -> d.getId().equals(nuevo.getId()))
                        .findFirst()
                        .ifPresent(detalle -> {
                            detalle.setInsumo(nuevo.getInsumo());
                            detalle.setCantidad(nuevo.getCantidad());
                            detalle.setUnidadMedida(nuevo.getUnidadMedida());
                        });
            }
        }

        return recetaProductoRepository.save(existente);
    }

    public RecetaProducto buscarPorId(Long id) {
        return recetaProductoRepository.findById(id).orElse(null);
    }

    public void eliminar(Long id) {
        recetaProductoRepository.deleteById(id);
    }

    public List<RecetaProducto> buscarPorNombreProducto(String nombre) {
        return recetaProductoRepository.buscarPorNombreProducto(nombre);
    }

}
