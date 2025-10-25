package com.example.demo.service.insumos;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.entity.Insumo;
import com.example.demo.entity.UnidadMedida;
import com.example.demo.repository.insumos.InsumoRepository;

@Service
public class InsumoService {
    @Autowired
    private InsumoRepository insumoRepository;

    // Buscar insumos por filtros: nombre y unidad de medida
    public List<Insumo> buscarInsumos(String nombre, UnidadMedida unidadMedida) {
        if (nombre == null) {
            nombre = "";
        }

        if (unidadMedida != null) {
            return insumoRepository.findByUnidadMedidaAndNombreContainingIgnoreCase(unidadMedida, nombre);
        } else {
            return insumoRepository.findByNombreContainingIgnoreCase(nombre);
        }
    }

    // Guardar o actualizar insumo
    public Insumo guardarInsumo(Insumo insumo) {
        return insumoRepository.save(insumo);
    }

    // buscar insumo por id
    public Insumo obtenerInsumosPorId(Long id) {
        return insumoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Insumo no encontrado"));
    }

    public void eliminarInsumo(Long id) throws Exception {
        Insumo insumo = obtenerInsumosPorId(id);

        if (insumo == null) {
            throw new Exception("El insumo no existe");
        }

        // Verificar si tiene movimientos asociados
        if (insumo.getMovimientos() != null && !insumo.getMovimientos().isEmpty()) {
            throw new Exception("No se puede eliminar: el insumo tiene movimientos registrados.");
        }

        insumoRepository.delete(insumo);
    }

    // Método para listar todos los insumos sin filtros
    public List<Insumo> listarInsumos() {
        return insumoRepository.findAll();
    }
}
