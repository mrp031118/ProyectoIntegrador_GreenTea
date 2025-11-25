package com.example.demo.service.lotes;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.example.demo.entity.insumos.Insumo;
import com.example.demo.entity.lotes.Lote;
import com.example.demo.entity.movimientos.Movimiento;
import com.example.demo.entity.movimientos.TipoMovimientoKardex;
import com.example.demo.entity.proveedores.Proveedor;
import com.example.demo.entity.usuarios.User;
import com.example.demo.repository.lotes.LoteRepository;
import com.example.demo.repository.movimientos.MovimientoRepository;
import com.example.demo.service.movimientos.TipoMovimientoService;
import com.example.demo.service.usuarios.CustomUserDetails;

@Service
public class LoteService {
    @Autowired
    private LoteRepository loteRepository;

    @Autowired
    private MovimientoRepository movimientoRepository;

    @Autowired
    private TipoMovimientoService tipoMovimientoService;

    // Buscar lotes con filtros
    public List<Lote> buscarLotes(Proveedor proveedor, Insumo insumo, String nombreInsumo) {
        if (proveedor != null && insumo != null) {
            return loteRepository.findByProveedorAndInsumo(proveedor, insumo);
        } else if (proveedor != null) {
            return loteRepository.findByProveedor(proveedor);
        } else if (insumo != null) {
            return loteRepository.findByInsumo(insumo);
        } else if (nombreInsumo != null && !nombreInsumo.isEmpty()) {
            return loteRepository.findByInsumo_NombreContainingIgnoreCase(nombreInsumo);
        } else {
            return loteRepository.findAll();
        }
    }

    public Lote guardarLote(Lote lote) {

        if (lote == null) {
            throw new IllegalArgumentException("El lote no puede ser nulo");
        }
        // guardar nuevo lote
        Lote nuevoLote = loteRepository.save(lote);

        // Buscar el tipo de movimiento "ENTRADA"
        TipoMovimientoKardex tipoEntrada = tipoMovimientoService.listarTipos()
                .stream()
                .filter(t -> t.getNombre().equalsIgnoreCase("ENTRADA"))
                .findFirst()
                .orElse(null);

        if (tipoEntrada == null) {
            throw new RuntimeException("Tipo de movimiento 'ENTRADA' no encontrado en la base de datos.");
        }
        // crear el nuevo movimiento
        Movimiento movimiento = new Movimiento();
        movimiento.setInsumo(nuevoLote.getInsumo());
        movimiento.setLote(nuevoLote);
        movimiento.setTipoMovimiento(tipoEntrada);
        movimiento.setFecha(LocalDateTime.now());
        movimiento.setCantidad(nuevoLote.getCantidad());
        movimiento.setCostoUnitario(nuevoLote.getCostoUnitario());
        movimiento.setTotal(nuevoLote.getCantidad() * nuevoLote.getCostoUnitario());
        movimiento.setObservaciones("Registro de nuevo lote de insumo");

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails cud = (CustomUserDetails) authentication.getPrincipal();
        User usuarioActual = cud.getUser();

        movimiento.setUsuario(usuarioActual);

        // Guardar el movimiento
        movimientoRepository.save(movimiento);

        return nuevoLote;
    }

    public List<Lote> listarLotes() {
        return loteRepository.findAll();
    }

    public Lote obtenerPorId(Long id) throws Exception {
        if (id == null) {
            throw new IllegalArgumentException("El ID no puede ser nulo");
        }

        return loteRepository.findById(id)
                .orElseThrow(() -> new Exception("El lote con ID " + id + " no existe"));
    }

    public void eliminarLote(Long id) throws Exception {
        if (id == null) {
            throw new IllegalArgumentException("El ID no puede ser nulo");
        }

        Lote lote = loteRepository.findById(id)
                .orElseThrow(() -> new Exception("El lote con ID " + id + " no existe"));

        if (lote.getMovimientos() != null && !lote.getMovimientos().isEmpty()) {
            throw new Exception("No se puede eliminar: el lote tiene movimientos registrados.");
        }

        loteRepository.delete(lote);
    }

}
