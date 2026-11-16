package edu.dosw.styleradar.pagos.application.service;

import edu.dosw.styleradar.pagos.domain.model.EstadoPago;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("EstadoPagoMapper (SR-RF-94 RN-03)")
class EstadoPagoMapperTest {

    @Test
    void traduceEstadosAprobados() {
        assertThat(EstadoPagoMapper.desdeMercadoPago("approved")).contains(EstadoPago.APROBADO);
        assertThat(EstadoPagoMapper.desdeMercadoPago("APPROVED")).contains(EstadoPago.APROBADO);
    }

    @Test
    void traduceEstadosPendientes() {
        assertThat(EstadoPagoMapper.desdeMercadoPago("pending")).contains(EstadoPago.PENDIENTE);
        assertThat(EstadoPagoMapper.desdeMercadoPago("in_process")).contains(EstadoPago.PENDIENTE);
        assertThat(EstadoPagoMapper.desdeMercadoPago("authorized")).contains(EstadoPago.PENDIENTE);
        assertThat(EstadoPagoMapper.desdeMercadoPago("in_mediation")).contains(EstadoPago.PENDIENTE);
    }

    @Test
    void traduceEstadosRechazados() {
        assertThat(EstadoPagoMapper.desdeMercadoPago("rejected")).contains(EstadoPago.RECHAZADO);
        assertThat(EstadoPagoMapper.desdeMercadoPago("cancelled")).contains(EstadoPago.RECHAZADO);
        assertThat(EstadoPagoMapper.desdeMercadoPago("canceled")).contains(EstadoPago.RECHAZADO);
    }

    @Test
    void traduceEstadosReembolsados() {
        assertThat(EstadoPagoMapper.desdeMercadoPago("refunded")).contains(EstadoPago.REEMBOLSADO);
        assertThat(EstadoPagoMapper.desdeMercadoPago("charged_back")).contains(EstadoPago.REEMBOLSADO);
    }

    @Test
    void ignoraEstadosDesconocidos() {
        assertThat(EstadoPagoMapper.desdeMercadoPago("otro")).isEmpty();
        assertThat(EstadoPagoMapper.desdeMercadoPago("")).isEmpty();
        assertThat(EstadoPagoMapper.desdeMercadoPago(null)).isEmpty();
    }
}
