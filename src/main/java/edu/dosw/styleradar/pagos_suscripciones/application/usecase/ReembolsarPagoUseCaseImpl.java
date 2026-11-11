package edu.dosw.styleradar.pagos_suscripciones.application.usecase;

import edu.dosw.styleradar.pagos_suscripciones.domain.exception.PaymentProcessingException;
import edu.dosw.styleradar.pagos_suscripciones.domain.model.Monto;
import edu.dosw.styleradar.pagos_suscripciones.domain.model.OrdenPago;
import edu.dosw.styleradar.pagos_suscripciones.domain.port.in.PagoPort;
import edu.dosw.styleradar.pagos_suscripciones.domain.port.out.MercadoPagoPort;
import edu.dosw.styleradar.pagos_suscripciones.domain.port.out.OrdenPagoRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReembolsarPagoUseCaseImpl implements PagoPort {

    private final OrdenPagoRepositoryPort ordenPagoRepository;
    private final MercadoPagoPort mercadoPagoPort;

    @Override
    @Transactional
    public void reembolsarPago(Long ordenPagoId, String montoReembolsoStr, String motivo) {
        log.info("Iniciando reembolso para orden: {}", ordenPagoId);
        
        OrdenPago orden = ordenPagoRepository.buscarPorId(ordenPagoId)
                .orElseThrow(() -> new PaymentProcessingException("Orden de pago no encontrada"));

        Monto montoReembolso = new Monto(new BigDecimal(montoReembolsoStr), orden.getMonto().moneda());
        
        // Lógica de dominio
        orden.reembolsar(montoReembolso, motivo);
        
        // Llamada a pasarela (Mock o Real)
        mercadoPagoPort.reembolsarPago(orden.getIdPagoExterno(), montoReembolsoStr);
        
        ordenPagoRepository.guardar(orden);
        log.info("Reembolso procesado exitosamente para orden: {}", ordenPagoId);
    }

    @Override
    public List<OrdenPago> consultarHistorialPagos(String suscriptorId) {
        log.info("Consultando historial de pagos para suscriptor: {}", suscriptorId);
        return ordenPagoRepository.buscarPorSuscriptorId(suscriptorId);
    }
}