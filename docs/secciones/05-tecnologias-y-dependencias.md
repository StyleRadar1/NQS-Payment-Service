[⬅ Volver al README Principal](../../README.md)

---

## 5. TECNOLOGÍAS Y DEPENDENCIAS

El stack del microservicio cumple con el estándar unificado de arquitectura de StyleRadar:

| Capa / Componente | Tecnología | Versión | Justificación |
| :--- | :--- | :--- | :--- |
| Lenguaje | Java LTS | `21` | Virtual Threads, `records`, `sealed`, rendimiento I/O |
| Framework | Spring Boot | `3.2.5` | Web, Security, Data JPA, AMQP, Actuator |
| Base de datos | PostgreSQL | `16` | ACID, `JSONB` para límites de plan, índices parciales |
| Migraciones | Flyway | `10.10.0` | Esquema reproducible V1–V5 |
| Broker | RabbitMQ | `3.13` (broker QA) | `styleradar.exchange`, TLS 5671 |
| Caché | Redis | `7` | `SETNX` para idempotencia |
| Cliente HTTP | Spring Cloud OpenFeign | `2023.0.1` | Preapproval, Payments, Refunds |
| Seguridad | Spring Security + JJWT | `0.12.5` | JWT HS256 local; sin llamar a ms-auth |
| Documentación | springdoc OpenAPI | `2.5.0` | Swagger UI, `v3/api-docs` |
| Pruebas | JUnit 5, Mockito, AssertJ | Spring Boot Test | Unitarias, MVC, integración |
| Integración | Testcontainers | `1.19.7` | PostgreSQL real cuando hay Docker |
| Cobertura | JaCoCo | `0.8.11` | Gate ≥ 85% |
| Calidad | SonarQube | Community 26.11 (local) | Quality Gate PASSED, 0 issues |

---

[⬅ Volver al README Principal](../../README.md)