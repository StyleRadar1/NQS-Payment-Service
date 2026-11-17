package edu.dosw.styleradar.pagos.application.port.out;

import java.time.Duration;

/** Puerto de salida para el bloqueo distribuido de idempotencia (ADR-06). */
public interface IdempotencyPort {

    boolean intentarBloquear(String clave, Duration ttl);

    void liberar(String clave);
}
