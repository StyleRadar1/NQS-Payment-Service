package edu.dosw.styleradar.pagos_suscripciones.application.usecase;

import edu.dosw.styleradar.pagos_suscripciones.domain.model.Monto;
import edu.dosw.styleradar.pagos_suscripciones.domain.model.OrdenPago;
import edu.dosw.styleradar.pagos_suscripciones.domain.port.in.PagoPort;
import edu.dosw.styleradar.pagos_suscripciones.domain.port.out.MercadoPagoPort;
import edu.dosw.styleradar.pagos_suscripciones.domain.port.out.OrdenPagoRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class ReembolsarPagoUseCaseImpl implements PagoPort {

    private final OrdenPagoRepositoryPort ordenPagoRepositoryPort;
    private final MercadoPagoPort mercadoPagoPort;

    @Override
    @Transactional
    public void reembolsarPago(Long ordenPagoId, String motivo) {
        OrdenPago orden = ordenPagoRepositoryPort.buscarPorId(ordenPagoId)
                .orElseThrow(() -> new IllegalArgumentException("Orden no encontrada"));
        
        // Asumimos un reembolso total para este ejemplo
        Monto montoReembolso = orden.getMonto();
        
        orden.reembolsar(montoReembolso, motivo);
        mercadoPagoPort.reembolsarPago(orden.getIdPagoExterno(), montoReembolso.valor().toString());
        
        ordenPagoRepositoryPort.guardar(orden);
    }
}