package edu.dosw.styleradar.pagos_suscripciones.infrastructure.adapter.in.rest;

import edu.dosw.styleradar.pagos_suscripciones.domain.model.EstadoPago;
import edu.dosw.styleradar.pagos_suscripciones.domain.model.Monto;
import edu.dosw.styleradar.pagos_suscripciones.domain.model.OrdenPago;
import edu.dosw.styleradar.pagos_suscripciones.domain.port.in.AdminPagoPort;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminPagoController.class)
class AdminPagoControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockBean private AdminPagoPort adminPagoPort;

    @Test
    void debeRetornarPagosConRolAdmin() throws Exception {
        OrdenPago o = new OrdenPago(1L, 100L, new Monto(BigDecimal.TEN, "COP"), "MP-1");
        when(adminPagoPort.consultarPagos(null)).thenReturn(List.of(o));

        mockMvc.perform(get("/api/v1/admin/pagos")
                .header("X-User-Role", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void debeRetornar403SiNoEsAdmin() throws Exception {
        mockMvc.perform(get("/api/v1/admin/pagos")
                .header("X-User-Role", "USER"))
                .andExpect(status().isForbidden());

        verify(adminPagoPort, never()).consultarPagos(any());
    }

    @Test
    void debeRetornar403SinHeaderDeRol() throws Exception {
        mockMvc.perform(get("/api/v1/admin/pagos"))
                .andExpect(status().isForbidden());
    }

    @Test
    void debeFiltrarPorEstado() throws Exception {
        when(adminPagoPort.consultarPagos(EstadoPago.APROBADO)).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/admin/pagos")
                .header("X-User-Role", "ADMIN")
                .param("estado", "APROBADO"))
                .andExpect(status().isOk());

        verify(adminPagoPort).consultarPagos(EstadoPago.APROBADO);
    }
}