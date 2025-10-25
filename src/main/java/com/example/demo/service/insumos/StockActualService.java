package com.example.demo.service.insumos;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.dto.StockActualProjection;
import com.example.demo.repository.insumos.StockActualRepository;

@Service
public class StockActualService {

    @Autowired
    private StockActualRepository stockActualRepository;

    public StockActualService(StockActualRepository stockActualRepository) {
        this.stockActualRepository = stockActualRepository;
    }

    public List<StockActualProjection> obtenerStockActual() {
        return stockActualRepository.findAllStockActual();
    }
}
