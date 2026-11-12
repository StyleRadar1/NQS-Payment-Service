package edu.dosw.styleradar.pagos_suscripciones.infrastructure.adapter.out.persistence;

import edu.dosw.styleradar.pagos_suscripciones.domain.model.Plan;
import edu.dosw.styleradar.pagos_suscripciones.infrastructure.adapter.out.persistence.entity.PlanEntity;
import edu.dosw.styleradar.pagos_suscripciones.infrastructure.adapter.out.persistence.repository.PlanJpaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlanPersistenceAdapterTest {

    @Mock private PlanJpaRepository repo;
    @InjectMocks private PlanPersistenceAdapter adapter;

    private PlanEntity entity(Long id, boolean activo) {
        PlanEntity e = new PlanEntity();
        e.setId(id);
        e.setNombre("Plan" + id);
        e.setTipoSuscriptor(edu.dosw.styleradar.pagos_suscripciones.domain.model.TipoSuscriptor.USUARIO);
        e.setPrecio(BigDecimal.TEN);
        e.setMoneda("COP");
        e.setLimites(Map.of());
        e.setActivo(activo);
        return e;
    }

    @Test
    void debeBuscarPorIdActivo() {
        when(repo.findById(1L)).thenReturn(Optional.of(entity(1L, true)));
        Optional<Plan> p = adapter.buscarPorId(1L);
        assertTrue(p.isPresent());
        assertTrue(p.get().isActivo());
    }

    @Test
    void debeDesactivarPlanInactivo() {
        when(repo.findById(1L)).thenReturn(Optional.of(entity(1L, false)));
        Optional<Plan> p = adapter.buscarPorId(1L);
        assertTrue(p.isPresent());
        assertFalse(p.get().isActivo());
    }

    @Test
    void debeRetornarVacioSiNoExiste() {
        when(repo.findById(99L)).thenReturn(Optional.empty());
        assertTrue(adapter.buscarPorId(99L).isEmpty());
    }

    @Test
    void listarActivosDebeFiltrarInactivos() {
        when(repo.findByActivoTrue()).thenReturn(List.of(entity(1L, true), entity(2L, true)));
        List<Plan> planes = adapter.listarActivos();
        assertEquals(2, planes.size());
        assertTrue(planes.stream().allMatch(Plan::isActivo));
    }
}