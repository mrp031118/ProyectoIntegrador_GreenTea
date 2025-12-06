package com.example.demo.service.movimientos;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.example.demo.dto.KardexLoteProjection;
import com.example.demo.entity.insumos.Insumo;
import com.example.demo.entity.insumos.UnidadMedida;
import com.example.demo.entity.lotes.Lote;
import com.example.demo.entity.movimientos.Movimiento;
import com.example.demo.entity.movimientos.TipoMovimientoKardex;
import com.example.demo.entity.produccion.Produccion;
import com.example.demo.entity.productos.DetalleRecetaProducto;
import com.example.demo.entity.productos.Producto;
import com.example.demo.entity.productos.RecetaProducto;
import com.example.demo.entity.usuarios.User;
import com.example.demo.repository.lotes.LoteRepository;
import com.example.demo.repository.movimientos.MovimientoRepository;
import com.example.demo.repository.movimientos.TipoMovimientoRepository;
import com.example.demo.repository.productos.RecetaProductoRepository;
import com.example.demo.service.insumos.ConversionUnidadService;
import com.example.demo.service.usuarios.CustomUserDetails;

import jakarta.transaction.Transactional;

@Service
public class MovimientoService {
    @Autowired
    private TipoMovimientoRepository tipoMovimientoRepository;

    @Autowired
    private MovimientoRepository movimientoRepository;

    @Autowired
    private LoteRepository loteRepository;

    @Autowired
    private ConversionUnidadService conversionUnidadService;

    @Autowired
    private RecetaProductoRepository recetaProductoRepository;

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

