package com.example.demo.service;


import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.dto.KardexLoteProjection;
import com.example.demo.entity.Lote;
import com.example.demo.repository.LoteRepository;

import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
public class KardexLoteService {

    @Autowired
    private LoteRepository loteRepository;

    public void guardarLote(Lote lote) {
        loteRepository.save(lote);
    }

    public List<KardexLoteProjection> obtenerKardexLotes() {
        return loteRepository.findAllKardexLotePeps();
    }
    
}
