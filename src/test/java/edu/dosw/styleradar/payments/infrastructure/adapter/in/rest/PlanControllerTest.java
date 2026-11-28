package edu.dosw.styleradar.payments.infrastructure.adapter.in.rest;

import edu.dosw.styleradar.payments.TestFixtures;
import edu.dosw.styleradar.payments.application.dto.response.PlanResponse;
import edu.dosw.styleradar.payments.application.port.in.PlanPort;
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
class PlanControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private PlanPort planPort;

    @Test
    void listaPlanes() throws Exception {
        when(planPort.list()).thenReturn(List.of(
                PlanResponse.from(TestFixtures.planRadarPlus()),
                PlanResponse.from(TestFixtures.planStore())));

        mockMvc.perform(get("/api/v1/plans"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Radar+"))
                .andExpect(jsonPath("$[0].price.value").value(14900.00))
                .andExpect(jsonPath("$[0].limits.probador_foto").value(true))
                .andExpect(jsonPath("$[1].subscriberType").value("STORE"));
    }
}

