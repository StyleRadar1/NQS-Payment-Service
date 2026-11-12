package edu.dosw.styleradar.pagos_suscripciones.application.service;

import edu.dosw.styleradar.pagos_suscripciones.domain.model.EstadoPago;
import edu.dosw.styleradar.pagos_suscripciones.domain.model.OrdenPago;
import edu.dosw.styleradar.pagos_suscripciones.domain.port.out.MercadoPagoPort;
import edu.dosw.styleradar.pagos_suscripciones.domain.port.out.OrdenPagoRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConciliacionScheduler {

    private final OrdenPagoRepositoryPort ordenPagoRepository;
    private final MercadoPagoPort mercadoPagoPort;
    private final SuscripcionActivacionService suscripcionActivacionService;

    /** Diario a las 3 AM: revisa órdenes pendientes >2h y concilia con MP. */
    @Scheduled(cron = "0 0 3 * * ?")
    public void conciliarPagosPendientes() {
        LocalDateTime limite = LocalDateTime.now().minusHours(2);
        List<OrdenPago> pendientes = ordenPagoRepository.buscarPendientesAntesDe(limite);
        log.info("Conciliación: {} pagos pendientes a revisar", pendientes.size());

        for (OrdenPago orden : pendientes) {
            conciliarOrdenIndividual(orden);
        }
    }

    private void conciliarOrdenIndividual(OrdenPago orden) {
        if (orden.getIdPagoExterno() == null) return;
        try {
            String estadoReal = mercadoPagoPort.consultarEstadoPago(orden.getIdPagoExterno());
            if (estadoReal == null) return;

            EstadoPago nuevoEstado = parseEstado(estadoReal);
            if (nuevoEstado != null && nuevoEstado != orden.getEstado()) {
                log.info("Conciliación: orden {} {} → {}", orden.getId(), orden.getEstado(), nuevoEstado);
                orden.actualizarEstado(nuevoEstado.name());
                ordenPagoRepository.guardar(orden);

                // FIX SR-RNF-14: si la pasarela confirma APPROVED, activar la suscripción
                if (nuevoEstado == EstadoPago.APROBADO) {
                    suscripcionActivacionService.activarYExtenderSuscripcion(orden);
                }
            }
        } catch (Exception e) {
            log.warn("Error conciliando orden {}: {}", orden.getId(), e.getMessage());
        }
    }

    private EstadoPago parseEstado(String raw) {
        return switch (raw.toLowerCase()) {
            case "approved" -> EstadoPago.APROBADO;
            case "rejected", "cancelled" -> EstadoPago.RECHAZADO;
            case "pending", "in_process", "authorized", "in_mediation" -> EstadoPago.PENDIENTE;
            case "refunded", "charged_back" -> EstadoPago.REEMBOLSADO;
            default -> null;
        };
    }
}