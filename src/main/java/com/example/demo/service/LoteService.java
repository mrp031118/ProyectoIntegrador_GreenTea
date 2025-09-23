package com.example.demo.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.entity.Lote;
import com.example.demo.repository.LoteRepository;

@Service
public class LoteService {
    @Autowired
    private LoteRepository loteRepository;

    public Lote guardarLote(Lote lote) {
        // Al crear lote, cantidad disponible inicial = cantidad total
        if (lote.getCantidadDisponible() == null) {
            lote.setCantidadDisponible(lote.getCantidad());
        }
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
