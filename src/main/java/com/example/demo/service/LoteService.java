package com.example.demo.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.entity.Insumo;
import com.example.demo.entity.Lote;
import com.example.demo.entity.Proveedor;
import com.example.demo.repository.LoteRepository;

@Service
public class LoteService {
    @Autowired
    private LoteRepository loteRepository;

    // Buscar lotes con filtros
    public List<Lote> buscarLotes(Proveedor proveedor, Insumo insumo, String nombreInsumo) {
        if (proveedor != null && insumo != null) {
            return loteRepository.findByProveedorAndInsumo(proveedor, insumo);
        } else if (proveedor != null) {
            return loteRepository.findByProveedor(proveedor);
        } else if (insumo != null) {
            return loteRepository.findByInsumo(insumo);
        } else if (nombreInsumo != null && !nombreInsumo.isEmpty()) {
            return loteRepository.findByInsumo_NombreContainingIgnoreCase(nombreInsumo);
        } else {
            return loteRepository.findAll();
        }
    }

    public Lote guardarLote(Lote lote) {
        return loteRepository.save(lote);
    }

    public List<Lote> listarLotes() {
        return loteRepository.findAll();
    }

    public Lote obtenerPorId(Long id) {
        return loteRepository.findById(id).orElse(null);
    }

    public void eliminarLote(Long id) throws Exception {
        Lote lote = loteRepository.findById(id)

                .orElseThrow(() -> new Exception("El lote con ID " + id + " no existe"));

        // Verificar si tiene movimientos asociados
        if (lote.getMovimientos() != null && !lote.getMovimientos().isEmpty()) {
            throw new Exception("No se puede eliminar: el lote tiene movimientos registrados.");
        }

        loteRepository.delete(lote);
    }

}
