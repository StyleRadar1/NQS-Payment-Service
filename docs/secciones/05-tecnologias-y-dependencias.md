[⬅ Volver al README Principal](../../README.md)

---

## 5. TECNOLOGÍAS Y DEPENDENCIAS

El stack tecnológico de `NQS-Payment-Service` (`ms-pagos-suscripciones`) cumple con el estándar unificado de DOSW 2026:

| Capa / Componente | Tecnología | Versión | Justificación Técnica |
| :-------------------------------- | :------------------------- | :--------- | :-------------------------------------------------------------------------------------------------- |
| **Lenguaje Core** | Java LTS | `21` | Proporciona Virtual Threads, `records` inmutables y Pattern Matching para alto rendimiento I/O. |
| **Framework Base** | Spring Boot | `3.2.5` | Ecosistema empresarial con soporte nativo para Web, Security, Data JPA, AMQP y Actuator. |
| **Base de Datos** | PostgreSQL | `16` | Garantiza integridad transaccional ACID, soporte `JSONB` para límites de plan e índices parciales. |
| **Control de Esquema** | Flyway | `10.10.0` | Gestiona migraciones versionadas reproducibles de base de datos (`V1`–`V9`). |
| **Caché & Idempotencia** | Redis | `7` | Provee operaciones atómicas `SETNX` en memoria para evitar reprocesamiento de webhooks. |
| **Broker de Eventos** | RabbitMQ | `3.13` | Implementa mensajería AMQP 0-9-1 desacoplada mediante Topic Exchange `styleradar.exchange` y TLS `5671`. |
| **Cliente HTTP Pasarela** | Spring Cloud OpenFeign | `2023.0.1` | Cliente declarativo para integración con las APIs de preaprobación y pagos de Mercado Pago. |
| **Seguridad** | Spring Security + JJWT | `0.12.5` | Validación stateless de tokens JWT (HS256 / RSA) y control de acceso por roles sin depender de `ms-auth`. |
| **Observabilidad** | Spring Boot Actuator | `3.2.5` | Expone sondas de salud (`/actuator/health`) e información operativa para Docker y Azure Container Apps. |
| **Documentación API** | SpringDoc OpenAPI | `2.5.0` | Generación automática de especificación OpenAPI 3.0 y Swagger UI con autenticación JWT Bearer. |
| **Productividad** | Lombok | `1.18.x` | Reduce código repetitivo en adaptadores, DTOs y entidades JPA mediante anotaciones en compilación. |
| **Pruebas Unitarias** | JUnit 5 + Mockito + AssertJ | `5.10.x` | Verificación aislada del dominio, casos de uso y validadores sin levantar el contexto completo. |
| **Pruebas de Integración** | Spring Boot Test + MockMvc | `3.2.5` | Pruebas de controladores REST, contratos RFC 7807 y flujos de seguridad JWT. |
| **Base en Memoria (`test`)** | H2 Database | `2.2.x` | Permite ejecutar pruebas rápidas y perfil `test` en modo compatibilidad PostgreSQL sin Docker. |
| **Testing de Contenedores** | Testcontainers | `1.21.3` | Levanta instancias reales de PostgreSQL en contenedores efímeros para validar migraciones Flyway. |
| **Métricas de Cobertura** | JaCoCo Maven Plugin | `0.8.11` | Verifica automáticamente una cobertura mínima de instrucciones (`INSTRUCTION`) ≥ 85% en fase `verify`. |
| **Calidad Estática** | SonarQube (`sonar-maven-plugin`) | `5.7.0.6970` | Configurado en `pom.xml` para análisis estático local (`mvn sonar:sonar`; aún no integrado en `ci.yml`). |

---

[⬅ Volver al README Principal](../../README.md)
