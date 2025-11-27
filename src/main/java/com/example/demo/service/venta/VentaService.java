package com.example.demo.service.venta;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.example.demo.dto.KardexLoteProjection;
import com.example.demo.entity.cliente.Cliente;
import com.example.demo.entity.insumos.UnidadMedida;
import com.example.demo.entity.lotes.Lote;
import com.example.demo.entity.movimientos.Movimiento;
import com.example.demo.entity.movimientos.TipoMovimientoKardex;
import com.example.demo.entity.productos.DetalleRecetaProducto;
import com.example.demo.entity.productos.Producto;
import com.example.demo.entity.productos.RecetaProducto;
import com.example.demo.entity.usuarios.User;
import com.example.demo.entity.venta.DetalleVenta;
import com.example.demo.entity.venta.MetodoPago;
import com.example.demo.entity.venta.Venta;
import com.example.demo.repository.cliente.ClienteRepository;
import com.example.demo.repository.lotes.LoteRepository;
import com.example.demo.repository.movimientos.MovimientoRepository;
import com.example.demo.repository.productos.RecetaProductoRepository;
import com.example.demo.repository.ventaa.VentaRepository;
import com.example.demo.service.insumos.ConversionUnidadService;
import com.example.demo.service.movimientos.MovimientoProductoService;
import com.example.demo.service.usuarios.CustomUserDetails;
import com.itextpdf.io.source.ByteArrayOutputStream;
import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;

import jakarta.transaction.Transactional;

@Service
public class VentaService {

    @Autowired
    private VentaRepository ventaRepository;
    @Autowired
    private RecetaProductoRepository recetaProductoRepository;

    @Autowired
    private MovimientoRepository movimientoRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private LoteRepository loteRepository;
    @Autowired
    private ConversionUnidadService conversionUnidadService;

    @Autowired
    private MovimientoProductoService movimientoProductoService; // Para consultar stock si es necesario

    // Método para listar todas las ventas
    public List<Venta> listarTodasVentas() {
        return ventaRepository.findAll();
    }

    // DTO interno para pasar producto + cantidad
    public static class ProductoCantidad {
        public Producto producto;
        public Double cantidad;

        public ProductoCantidad(Producto producto, Double cantidad) {
            this.producto = producto;
            this.cantidad = cantidad;
        }
    }

