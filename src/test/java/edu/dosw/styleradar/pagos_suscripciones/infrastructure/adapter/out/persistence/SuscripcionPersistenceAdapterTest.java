package edu.dosw.styleradar.pagos_suscripciones.infrastructure.adapter.out.persistence;

import edu.dosw.styleradar.pagos_suscripciones.domain.model.Suscripcion;
import edu.dosw.styleradar.pagos_suscripciones.domain.model.TipoSuscriptor;
import edu.dosw.styleradar.pagos_suscripciones.infrastructure.adapter.out.persistence.repository.SuscripcionJpaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest(
    excludeAutoConfiguration = {
        FlywayAutoConfiguration.class, // Evita que Flyway corra y falle con JSONB
        RabbitAutoConfiguration.class  // Evita problemas con RabbitMQ
    }
)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY) // Fuerza a usar H2
@ActiveProfiles("test") // <--- ESTO ES CLAVE: Usa tu application-test.yml
@Import(SuscripcionPersistenceAdapter.class)
class SuscripcionPersistenceAdapterTest {

    @Autowired
    private SuscripcionPersistenceAdapter adapter;

    @Autowired
    private SuscripcionJpaRepository repository;

    @Test
    void debeGuardarYBuscarSuscripcion() {
        // Arrange
        Suscripcion suscripcion = new Suscripcion(null, "USER-999", TipoSuscriptor.USUARIO, 1L);
        suscripcion.activar(LocalDate.now().plusMonths(1));
        suscripcion.setPreapprovalIdMp("PRE-999");

        // Act
        Suscripcion guardada = adapter.guardar(suscripcion);
        Optional<Suscripcion> encontrada = adapter.buscarPorSuscriptorId("USER-999");

        // Assert
        assertTrue(encontrada.isPresent());
        assertEquals("USER-999", encontrada.get().getSuscriptorId());
        assertEquals("PRE-999", encontrada.get().getPreapprovalIdMp());
        assertNotNull(guardada.getId());
    }
}