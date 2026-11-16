package edu.dosw.styleradar.pagos.domain.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Agregado raíz que representa la suscripción de un usuario o de un almacén.
 *
 * <p>Reglas principales:</p>
 * <ul>
 *   <li>SR-RF-67: nace PENDIENTE y se activa solo con pago APROBADO.</li>
 *   <li>SR-RF-68 y SR-RF-101: cancelar deshabilita la renovación, no el periodo pagado.</li>
 *   <li>SR-RF-95: la renovación extiende un mes desde la fecha de fin vigente; si ya
 *       venció, cuenta desde la fecha del nuevo pago aprobado.</li>
 *   <li>SR-RF-99 y SR-RF-103: al vencer sin pago aprobado pasa a SUSPENDIDA,
 *       sin periodo de gracia.</li>
 * </ul>
 */
public final class Suscripcion {

    private Long id;
    private final String suscriptorId;
    private final TipoSuscriptor tipoSuscriptor;
    private final Long planId;
    private final Long planProgramadoId;
    private EstadoSuscripcion estado;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private boolean autoRenovacion;
    private String preapprovalIdMp;
    private String nit;
    private java.util.UUID almacenId;
    private LocalDateTime fechaActualizacion;

    private Suscripcion(Long id,
                        String suscriptorId,
                        TipoSuscriptor tipoSuscriptor,
                        Long planId,
                        Long planProgramadoId,
                        EstadoSuscripcion estado,
                        LocalDate fechaInicio,
                        LocalDate fechaFin,
                        boolean autoRenovacion,
                        String preapprovalIdMp,
                        String nit,
                        java.util.UUID almacenId,
                        LocalDateTime fechaActualizacion) {
        if (suscriptorId == null || suscriptorId.isBlank()) {
            throw new IllegalArgumentException("El suscriptor es obligatorio.");
        }
        this.id = id;
        this.suscriptorId = suscriptorId.trim();
        this.tipoSuscriptor = Objects.requireNonNull(tipoSuscriptor, "El tipo de suscriptor es obligatorio.");
        this.planId = Objects.requireNonNull(planId, "El plan es obligatorio.");
        this.planProgramadoId = planProgramadoId;
        this.estado = Objects.requireNonNull(estado, "El estado de la suscripción es obligatorio.");
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.autoRenovacion = autoRenovacion;
        this.preapprovalIdMp = preapprovalIdMp;
        this.nit = nit;
        this.almacenId = almacenId;
        this.fechaActualizacion = fechaActualizacion == null ? LocalDateTime.now() : fechaActualizacion;
    }

    public static Suscripcion pendiente(String suscriptorId,
                                        TipoSuscriptor tipoSuscriptor,
                                        Long planId,
                                        LocalDateTime ahora) {
        return new Suscripcion(null, suscriptorId, tipoSuscriptor, planId, null,
                EstadoSuscripcion.PENDIENTE, null, null, true, null, null, null, ahora);
    }

    public static Suscripcion rehidratar(Long id,
                                         String suscriptorId,
                                         TipoSuscriptor tipoSuscriptor,
                                         Long planId,
                                         Long planProgramadoId,
                                         EstadoSuscripcion estado,
                                         LocalDate fechaInicio,
                                         LocalDate fechaFin,
                                         boolean autoRenovacion,
                                         String preapprovalIdMp,
                                         String nit,
                                         LocalDateTime fechaActualizacion) {
        return rehidratar(id, suscriptorId, tipoSuscriptor, planId, planProgramadoId, estado,
                fechaInicio, fechaFin, autoRenovacion, preapprovalIdMp, nit, null, fechaActualizacion);
    }

    public static Suscripcion rehidratar(Long id,
                                         String suscriptorId,
                                         TipoSuscriptor tipoSuscriptor,
                                         Long planId,
                                         Long planProgramadoId,
                                         EstadoSuscripcion estado,
                                         LocalDate fechaInicio,
                                         LocalDate fechaFin,
                                         boolean autoRenovacion,
                                         String preapprovalIdMp,
                                         String nit,
                                         java.util.UUID almacenId,
                                         LocalDateTime fechaActualizacion) {
        return new Suscripcion(id, suscriptorId, tipoSuscriptor, planId, planProgramadoId,
                estado, fechaInicio, fechaFin, autoRenovacion, preapprovalIdMp, nit, almacenId,
                fechaActualizacion);
    }

    /**
     * Asigna el identificador de la preapproval de Mercado Pago.
     * SR-RF-93 y SR-RF-94: permite conciliar el webhook con la suscripción.
     */
    public void asignarPreapproval(String preapprovalId) {
        if (preapprovalId == null || preapprovalId.isBlank()) {
            throw new IllegalArgumentException("El identificador de la preapproval es obligatorio.");
        }
        this.preapprovalIdMp = preapprovalId.trim();
        this.fechaActualizacion = LocalDateTime.now();
    }

