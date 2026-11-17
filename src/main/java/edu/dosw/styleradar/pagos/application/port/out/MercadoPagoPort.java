package edu.dosw.styleradar.pagos.application.port.out;

import edu.dosw.styleradar.pagos.domain.model.Monto;

import java.math.BigDecimal;

/**
 * Puerto de salida hacia Mercado Pago (ADR-04).
 *
 * <p>Se usa la API REST mediante OpenFeign; no se usa el SDK oficial.
 * SR-RNF-13: los datos de tarjeta nunca pasan por este servicio.</p>
 */
public interface MercadoPagoPort {

    PreapprovalCreado crearPreapproval(PreapprovalSolicitud solicitud);

    PagoMp consultarPago(String idPago);

    ReembolsoMp reembolsar(String idPago, BigDecimal monto);

    void cancelarPreapproval(String idPreapproval);

    record PreapprovalSolicitud(
            String razon,
            String referenciaExterna,
            String correoPagador,
            Monto monto,
            String urlRetorno) {
    }

    record PreapprovalCreado(String id, String initPoint, String estado) {
    }

    record PagoMp(
            String id,
            String estado,
            String referenciaExterna,
            String preapprovalId,
            BigDecimal monto,
            String moneda) {
    }

    record ReembolsoMp(String id, BigDecimal monto) {
    }
}
