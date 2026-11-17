package edu.dosw.styleradar.pagos.infrastructure.adapter.in.rest;

import edu.dosw.styleradar.pagos.TestFixtures;
import edu.dosw.styleradar.pagos.application.dto.response.PlanResponse;
import edu.dosw.styleradar.pagos.application.port.in.PlanPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = PlanController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("PlanController (SR-RF-100/102)")
class PlanControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private PlanPort planPort;

    @Test
    void listaPlanes() throws Exception {
        when(planPort.listar()).thenReturn(List.of(
                PlanResponse.desde(TestFixtures.planRadarPlus()),
                PlanResponse.desde(TestFixtures.planAlmacen())));

        mockMvc.perform(get("/api/v1/planes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Radar+"))
                .andExpect(jsonPath("$[0].precio.valor").value(14900.00))
                .andExpect(jsonPath("$[0].limites.probador_foto").value(true))
                .andExpect(jsonPath("$[1].tipoSuscriptor").value("ALMACEN"));
    }
}
