package edu.dosw.styleradar.pagos.infrastructure.adapter.out.persistence;

import edu.dosw.styleradar.pagos.application.port.out.WebhookLogPort;
import edu.dosw.styleradar.pagos.infrastructure.adapter.out.persistence.entity.WebhookLogEntity;
import edu.dosw.styleradar.pagos.infrastructure.adapter.out.persistence.repository.WebhookLogJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;

/** Adaptador JPA de la auditoría de webhooks (SR-RNF-14). */
@Slf4j
@Component
@RequiredArgsConstructor
public class WebhookLogPersistenceAdapter implements WebhookLogPort {

    private final WebhookLogJpaRepository webhookLogJpaRepository;

    @Override
    public boolean existe(String idPagoExterno) {
        return webhookLogJpaRepository.existsByIdPagoExterno(idPagoExterno);
    }

    @Override
    public void registrar(String idPagoExterno, Map<String, Object> payload, String estadoProcesamiento) {
        WebhookLogEntity entity = WebhookLogEntity.builder()
                .idPagoExterno(idPagoExterno)
                .payloadRecibido(payload)
                .estadoProcesamiento(estadoProcesamiento)
                .fechaRecepcion(LocalDateTime.now())
                .build();
        try {
            webhookLogJpaRepository.save(entity);
        } catch (DataIntegrityViolationException ex) {
            // Otro hilo ya registró la misma notificación; la restricción única
            // es la segunda barrera de idempotencia (ADR-06).
            log.debug("Webhook {} ya registrado por otra transacción", idPagoExterno);
        }
    }
}