    @Transactional
    public void registrarVenta(User empleado, Integer clienteId, String clienteNombre, MetodoPago metodoPago,
            List<ProductoCantidad> lista) throws Exception { // Agregué 'throws Exception' para manejar errores
        // 🔹 NUEVA VALIDACIÓN: Verificar que TODOS los productos tengan receta antes de
        // proceder
        for (ProductoCantidad pc : lista) {
            RecetaProducto receta = recetaProductoRepository.findByProducto_Id(pc.producto.getId()).orElse(null);
            if (receta == null) {
                throw new Exception("El producto '" + pc.producto.getNombre()
                        + "' no tiene una receta asociada. No se puede registrar la venta.");
            }
        }
        // Si todos tienen receta, proceder con la venta
        Venta venta = new Venta();
        venta.setEmpleado(empleado);
        Cliente clienteObj = null;
        if (clienteId != null) {
            clienteObj = clienteRepository.findById(clienteId).orElse(null);
        }
        venta.setCliente(clienteObj);
        venta.setMetodoPago(metodoPago);
        venta.setFecha(LocalDateTime.now());
        BigDecimal totalVenta = BigDecimal.ZERO;
        for (ProductoCantidad pc : lista) {
            // Obtener receta (ya validada arriba, pero por seguridad)
            RecetaProducto receta = recetaProductoRepository.findByProducto_Id(pc.producto.getId()).orElse(null);
            if (receta == null)
                continue; // No debería pasar, pero por si acaso
            // 1️⃣ Crear detalle de venta
            DetalleVenta detalle = new DetalleVenta();
            detalle.setProducto(pc.producto);
            detalle.setCantidad(pc.cantidad);
            detalle.setPrecioUnidad(BigDecimal.valueOf(pc.producto.getPrecio()));
            detalle.setVenta(venta);
            BigDecimal subtotal = BigDecimal.valueOf(pc.producto.getPrecio())
                    .multiply(BigDecimal.valueOf(pc.cantidad));
            detalle.setTotal(subtotal);
            venta.getDetalles().add(detalle);
            totalVenta = totalVenta.add(subtotal);
            // 2️⃣ Procesar descuentos de insumos (igual que antes)
            String tipoControl = pc.producto.getCategoria().getTipoControl();
            if ("instantaneo".equalsIgnoreCase(tipoControl)) {
                // 🔹 Descuento de insumos usando FIFO con vista kardex
                for (DetalleRecetaProducto drp : receta.getDetalles()) {
                    BigDecimal cantidadRequerida = drp.getCantidad()
                            .multiply(BigDecimal.valueOf(pc.cantidad));
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
                    List<KardexLoteProjection> lotesDisponibles = loteRepository
                            .findAllKardexLotePeps().stream()
                            .filter(k -> k.getInsumoId().equals(drp.getInsumo().getInsumoId())
                                    && k.getCantidadDisponible() > 0)
                            .sorted((a, b) -> a.getFechaEntrada().compareTo(b.getFechaEntrada()))
                            .toList();
                    for (KardexLoteProjection loteKardex : lotesDisponibles) {
                        if (restante.compareTo(BigDecimal.ZERO) <= 0)
                            break;
                        BigDecimal cantidadDisponible = BigDecimal.valueOf(loteKardex.getCantidadDisponible());
                        BigDecimal cantidadDesdeLote = cantidadDisponible.min(restante);
                        Movimiento mov = new Movimiento();
                        mov.setInsumo(drp.getInsumo());
                        mov.setCantidad(cantidadDesdeLote.doubleValue());
                        mov.setCostoUnitario(loteKardex.getCostoUnitario());
                        mov.setTotal(cantidadDesdeLote.multiply(BigDecimal.valueOf(loteKardex.getCostoUnitario()))
                                .doubleValue());
                        mov.setFecha(LocalDateTime.now());
                        TipoMovimientoKardex tipoSalida = new TipoMovimientoKardex();
                        tipoSalida.setId(2);
                        tipoSalida.setNombre("SALIDA");
                        mov.setTipoMovimiento(tipoSalida);
                        Lote loteReal = loteRepository.findById(loteKardex.getLoteId()).orElseThrow();
                        mov.setLote(loteReal);
                        mov.setObservaciones("Venta producto instantáneo: " + pc.producto.getNombre());
                        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                        CustomUserDetails cud = (CustomUserDetails) auth.getPrincipal();
                        User usuarioActual = cud.getUser();
                        mov.setUsuario(usuarioActual);
                        movimientoRepository.save(mov);
                        restante = restante.subtract(cantidadDesdeLote);
                    }
                    if (restante.compareTo(BigDecimal.ZERO) > 0) {
                        System.out.println("⚠️ Insumo " + drp.getInsumo().getNombre()
                                + " no tiene suficiente stock. Falta: " + restante + " " + unidadStock.getNombre());
                    }
                }
            } else if ("elaborado".equalsIgnoreCase(tipoControl)) {
                movimientoProductoService.descontarPorVenta(detalle.getProducto(), detalle.getCantidad());
            }
        }
        venta.setTotal(totalVenta);
        ventaRepository.save(venta);
        System.out.println("✅ Venta registrada con total: " + totalVenta);
    }

    // NUEVO MÉTODO: Generar comprobante en PDF
    public byte[] generarComprobantePDF(Venta venta) throws Exception {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdf = new PdfDocument(writer);
        Document doc = new Document(pdf);

        // ----------------------------------------------------------
        // ESTILO GENERAL
        // ----------------------------------------------------------
        doc.setMargins(40, 40, 40, 40);

        Color verdeCafe = new DeviceRgb(34, 80, 60); // Verde oscuro tipo cafetería
        Color grisSuave = new DeviceRgb(245, 245, 245);
        Color grisTexto = new DeviceRgb(90, 90, 90);

        // ----------------------------------------------------------
        // LOGO "GT" + GREEN TEA
        // ----------------------------------------------------------
        Paragraph logo = new Paragraph("GT")
                .setFontSize(50)
                .setBold()
                .setFontColor(verdeCafe)
                .setTextAlignment(TextAlignment.CENTER);

        Paragraph brand = new Paragraph("GREEN TEA")
                .setFontSize(14)
                .setFontColor(verdeCafe)
                .setTextAlignment(TextAlignment.CENTER);

        doc.add(logo);
        doc.add(brand);
        doc.add(new Paragraph("\n"));

        // ----------------------------------------------------------
        // TÍTULO FACTURA
        // ----------------------------------------------------------
        Paragraph facturaTitulo = new Paragraph("FACTURA")
                .setFontSize(28)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER);

        doc.add(facturaTitulo);
        doc.add(new Paragraph("\n"));

        // ----------------------------------------------------------
        // CLIENTE + DATOS DE FACTURA
        // ----------------------------------------------------------
        Table header = new Table(UnitValue.createPercentArray(new float[] { 1, 1 }))
                .useAllAvailableWidth();

        // CLIENTE
        // CLIENTE + EMPLEADO
        Cell cliente = new Cell().setBorder(Border.NO_BORDER);

