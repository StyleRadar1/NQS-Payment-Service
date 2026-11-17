package edu.dosw.styleradar.pagos.infrastructure.adapter.out.persistence;

import edu.dosw.styleradar.pagos.infrastructure.adapter.out.persistence.repository.PlanJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifica las migraciones Flyway reales contra PostgreSQL 16.
 *
 * <p>Se omite automáticamente si Docker no está disponible; en CI se ejecuta
 * y valida V1–V4, el seed de planes y el mapeo JPA (SR-RNF-14, SR-RF-100).</p>
 */
@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Migraciones Flyway en PostgreSQL 16")
class PostgresMigrationTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void datasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        registry.add("spring.flyway.enabled", () -> "true");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
    }

    @Autowired
    private PlanJpaRepository planRepository;

    @Test
    void aplicaMigracionesYseedDePlanes() {
        assertThat(planRepository.count()).isEqualTo(3);
    }
}
