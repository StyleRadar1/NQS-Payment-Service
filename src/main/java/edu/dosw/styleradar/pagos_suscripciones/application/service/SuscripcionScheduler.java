package edu.dosw.styleradar.pagos_suscripciones.application.service;

import edu.dosw.styleradar.pagos_suscripciones.domain.model.EstadoSuscripcion;
import edu.dosw.styleradar.pagos_suscripciones.domain.model.Suscripcion;
import edu.dosw.styleradar.pagos_suscripciones.domain.port.out.EventPublisherPort;
import edu.dosw.styleradar.pagos_suscripciones.domain.port.out.SuscripcionRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SuscripcionScheduler {

    private final SuscripcionRepositoryPort suscripcionRepository;
    private final EventPublisherPort eventPublisher;

    // Se ejecuta todos los días a las 00:00
    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void revisarSuscripcionesVencidas() {
        log.info("Iniciando revisión diaria de suscripciones vencidas...");
        
        List<Suscripcion> vencidas = suscripcionRepository.buscarVencidas(LocalDate.now());
        
        for (Suscripcion suscripcion : vencidas) {
            log.warn("Suscripción vencida detectada para suscriptor: {}", suscripcion.getSuscriptorId());
            
            if (suscripcion.getTipoSuscriptor().name().equals("USUARIO")) {
                // SR-RF-103: Revertir a plan gratuito (ID 1)
                suscripcion.revertirAPlanGratuito(1L);
            } else {
                // SR-RF-99: Suspender catálogo de almacén
                suscripcion.suspenderPorFaltaDePago();
            }
            
            suscripcionRepository.guardar(suscripcion);
            // Notificar a ms-tiendas / ms-usuarios para que revoquen beneficios
            eventPublisher.publicarSuscripcionSuspendida(suscripcion);
        }
    }
}