    // 🔹 Registra una salida automática cuando el producto pertenece a una
    // categoría de CONTROL_INSTANTANEO
    public void registrarSalidaAutomatica(Object producto, double cantidad, String observacion) {
        try {
            Producto prod = new Producto();

            // Verificamos tipo de control de la categoría del producto
            String tipoControl = prod.getCategoria().getTipoControl();
            if (!"INSTANTANEO".equalsIgnoreCase(tipoControl)) {
                return; // si no es instantáneo, no genera salida
            }

            Movimiento movimiento = new Movimiento();
            movimiento.setCantidad(cantidad);
            movimiento.setFecha(LocalDateTime.now());
            movimiento.setObservaciones(observacion);

            // ⚙️ Tipo de movimiento: salida
            TipoMovimientoKardex tipoSalida = new TipoMovimientoKardex();
            tipoSalida.setId(2); // asegúrate que 2 sea SALIDA
            tipoSalida.setNombre("SALIDA");

            movimiento.setTipoMovimiento(tipoSalida);

            movimientoRepository.save(movimiento);
            System.out.println("✅ Movimiento de salida registrado por CONTROL_INSTANTANEO");
        } catch (Exception e) {
            System.err.println("❌ Error al registrar salida automática: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void descontarInsumosPorProduccion(Producto producto, double cantidadElaborada, Produccion produccion) {
        // Obtener la receta del producto
        RecetaProducto receta = recetaProductoRepository.findByProducto_Id(producto.getId()).orElse(null);
        if (receta == null)
            return;

        for (DetalleRecetaProducto drp : receta.getDetalles()) {

            // Cantidad total requerida por insumo
            BigDecimal cantidadRequerida = drp.getCantidad().multiply(BigDecimal.valueOf(cantidadElaborada));

            // Convertir unidades si es necesario
            UnidadMedida unidadReceta = drp.getUnidadMedida();
            UnidadMedida unidadStock = drp.getInsumo().getUnidadMedida();
            BigDecimal cantidadParaRestar = cantidadRequerida;

            if (!unidadReceta.getId().equals(unidadStock.getId())) {
                try {
                    cantidadParaRestar = conversionUnidadService.convertir(cantidadRequerida, unidadReceta,
                            unidadStock);
                } catch (Exception e) {
                    cantidadParaRestar = BigDecimal.ZERO;
                }
            }

            BigDecimal restante = cantidadParaRestar;

            // Obtener los lotes disponibles (PEPS)
            List<KardexLoteProjection> lotesDisponibles = loteRepository.findAllKardexLotePeps().stream()
                    .filter(k -> k.getInsumoId().equals(drp.getInsumo().getInsumoId()) && k.getCantidadDisponible() > 0)
                    .sorted((a, b) -> a.getFechaEntrada().compareTo(b.getFechaEntrada()))
                    .toList();

            for (KardexLoteProjection loteKardex : lotesDisponibles) {
                if (restante.compareTo(BigDecimal.ZERO) <= 0)
                    break;

                BigDecimal cantidadDisponible = BigDecimal.valueOf(loteKardex.getCantidadDisponible());

                // ⚡ Si la unidad es "Unidad" (como huevos), redondear hacia abajo
                BigDecimal cantidadDesdeLote;
                if ("Unidad".equalsIgnoreCase(unidadStock.getNombre())) {
                    cantidadDesdeLote = cantidadDisponible.min(restante).setScale(0, RoundingMode.FLOOR);
                } else {
                    cantidadDesdeLote = cantidadDisponible.min(restante);
                }

                // Registrar movimiento de salida
                Movimiento mov = new Movimiento();
                mov.setInsumo(drp.getInsumo());
                mov.setCantidad(cantidadDesdeLote.doubleValue());
                mov.setCostoUnitario(loteKardex.getCostoUnitario());
                mov.setTotal(
                        cantidadDesdeLote.multiply(BigDecimal.valueOf(loteKardex.getCostoUnitario())).doubleValue());
                mov.setFecha(LocalDateTime.now());

                TipoMovimientoKardex tipoSalida = new TipoMovimientoKardex();
                tipoSalida.setId(2); // SALIDA
                tipoSalida.setNombre("SALIDA");
                mov.setTipoMovimiento(tipoSalida);

                // Asociar lote real (sin modificar cantidad)
                Lote loteReal = loteRepository.findById(loteKardex.getLoteId()).orElseThrow();
                mov.setLote(loteReal);
                mov.setObservaciones("Producción: " + producto.getNombre());

                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                CustomUserDetails cud = (CustomUserDetails) auth.getPrincipal();
                User usuarioActual = cud.getUser();
                mov.setUsuario(usuarioActual);
                movimientoRepository.save(mov);
                // Reducir el restante
                restante = restante.subtract(cantidadDesdeLote);
            }

            // Alertar si no hay suficiente stock
            if (restante.compareTo(BigDecimal.ZERO) > 0) {
                System.out.println("⚠️ Insumo " + drp.getInsumo().getNombre() +
                        " no tiene suficiente stock. Falta: " + restante + " " + unidadStock.getNombre());
            }
        }
    }

    // Calcula el costo total del lote según la receta del producto
    public double calcularCostoPorReceta(Producto producto, double factor) {

        // 1. Buscar la receta desde el repositorio
        RecetaProducto receta = recetaProductoRepository.findByProducto_Id(producto.getId())
                .orElse(null);

        if (receta == null) {
            System.out.println("⚠ El producto no tiene receta registrada");
            return 0.0;
        }

        double costoTotal = 0.0;

        // 2. Recorrer los insumos del detalle
        for (DetalleRecetaProducto det : receta.getDetalles()) {
            // Cantidad total requerida por insumo (aplicando el factor)
            BigDecimal cantidadRequerida = det.getCantidad().multiply(BigDecimal.valueOf(factor));

            // Convertir unidades si es necesario
            UnidadMedida unidadReceta = det.getUnidadMedida();
            UnidadMedida unidadStock = det.getInsumo().getUnidadMedida();
            BigDecimal cantidadParaRestar = cantidadRequerida;

            if (!unidadReceta.getId().equals(unidadStock.getId())) {
                try {
                    cantidadParaRestar = conversionUnidadService.convertir(cantidadRequerida, unidadReceta,
                            unidadStock);
                } catch (Exception e) {
                    cantidadParaRestar = BigDecimal.ZERO;
                }
            }
            BigDecimal restante = cantidadParaRestar;

            // 3. Obtener los lotes disponibles (PEPS) y calcular el costo basado en ellos
            List<KardexLoteProjection> lotesDisponibles = loteRepository.findAllKardexLotePeps().stream()
                    .filter(k -> k.getInsumoId().equals(det.getInsumo().getInsumoId()) && k.getCantidadDisponible() > 0)
                    .sorted((a, b) -> a.getFechaEntrada().compareTo(b.getFechaEntrada())) // PEPS: ordenar por fecha
                                                                                          // ascendente
                    .toList();

            // Simular el descuento PEPS y acumular el costo
            for (KardexLoteProjection loteKardex : lotesDisponibles) {
                if (restante.compareTo(BigDecimal.ZERO) <= 0)
                    break;
                BigDecimal cantidadDisponible = BigDecimal.valueOf(loteKardex.getCantidadDisponible());
                // Si la unidad es "Unidad" (ej. huevos), redondear hacia abajo
                BigDecimal cantidadDesdeLote;
                if ("Unidad".equalsIgnoreCase(unidadStock.getNombre())) {
                    cantidadDesdeLote = cantidadDisponible.min(restante).setScale(0, RoundingMode.FLOOR);
                } else {
                    cantidadDesdeLote = cantidadDisponible.min(restante);
                }
                // Acumular el costo: cantidad tomada * costo unitario del lote
                costoTotal += cantidadDesdeLote.doubleValue() * loteKardex.getCostoUnitario();
                // Reducir el restante
                restante = restante.subtract(cantidadDesdeLote);
            }

            // Si no hay suficiente stock, el costo se calcula solo con lo disponible
            // (puedes agregar una alerta si quieres)
            if (restante.compareTo(BigDecimal.ZERO) > 0) {
                System.out.println("⚠️ Insumo " + det.getInsumo().getNombre() +
                        " no tiene suficiente stock para el cálculo. Falta: " + restante + " "
                        + unidadStock.getNombre());
            }

        }

        return costoTotal;
    }

    @Transactional
    public void descontarInsumosPorRecetaMerma(Producto producto, double cantidadMerma, String motivo) {

        RecetaProducto receta = recetaProductoRepository
                .findByProducto_Id(producto.getId())
                .orElseThrow(() -> new RuntimeException("Receta no encontrada"));

        for (DetalleRecetaProducto drp : receta.getDetalles()) {

            BigDecimal cantidadRequerida = drp.getCantidad()
                    .multiply(BigDecimal.valueOf(cantidadMerma));

            UnidadMedida unidadReceta = drp.getUnidadMedida();
            UnidadMedida unidadStock = drp.getInsumo().getUnidadMedida();
            BigDecimal cantidadConvertida = cantidadRequerida;

            // 🔥 CONVERSIÓN FALTANTE (g → kg, ml → L)
            if (!unidadReceta.getId().equals(unidadStock.getId())) {
                cantidadConvertida = conversionUnidadService.convertir(
                        cantidadRequerida,
                        unidadReceta,
                        unidadStock);
            }

            System.out.println(" - Insumo: " + drp.getInsumo().getNombre() +
                    " requiere: " + cantidadRequerida + " " + unidadReceta.getNombre() +
                    " convertido a: " + cantidadConvertida + " " + unidadStock.getNombre());

            descontarInsumoPeps(
                    drp.getInsumo(),
                    cantidadConvertida,
                    4, // merma
                    "Merma instantáneo: " + producto.getNombre() + " - " + motivo, producto);
        }
    }

    @Transactional
    public void descontarInsumoPeps(Insumo insumo, BigDecimal cantidadSolicitada,
            int tipoMovimientoId, String observacion, Producto producto) {

        System.out.println(
                " => descontarInsumoPeps insumo=" + insumo.getNombre() + " cantidadSolicitada=" + cantidadSolicitada);

        List<KardexLoteProjection> lotes = loteRepository.findAllKardexLotePeps().stream()
                .filter(k -> k.getInsumoId().equals(insumo.getInsumoId()) && k.getCantidadDisponible() > 0)
                .sorted((a, b) -> a.getFechaEntrada().compareTo(b.getFechaEntrada()))
                .toList();

        BigDecimal restante = cantidadSolicitada;

        // Validar tipo de movimiento existe
        TipoMovimientoKardex tipoMov = tipoMovimientoRepository.findById(tipoMovimientoId)
                .orElseThrow(() -> new RuntimeException("Tipo movimiento id=" + tipoMovimientoId + " no encontrado"));

        for (KardexLoteProjection loteKardex : lotes) {

            if (restante.compareTo(BigDecimal.ZERO) <= 0)
                break;

            BigDecimal disponible = BigDecimal.valueOf(loteKardex.getCantidadDisponible());
            BigDecimal tomar = disponible.min(restante);

            // Registrar movimiento
            Movimiento mov = new Movimiento();
            mov.setInsumo(insumo);
            mov.setCantidad(tomar.doubleValue());
            mov.setCostoUnitario(loteKardex.getCostoUnitario());
            mov.setTotal(tomar.multiply(BigDecimal.valueOf(loteKardex.getCostoUnitario())).doubleValue());
            mov.setFecha(LocalDateTime.now());
            mov.setTipoMovimiento(tipoMov);
            mov.setLote(loteRepository.findById(loteKardex.getLoteId()).orElseThrow());
            mov.setObservaciones(observacion);

            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            CustomUserDetails cud = (CustomUserDetails) auth.getPrincipal();
            User usuarioActual = cud.getUser();
            mov.setUsuario(usuarioActual);

            movimientoRepository.save(mov);

            System.out.println(" > Guardado movimiento insumo: " + mov.getCantidad() + " " + insumo.getNombre()
                    + " loteId=" + loteKardex.getLoteId());

            restante = restante.subtract(tomar);
        }

        if (restante.compareTo(BigDecimal.ZERO) > 0) {
            System.out.println(
                    "⚠️ No fue posible descontar totalmente el insumo " + insumo.getNombre() + ", falta=" + restante);
        }
    }

    public double obtenerStockActualInsumo(Long insumoId) {

        List<Movimiento> movimientos = movimientoRepository.findByInsumo_InsumoId(insumoId);

        double stock = 0;

        for (Movimiento mov : movimientos) {
            if (mov.getTipoMovimiento().getNombre().equalsIgnoreCase("ENTRADA")) {
                stock += mov.getCantidad();
            } else {
                stock -= mov.getCantidad();
            }
        }

        return stock < 0 ? 0 : stock;
    }

    public double obtenerStockDisponiblePorReceta(Producto producto) {

        RecetaProducto receta = recetaProductoRepository.findByProducto(producto)
                .orElse(null);

        if (receta == null)
            return 0;

        double minimo = Double.MAX_VALUE;

        for (DetalleRecetaProducto det : receta.getDetalles()) {

            Long insumoId = det.getInsumo().getInsumoId();

            // Obtener LOTES REALES PEPS
            List<KardexLoteProjection> lotes = loteRepository.findAllKardexLotePeps().stream()
                    .filter(k -> k.getInsumoId().equals(insumoId) && k.getCantidadDisponible() > 0)
                    .sorted((a, b) -> a.getFechaEntrada().compareTo(b.getFechaEntrada()))
                    .toList();

            if (lotes.isEmpty())
                return 0;

            // Cantidad necesaria por unidad del producto (convertida)
            BigDecimal cantidadReceta = det.getCantidad();
            UnidadMedida unidadReceta = det.getUnidadMedida();
            UnidadMedida unidadStock = det.getInsumo().getUnidadMedida();

            BigDecimal cantidadConvertida = cantidadReceta;
            if (!unidadReceta.getId().equals(unidadStock.getId())) {
                cantidadConvertida = conversionUnidadService.convertir(cantidadReceta, unidadReceta, unidadStock);
            }

            double totalDisponible = 0;

            for (KardexLoteProjection lote : lotes) {
                totalDisponible += lote.getCantidadDisponible();
            }

            double unidades = totalDisponible / cantidadConvertida.doubleValue();

            minimo = Math.min(minimo, unidades);
        }

        return minimo == Double.MAX_VALUE ? 0 : minimo;
    }

}
