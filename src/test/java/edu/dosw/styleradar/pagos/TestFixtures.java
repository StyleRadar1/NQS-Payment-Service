package edu.dosw.styleradar.pagos;

import edu.dosw.styleradar.pagos.domain.model.EstadoPago;
import edu.dosw.styleradar.pagos.domain.model.EstadoSuscripcion;
import edu.dosw.styleradar.pagos.domain.model.Monto;
import edu.dosw.styleradar.pagos.domain.model.OrdenPago;
import edu.dosw.styleradar.pagos.domain.model.Plan;
import edu.dosw.styleradar.pagos.domain.model.Suscripcion;
import edu.dosw.styleradar.pagos.domain.model.TipoSuscriptor;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Map;

/** Datos de prueba compartidos por las capas de aplicación e infraestructura. */
public final class TestFixtures {

    public static final LocalDate HOY = LocalDate.of(2026, 11, 14);
    public static final LocalDateTime AHORA = LocalDateTime.of(2026, 11, 14, 10, 0);
    public static final Clock CLOCK = Clock.fixed(
            LocalDateTime.of(2026, 11, 14, 10, 0).toInstant(ZoneOffset.UTC), ZoneOffset.UTC);

    private TestFixtures() {
    }

    public static Plan planRadarPlus() {
        return new Plan(2L, "Radar+", TipoSuscriptor.USUARIO,
                Monto.cop(new BigDecimal("14900.00")),
                Map.of("max_wishlist", -1, "probador_foto", true), true);
    }

    public static Plan planAlmacen() {
        return new Plan(3L, "Almacén Básico", TipoSuscriptor.ALMACEN,
                Monto.cop(new BigDecimal("29900.00")),
                Map.of("max_productos", 50), true);
    }

    public static Suscripcion pendienteUsuario(String suscriptorId) {
        return Suscripcion.pendiente(suscriptorId, TipoSuscriptor.USUARIO, 2L, AHORA);
    }

    public static Suscripcion pendienteAlmacen(String suscriptorId, String nit) {
        Suscripcion suscripcion = Suscripcion.pendiente(suscriptorId, TipoSuscriptor.ALMACEN, 3L, AHORA);
        suscripcion.asignarNit(nit);
        return suscripcion;
    }

    public static Suscripcion activaUsuario(String suscriptorId, LocalDate inicio, LocalDate fin) {
        return Suscripcion.rehidratar(100L, suscriptorId, TipoSuscriptor.USUARIO, 2L, null,
                EstadoSuscripcion.ACTIVA, inicio, fin, true, "PRE-100", null, AHORA);
    }

    public static Suscripcion activaAlmacen(String suscriptorId, String nit, LocalDate inicio, LocalDate fin) {
        return Suscripcion.rehidratar(200L, suscriptorId, TipoSuscriptor.ALMACEN, 3L, null,
                EstadoSuscripcion.ACTIVA, inicio, fin, true, "PRE-200", nit, AHORA);
    }

    public static Suscripcion suspendidaUsuario(String suscriptorId) {
        return Suscripcion.rehidratar(101L, suscriptorId, TipoSuscriptor.USUARIO, 2L, null,
                EstadoSuscripcion.SUSPENDIDA, HOY.minusMonths(2), HOY.minusMonths(1),
                false, "PRE-101", null, AHORA);
    }

    public static OrdenPago pendiente(Long suscripcionId, String valor) {
        return OrdenPago.pendiente(suscripcionId, Monto.cop(new BigDecimal(valor)), AHORA);
    }

    public static OrdenPago aprobada(Long suscripcionId, String valor) {
        OrdenPago orden = pendiente(suscripcionId, valor);
        orden.actualizarEstado(EstadoPago.APROBADO, "MP-1");
        return orden;
    }

    public static Suscripcion conIdSuscripcion(Suscripcion suscripcion, Long id) {
        return Suscripcion.rehidratar(id, suscripcion.getSuscriptorId(), suscripcion.getTipoSuscriptor(),
                suscripcion.getPlanId(), suscripcion.getPlanProgramadoId(), suscripcion.getEstado(),
                suscripcion.getFechaInicio(), suscripcion.getFechaFin(), suscripcion.isAutoRenovacion(),
                suscripcion.getPreapprovalIdMp(), suscripcion.getNit(), suscripcion.getFechaActualizacion());
    }

    public static OrdenPago conIdOrden(OrdenPago orden, Long id) {
        return OrdenPago.rehidratar(id, orden.getSuscripcionId(), orden.getMonto(), orden.getEstado(),
                orden.getIdPagoExterno(), orden.getMontoReembolsado(), orden.getMotivoReembolso(),
                orden.getFechaCreacion());
    }
}