    /**
     * Referencia tributaria del almacén (NIT) para que catálogo y tiendas
     * puedan correlacionar la suspensión sin consultar la base de ms-tiendas.
     * SR-RF-99: identifica el catálogo que debe ocultarse. No aplica a USUARIO.
     */
    public void asignarNit(String nit) {
        if (nit == null || nit.isBlank()) {
            this.nit = null;
            return;
        }
        String normalizado = nit.trim();
        if (normalizado.length() > 30) {
            throw new IllegalArgumentException("El NIT no puede superar 30 caracteres.");
        }
        this.nit = normalizado;
        this.fechaActualizacion = LocalDateTime.now();
    }

    /**
     * Identificador UUID del almacén en el catálogo y la geolocalización.
     * SR-RF-99: ms-item-catalogo suspende el catálogo con este identificador
     * sin necesidad de consultar la base de datos de ms-tiendas.
     */
    public void asignarAlmacenId(java.util.UUID almacenId) {
        this.almacenId = almacenId;
        this.fechaActualizacion = LocalDateTime.now();
    }

    /**
     * Activa la suscripción por primera vez o extiende su vigencia.
     *
     * <p>SR-RF-67: la primera activación asigna fecha de inicio y fin.
     * SR-RF-95: si sigue vigente, extiende un mes desde la fecha de fin;
     * si ya venció, cuenta desde la fecha del pago aprobado.</p>
     */
    public void activar(LocalDate fechaPago) {
        Objects.requireNonNull(fechaPago, "La fecha del pago es obligatoria.");
        boolean vigente = estado == EstadoSuscripcion.ACTIVA
                && fechaFin != null
                && !fechaFin.isBefore(fechaPago);
        if (vigente) {
            this.fechaFin = this.fechaFin.plusMonths(1);
        } else {
            this.fechaInicio = fechaPago;
            this.fechaFin = fechaPago.plusMonths(1);
        }
        this.estado = EstadoSuscripcion.ACTIVA;
        this.autoRenovacion = true;
        this.fechaActualizacion = LocalDateTime.now();
    }

    /**
     * Deshabilita la renovación sin interrumpir el periodo ya pagado.
     * SR-RF-68 RN-01/RN-02 y SR-RF-101 RN-01/RN-02.
     */
    public void cancelarRenovacion() {
        if (estado != EstadoSuscripcion.ACTIVA) {
            throw new IllegalStateException("Solo se puede cancelar la renovación de una suscripción activa.");
        }
        this.autoRenovacion = false;
        this.fechaActualizacion = LocalDateTime.now();
    }

    /**
     * Suspende la suscripción al vencer el periodo sin pago aprobado.
     * SR-RF-99 RN-01/RN-02 y SR-RF-103 RN-01: sin periodo de gracia.
     */
    public void suspenderPorFaltaDePago(LocalDate hoy) {
        Objects.requireNonNull(hoy, "La fecha de suspensión es obligatoria.");
        if (estado != EstadoSuscripcion.ACTIVA) {
            throw new IllegalStateException("Solo se puede suspender una suscripción activa.");
        }
        if (fechaFin != null && !fechaFin.isBefore(hoy)) {
            throw new IllegalStateException("La suscripción aún no ha vencido.");
        }
        this.estado = EstadoSuscripcion.SUSPENDIDA;
        this.autoRenovacion = false;
        this.fechaActualizacion = LocalDateTime.now();
    }

    public boolean estaVigente(LocalDate hoy) {
        return estado == EstadoSuscripcion.ACTIVA
                && fechaFin != null
                && !fechaFin.isBefore(hoy);
    }

    /**
     * SR-RF-67: el acceso a las funcionalidades del almacén se habilita
     * únicamente cuando la suscripción está ACTIVA.
     */
    public boolean permiteAcceso(LocalDate hoy) {
        return estaVigente(hoy);
    }

    public boolean esDeTipo(TipoSuscriptor tipo) {
        return tipoSuscriptor == tipo;
    }

    public boolean esPendiente() {
        return estado == EstadoSuscripcion.PENDIENTE;
    }

    public boolean esActiva() {
        return estado == EstadoSuscripcion.ACTIVA;
    }

    public boolean esSuspendida() {
        return estado == EstadoSuscripcion.SUSPENDIDA;
    }

    public void marcarActualizada() {
        this.fechaActualizacion = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getSuscriptorId() {
        return suscriptorId;
    }

    public TipoSuscriptor getTipoSuscriptor() {
        return tipoSuscriptor;
    }

    public Long getPlanId() {
        return planId;
    }

    public Long getPlanProgramadoId() {
        return planProgramadoId;
    }

    public EstadoSuscripcion getEstado() {
        return estado;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public LocalDate getFechaFin() {
        return fechaFin;
    }

    public boolean isAutoRenovacion() {
        return autoRenovacion;
    }

    public String getPreapprovalIdMp() {
        return preapprovalIdMp;
    }

    public String getNit() {
        return nit;
    }

    public java.util.UUID getAlmacenId() {
        return almacenId;
    }

    public LocalDateTime getFechaActualizacion() {
        return fechaActualizacion;
    }
}
