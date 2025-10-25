package com.example.demo.service.movimientos;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.entity.Movimiento;
import com.example.demo.repository.movimientos.MovimientoRepository;

@Service
public class MovimientoService {

    @Autowired
    private MovimientoRepository movimientoRepository;

    public Movimiento registrarMovimiento(Movimiento movimiento) {
        return movimientoRepository.save(movimiento);
    }

    public void eliminarMovimiento(Long id) {
        movimientoRepository.deleteById(id);
    }

    public List<Movimiento> listarMovimientos() {
        return movimientoRepository.findAll();
    }

    // 🧮 Método para obtener movimientos con lógica PEPS (FIFO)
    public List<Movimiento> listarMovimientosPEPS() {
        List<Movimiento> movimientos = movimientoRepository.findAll();
        movimientos.sort(Comparator.comparing(Movimiento::getFecha));

        // Mapa para manejar los lotes PEPS por insumo
        Map<Long, List<LotePEPS>> lotesPorInsumo = new HashMap<>();
        List<Movimiento> resultado = new ArrayList<>();

        for (Movimiento mov : movimientos) {
            Long insumoId = mov.getInsumo().getInsumoId();
            String tipo = mov.getTipoMovimiento().getNombre().trim().toUpperCase();
            // 🧩 Depuración: muestra en consola qué tipo se está leyendo
            System.out.println("Tipo detectado: " + mov.getTipoMovimiento().getNombre());

            // Obtener o crear lista de lotes para este insumo
            List<LotePEPS> lotes = lotesPorInsumo.computeIfAbsent(insumoId, k -> new ArrayList<>());

            if (tipo.equals("ENTRADA")) {
                lotes.add(new LotePEPS(mov.getCantidad(), mov.getCostoUnitario()));

            } else if (tipo.equals("SALIDA") || tipo.equals("MERMAS") || tipo.equals("USO")) {
                double cantidadRestante = mov.getCantidad();

                while (cantidadRestante > 0 && !lotes.isEmpty()) {
                    LotePEPS lote = lotes.get(0);

                    if (lote.cantidad > cantidadRestante) {
                        lote.cantidad -= cantidadRestante;
                        cantidadRestante = 0;
                    } else {
                        cantidadRestante -= lote.cantidad;
                        lotes.remove(0);
                    }
                }
            }

            // Calcular el saldo actual SOLO para este insumo
            double saldoCantidad = 0;
            double saldoValor = 0;
            for (LotePEPS lote : lotes) {
                saldoCantidad += lote.cantidad;
                saldoValor += lote.cantidad * lote.costoUnitario;
            }

            // Crear movimiento con el saldo actualizado
            Movimiento movConSaldo = new Movimiento();
            movConSaldo.setMovimientoId(mov.getMovimientoId());
            movConSaldo.setInsumo(mov.getInsumo());
            movConSaldo.setTipoMovimiento(mov.getTipoMovimiento());
            movConSaldo.setFecha(mov.getFecha());
            movConSaldo.setCantidad(mov.getCantidad());
            movConSaldo.setCostoUnitario(mov.getCostoUnitario());
            // Asignar el saldo total calculado al campo 'total'
            movConSaldo.setTotal(saldoValor); // Campo 'Valor Total' del Saldo
            movConSaldo.setLote(mov.getLote());
            movConSaldo.setObservaciones(mov.getObservaciones());

            // AÑADE ESTA LÍNEA CLAVE:
            // Asignar la cantidad de saldo calculada al campo Transient 'saldoCantidad'
            movConSaldo.setSaldoCantidad(saldoCantidad); // Campo 'Cantidad' del Saldo

            resultado.add(movConSaldo);
        }

        return resultado;
    }

    // Clase auxiliar para manejar los lotes FIFO por insumo
    private static class LotePEPS {
        double cantidad;
        double costoUnitario;

        LotePEPS(double cantidad, double costoUnitario) {
            this.cantidad = cantidad;
            this.costoUnitario = costoUnitario;
        }
    }

}
