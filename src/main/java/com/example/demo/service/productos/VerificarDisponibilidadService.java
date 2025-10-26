package com.example.demo.service.productos;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.dto.StockActualProjection;
import com.example.demo.entity.insumos.UnidadMedida;
import com.example.demo.entity.productos.DetalleRecetaProducto;
import com.example.demo.entity.productos.Producto;
import com.example.demo.entity.productos.RecetaProducto;
import com.example.demo.repository.productos.ProductoRepository;
import com.example.demo.repository.productos.RecetaProductoRepository;
import com.example.demo.service.insumos.ConversionUnidadService;
import com.example.demo.service.insumos.StockActualService;

@Service
public class VerificarDisponibilidadService {

    @Autowired
    private RecetaProductoRepository recetaProductoRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private StockActualService stockActualService;

    @Autowired
    private ConversionUnidadService conversionUnidadService;

    // DTO para mostrar resultado por insumo
    public static class ResultadoDisponibilidad {
        public String insumo;
        public BigDecimal requerido;
        public BigDecimal disponible;
        public String unidad;
        public boolean disponibleFlag;
    }

    public List<ResultadoDisponibilidad> verificarDisponibilidad(Long productoId, Double cantidadSolicitada) {

        RecetaProducto receta = recetaProductoRepository.findByProducto_Id(productoId)
                .orElse(null);

        if (receta == null) {
            return null; 
        }

        List<StockActualProjection> stockActualList = stockActualService.obtenerStockActual();

        List<ResultadoDisponibilidad> resultados = new ArrayList<>();

        for (DetalleRecetaProducto detalle : receta.getDetalles()) {
            Long insumoId = detalle.getInsumo().getInsumoId();
            UnidadMedida unidadReceta = detalle.getUnidadMedida(); // unidad usada en receta (g, ml)
            BigDecimal requerido = detalle.getCantidad().multiply(BigDecimal.valueOf(cantidadSolicitada));

            StockActualProjection stock = stockActualList.stream()
                    .filter(s -> s.getInsumoId().equals(insumoId))
                    .findFirst()
                    .orElse(null);

            ResultadoDisponibilidad r = new ResultadoDisponibilidad();
            r.insumo = detalle.getInsumo().getNombre();
            r.requerido = requerido;
            r.unidad = unidadReceta.getNombre();

            if (stock != null) {
                BigDecimal disponible = BigDecimal.valueOf(stock.getStockActual());
                UnidadMedida unidadStock = detalle.getInsumo().getUnidadMedida(); // unidad real en inventario

                // 🔹 Si las unidades difieren, convierte disponible a la unidad de la receta
                if (!unidadStock.getId().equals(unidadReceta.getId())) {
                    try {
                        disponible = conversionUnidadService.convertir(disponible, unidadStock, unidadReceta);
                    } catch (Exception e) {
                        // Si no hay conversión posible, marcar como no disponible
                        disponible = BigDecimal.ZERO;
                    }
                }

                r.disponible = disponible;
                r.disponibleFlag = disponible.compareTo(requerido) >= 0;

            } else {
                r.disponible = BigDecimal.ZERO;
                r.disponibleFlag = false;
            }

            resultados.add(r);
        }

        return resultados;
    }

    public List<Producto> obtenerTodosLosProductos() {
        return productoRepository.findAll();
    }

}
