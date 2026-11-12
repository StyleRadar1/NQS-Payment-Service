package edu.dosw.styleradar.pagos_suscripciones.application.usecase;

import edu.dosw.styleradar.pagos_suscripciones.domain.exception.PaymentProcessingException;
import edu.dosw.styleradar.pagos_suscripciones.domain.model.*;
import edu.dosw.styleradar.pagos_suscripciones.domain.port.in.PagoPort;
import edu.dosw.styleradar.pagos_suscripciones.domain.port.out.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReembolsarPagoUseCaseImpl implements PagoPort {

    private final OrdenPagoRepositoryPort ordenPagoRepository;
    private final SuscripcionRepositoryPort suscripcionRepository;
    private final PlanRepositoryPort planRepository;
    private final MercadoPagoPort mercadoPagoPort;

    @Override
    @Transactional
    public void reembolsarPago(Long ordenPagoId, String montoReembolsoStr, String motivo) {
        log.info("Reembolso solicitado para orden {}: monto={}, motivo={}", ordenPagoId, montoReembolsoStr, motivo);

        OrdenPago orden = ordenPagoRepository.buscarPorId(ordenPagoId)
                .orElseThrow(() -> new PaymentProcessingException("Orden de pago no encontrada"));

        if (orden.getEstado() != EstadoPago.APROBADO && orden.getEstado() != EstadoPago.REEMBOLSADO) {
            throw new IllegalStateException("Solo se reembolsan pagos aprobados");
        }

        Monto montoReembolso = new Monto(new BigDecimal(montoReembolsoStr), orden.getMonto().moneda());
        orden.reembolsar(montoReembolso, motivo);
        mercadoPagoPort.reembolsarPago(orden.getIdPagoExterno(), montoReembolsoStr);
        ordenPagoRepository.guardar(orden);

        // SR-RF-98 FA-02: reembolso total cancela la suscripción
        if (orden.fueReembolsadoTotalmente()) {
            suscripcionRepository.buscarPorId(orden.getSuscripcionId()).ifPresent(sub -> {
                sub.cancelarRenovacion();
                if (sub.getPreapprovalIdMp() != null) {
                    try {
                        mercadoPagoPort.cancelarPreapproval(sub.getPreapprovalIdMp());
                    } catch (Exception e) {
                        log.warn("No se pudo cancelar preapproval: {}", e.getMessage());
                    }
                }
                suscripcionRepository.guardar(sub);
                log.info("Suscripción {} cancelada tras reembolso total", sub.getId());
            });
        }
    }

    @Override
    public List<OrdenPago> consultarHistorialPagos(String suscriptorId) {
        log.info("Historial de pagos para: {}", suscriptorId);
        return ordenPagoRepository.buscarPorSuscriptorId(suscriptorId);
    }

    @Override
    @Transactional
    public String reintentarPago(Long ordenPagoId, String suscriptorId, String emailPayer) {
        log.info("Reintento de pago para orden {} por {}", ordenPagoId, suscriptorId);

        OrdenPago ordenOriginal = ordenPagoRepository.buscarPorId(ordenPagoId)
                .orElseThrow(() -> new PaymentProcessingException("Orden de pago no encontrada"));

        // RN-01: no re-intentar si ya hay un pago aprobado para el mismo período
        Suscripcion sub = suscripcionRepository.buscarPorId(ordenOriginal.getSuscripcionId())
                .orElseThrow(() -> new PaymentProcessingException("Suscripción asociada no encontrada"));

        if (!sub.getSuscriptorId().equals(suscriptorId)) {
            throw new SecurityException("No puede reintentar pagos de otro suscriptor.");
        }

        if (sub.estaActiva()) {
            throw new IllegalStateException(
                "La suscripción ya está activa. No se requiere reintentar el pago.");
        }

        if (ordenOriginal.getEstado() != EstadoPago.RECHAZADO
                && ordenOriginal.getEstado() != EstadoPago.PENDIENTE) {
            throw new IllegalStateException(
                "Solo se pueden reintentar pagos rechazados o pendientes.");
        }

        // Crear nueva orden
        String ref = UUID.randomUUID().toString();
        OrdenPago nueva = new OrdenPago(null, sub.getId(), ordenOriginal.getMonto(), ref);
        ordenPagoRepository.guardar(nueva);

        Map<String, String> mp = mercadoPagoPort.crearPreferenciaPago(nueva, sub, emailPayer);
        return mp.get("init_point");
    }
}