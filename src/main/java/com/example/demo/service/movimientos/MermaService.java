package com.example.demo.service.movimientos;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.dto.MermaDTO;
import com.example.demo.entity.movimientos.MovimientoProducto;
import com.example.demo.entity.produccion.MermaProducto;
import com.example.demo.entity.usuarios.User;
import com.example.demo.repository.movimientos.MovimientoProductoRepository;

@Service
public class MermaService {
    // En ProduccionService.java (o crea MermaService.java)
    @Autowired
    private MovimientoProductoRepository movimientoProductoRepository; // Ya tienes
    // En MermaService.java
    @Autowired
    private MermaProductoService mermaProductoService; // Agrega

    public List<MermaDTO> listarMermasPorEmpleado(User empleado) {
        List<MermaDTO> mermas = new ArrayList<>();

        // Mermas elaboradas (igual)
        List<MovimientoProducto> mermasElaboradas = movimientoProductoRepository
                .findByEmpleadoAndTipoMovimientoId_Id(empleado, 4);
        for (MovimientoProducto mp : mermasElaboradas) {
            MermaDTO dto = new MermaDTO();
            dto.setProductoNombre(mp.getProducto().getNombre());
            dto.setCantidad(mp.getCantidad());
            dto.setFecha(mp.getFecha());
            dto.setMotivo(mp.getObservaciones());
            dto.setEmpleadoNombre(empleado.getNombre());
            mermas.add(dto);
        }

        // Mermas instantáneas (de MermaProducto, no de MermaProductoInsumo)
        List<MermaProducto> mermasInstantaneas = mermaProductoService.listarPorEmpleado(empleado);
        for (MermaProducto mp : mermasInstantaneas) {
            MermaDTO dto = new MermaDTO();
            dto.setProductoNombre(mp.getProducto().getNombre());
            dto.setCantidad(mp.getCantidad()); // Cantidad total mermada del producto
            dto.setFecha(mp.getFecha());
            dto.setMotivo(mp.getMotivo());
            dto.setEmpleadoNombre(empleado.getNombre());
            mermas.add(dto);
        }

        // Ordena
        mermas.sort(Comparator.comparing(MermaDTO::getFecha).reversed());
        return mermas;
    }
}
