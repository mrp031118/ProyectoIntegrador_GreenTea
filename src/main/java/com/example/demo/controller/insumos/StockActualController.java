package com.example.demo.controller.insumos;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.StockActualProjection;
import com.example.demo.service.insumos.StockActualService;

@RestController
@RequestMapping("/admin/api/stock")
public class StockActualController {
    
    @Autowired
    private StockActualService stockActualService;

    @GetMapping("/actual")
    public List<StockActualProjection> getStockActual() {
        return stockActualService.obtenerStockActual();
    }
}
