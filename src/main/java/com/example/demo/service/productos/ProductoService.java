package com.example.demo.service.productos;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.entity.productos.Producto;
import com.example.demo.repository.productos.ProductoRepository;
import com.example.demo.repository.ventaa.ProduccionRepository;

@Service
public class ProductoService {
    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private ProduccionRepository produccionRepository;

    public ProductoService(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    public List<Producto> listarProductos() {
        return productoRepository.findAll();
    }

    public Producto guardar(Producto producto) {
        // Si categoría NO es ELABORADO → forzamos fraccionable = false
        if (producto.getCategoria() != null
                && !producto.getCategoria().getTipoControl().equalsIgnoreCase("ELABORADO")) {

            producto.setFraccionable(false); // 0 en BD
        }
        return productoRepository.save(producto);
    }

    public Producto obtenerPorId(Long id) {
        return productoRepository.findById(id).orElse(null);
    }

    public void eliminar(Long id) {
        productoRepository.deleteById(id);
    }

    public void actualizarStockActual(Producto producto) {
        // Suma de saldoActual de todas las producciones del producto
        Double stockTotal = produccionRepository.sumSaldoActualByProducto(producto.getId());
        if (stockTotal == null)
            stockTotal = 0.0;
        producto.setStockActual(stockTotal);
        productoRepository.save(producto); // Guarda el cambio
    }
}