        cliente.add(new Paragraph("CLIENTE:").setBold().setFontSize(10));

        if (venta.getCliente() != null) {
            cliente.add(new Paragraph(
                    venta.getCliente().getNombre() + " " +
                            venta.getCliente().getApellido())
                    .setFontColor(grisTexto));

            cliente.add(new Paragraph("Tel: " + venta.getCliente().getTelefono())
                    .setFontColor(grisTexto));
        } else {
            cliente.add(new Paragraph("Cliente general").setFontColor(grisTexto));
        }

        cliente.add(new Paragraph("\nEMPLEADO:").setBold().setFontSize(10));
        cliente.add(new Paragraph(
                venta.getEmpleado().getNombre() + " " +
                        venta.getEmpleado().getApellido())
                .setFontColor(grisTexto));

        header.addCell(cliente);

        // FACTURA INFO
        Cell factura = new Cell().setBorder(Border.NO_BORDER)
                .setTextAlignment(TextAlignment.RIGHT);

        factura.add(new Paragraph("Factura n.º " + venta.getId()).setFontColor(grisTexto));
        factura.add(new Paragraph(venta.getFecha().toLocalDate().toString()).setFontColor(grisTexto));

        header.addCell(factura);

        doc.add(header);

        // Línea separadora
        doc.add(new Paragraph("\n-----------------------------------------\n").setFontColor(grisTexto));

        // ----------------------------------------------------------
        // TABLA DE PRODUCTOS
        // ----------------------------------------------------------
        Table table = new Table(UnitValue.createPercentArray(new float[] { 4, 2, 2, 2 }))
                .useAllAvailableWidth();

        // Encabezados
        String[] enc = { "Producto", "Cantidad", "Precio unitario", "Total" };

        for (String h : enc) {
            table.addHeaderCell(
                    new Cell()
                            .add(new Paragraph(h).setBold().setFontSize(10))
                            .setBackgroundColor(grisSuave)
                            .setBorder(Border.NO_BORDER)
                            .setPadding(6));
        }

        // Detalles
        for (DetalleVenta d : venta.getDetalles()) {
            table.addCell(new Cell().add(new Paragraph(d.getProducto().getNombre())).setBorder(Border.NO_BORDER));
            table.addCell(new Cell().add(new Paragraph(String.valueOf(d.getCantidad()))).setBorder(Border.NO_BORDER));
            table.addCell(new Cell().add(new Paragraph("S/ " + d.getPrecioUnidad())).setBorder(Border.NO_BORDER));
            table.addCell(new Cell().add(new Paragraph("S/ " + d.getTotal())).setBorder(Border.NO_BORDER));
        }

        doc.add(table);
        doc.add(new Paragraph("\n"));
        // ----------------------------------------------------------
        // SUBTOTAL - IGV (8%) INCLUIDO - TOTAL
        // ----------------------------------------------------------
        BigDecimal total = venta.getTotal();

        // Subtotal = total / 1.08
        BigDecimal divisor = new BigDecimal("1.08");
        BigDecimal subtotal = total.divide(divisor, 2, BigDecimal.ROUND_HALF_UP);

        // IGV = total - subtotal
        BigDecimal igv = total.subtract(subtotal).setScale(2, BigDecimal.ROUND_HALF_UP);

        // Total ya incluye IGV
        BigDecimal totalRedondeado = total.setScale(2, BigDecimal.ROUND_HALF_UP);

        Table totales = new Table(UnitValue.createPercentArray(new float[] { 6, 2 }))
                .useAllAvailableWidth();

        totales.addCell(new Cell().add(new Paragraph("Subtotal")).setBorder(Border.NO_BORDER));
        totales.addCell(new Cell().add(new Paragraph("S/ " + subtotal)).setBorder(Border.NO_BORDER));

        totales.addCell(new Cell().add(new Paragraph("IGV (8%)")).setBorder(Border.NO_BORDER));
        totales.addCell(new Cell().add(new Paragraph("S/ " + igv)).setBorder(Border.NO_BORDER));

        Cell totalCell = new Cell(1, 2)
                .add(new Paragraph("Total: S/ " + totalRedondeado)
                        .setBold()
                        .setFontSize(16)
                        .setTextAlignment(TextAlignment.RIGHT))
                .setBorder(Border.NO_BORDER)
                .setPaddingTop(10);

        totales.addCell(totalCell);

        doc.add(totales);

        doc.add(new Paragraph("\n"));

        // ----------------------------------------------------------
        // MENSAJE FINAL
        // ----------------------------------------------------------
        doc.add(new Paragraph("¡Muchas gracias!")
                .setBold()
                .setFontSize(16)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(30));

        doc.close();
        return baos.toByteArray();
    }

}
