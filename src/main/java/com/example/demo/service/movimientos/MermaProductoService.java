package com.example.demo.service.movimientos;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.entity.produccion.MermaProducto;
import com.example.demo.entity.productos.Producto;
import com.example.demo.entity.usuarios.User;
import com.example.demo.repository.movimientos.MermaProductoRepository;

@Service
public class MermaProductoService {
    @Autowired
    private MermaProductoRepository mermaProductoRepository;

    public void registrarMermaProducto(Producto producto, double cantidad, String motivo, User empleado) {
        MermaProducto merma = new MermaProducto();
        merma.setProducto(producto);
        merma.setCantidad(cantidad); // Cantidad total mermada
        merma.setFecha(LocalDateTime.now());
        merma.setMotivo(motivo);
        merma.setEmpleado(empleado);
        mermaProductoRepository.save(merma);
    }

    public List<MermaProducto> listarPorEmpleado(User empleado) {
        return mermaProductoRepository.findByEmpleado(empleado);
    }
}
