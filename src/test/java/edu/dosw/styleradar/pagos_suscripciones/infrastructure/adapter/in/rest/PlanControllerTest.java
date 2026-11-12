package edu.dosw.styleradar.pagos_suscripciones.infrastructure.adapter.in.rest;

import edu.dosw.styleradar.pagos_suscripciones.domain.model.Monto;
import edu.dosw.styleradar.pagos_suscripciones.domain.model.Plan;
import edu.dosw.styleradar.pagos_suscripciones.domain.model.TipoSuscriptor;
import edu.dosw.styleradar.pagos_suscripciones.domain.port.out.PlanRepositoryPort;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PlanController.class)
@ActiveProfiles("test")
class PlanControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockBean private PlanRepositoryPort planRepository;

    @Test
    void debeListarPlanes() throws Exception {
        Plan p = new Plan(1L, "Radar", TipoSuscriptor.USUARIO,
                new Monto(BigDecimal.ZERO, "COP"), Map.of());
        when(planRepository.listarActivos()).thenReturn(List.of(p));

        mockMvc.perform(get("/api/v1/planes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Radar"));
    }

    @Test
    void debeRetornarListaVaciaSiNoHayPlanes() throws Exception {
        when(planRepository.listarActivos()).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/planes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }
}