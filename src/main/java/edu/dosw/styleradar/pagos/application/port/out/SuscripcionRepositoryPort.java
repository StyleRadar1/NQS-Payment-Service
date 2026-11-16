package edu.dosw.styleradar.pagos.application.port.out;

import edu.dosw.styleradar.pagos.domain.model.Suscripcion;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/** Puerto de salida para la persistencia de suscripciones. */
public interface SuscripcionRepositoryPort {

    Suscripcion guardar(Suscripcion suscripcion);

    Optional<Suscripcion> buscarPorId(Long id);

    Optional<Suscripcion> buscarUltimaPorSuscriptor(String suscriptorId);

    /** SR-RF-67 RN-04: una suscripción ACTIVA o PENDIENTE por suscriptor. */
    Optional<Suscripcion> buscarActivaOPendiente(String suscriptorId);

    Optional<Suscripcion> buscarPorPreapprovalId(String preapprovalId);

    /** SR-RF-99/103: suscripciones ACTIVAS cuyo periodo ya venció. */
    List<Suscripcion> buscarActivasVencidas(LocalDate hoy);
}
