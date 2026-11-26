# Seguridad, Privacidad (Ley 1581) y Análisis OWASP Top 10:2025

**Microservicio:** `NQS-Payment-Service` (`ms-pagos-suscripciones`)  
**Squad:** `NQS` (No Queremos Scrum)  
**Estándar de Referencia:** OWASP Top 10:2025 · Ley 1581 de 2012 (Habeas Data Colombia) · RFC 7807 (Problem Details)

---

## 1. Arquitectura de Seguridad Implementada en el Código

### 1.1 Autenticación Stateless y Validación JWT (`SecurityConfig`, `ServiceAuthenticationFilter`)
* **Política sin estado:** `SessionCreationPolicy.STATELESS`, con `CSRF`, `httpBasic`, `formLogin` y `logout` deshabilitados explícitamente para API REST stateless.
* **Validación criptográfica de JWT:** `ServiceAuthenticationFilter` soporta verificación mediante clave secreta **HS256** (`JWT_SECRET_BASE64`, usada en QA y PROD) o clave pública **RSA X.509** (`JWT_PUBLIC_KEY` en formato PEM).
* **Aislamiento por perfil:** En `qa` y `prod` (`application-qa.yml`, `application-prod.yml`), `styleradar.security.jwt.enabled=true` y `require-token=true`. En `dev` y `test`, la validación JWT se desactiva para pruebas locales con cabeceras de contexto.
* **Verificación cruzada de identidad:** Si el cliente o el API Gateway envía la cabecera `X-User-Id` junto con un JWT, el filtro verifica que coincida exactamente con el claim `sub` del token; de lo contrario, rechaza la solicitud con `401 Unauthorized`.

### 1.2 Control de Acceso Basado en Roles y Titularidad (`SecurityConfig`, `Roles`)
* **Rutas administrativas restringidas:** Todo endpoint bajo `/api/v1/admin/**` exige alguno de los roles `STYLERADAR_ADMIN`, `ADMIN_STYLERADAR` o `ADMIN` (`hasAnyRole`).
* **Rutas públicas acotadas:** Únicamente `/api/v1/webhooks/**`, `/api/v1/health`, `/actuator/health/**`, `/actuator/info`, `/v3/api-docs/**`, `/swagger-ui/**` y `/swagger-ui.html` son accesibles sin JWT. El resto de rutas requiere autenticación.
* **Validación de titularidad (Anti-IDOR):** En `GET /api/v1/subscriptions/{subscriberId}`, `POST /api/v1/subscriptions/{subscriberId}/cancel` y `POST /api/v1/payments/{orderId}/retry`, la capa de aplicación verifica `Roles.isOwner(subscriberId, requesterId)` o rol administrativo (`Roles.isAdmin(role)`), lanzando `OperationNotAllowedException` (`403 Forbidden`) ante accesos ajenos.

### 1.3 Integridad y Autenticidad de Webhooks (`MercadoPagoSignatureValidator`)
* **Validación HMAC-SHA256:** Cada notificación entrante en `POST /api/v1/webhooks/mercadopago` exige la cabecera `x-signature` con parámetros `ts` y `v1`.
* **Manifiesto oficial:** Se reconstruye `id:{dataId};request-id:{xRequestId};ts:{ts};` y se firma con `MP_WEBHOOK_SECRET` usando `HmacSHA256`.
* **Comparación en tiempo constante:** Se utiliza `MessageDigest.isEqual(...)` para prevenir ataques de temporización (*timing attacks*).
* **Firma dummy restringida:** El valor `dummy` solo es aceptado cuando están activos los perfiles `dev` o `test`; en `qa` y `prod` cualquier firma inválida o ausente retorna `401 Unauthorized`.

### 1.4 Doble Idempotencia y Transactional Outbox
* **Primera barrera (Redis):** `RedisIdempotencyAdapter` emplea operaciones atómicas `SETNX` con TTL para evitar el reprocesamiento concurrente de un mismo `external_payment_id` o `x-request-id`.
* **Segunda barrera (PostgreSQL):** Restricciones `UNIQUE` sobre `payment_orders.external_payment_id` y `webhook_logs.external_payment_id`, además del índice único parcial `uq_active_pending_subscription` (`WHERE status IN ('ACTIVE', 'PENDING')`) para impedir suscripciones activas o pendientes duplicadas por suscriptor.
* **Confiabilidad de eventos (Outbox):** `V8__event_outbox.sql` y `OutboxEventPublisherAdapter` persisten los eventos de dominio en `event_outbox` dentro de la misma transacción ACID de base de datos, y `OutboxPublisherScheduler` los publica a RabbitMQ con reintentos exponenciales (hasta 10 intentos).

### 1.5 Manejo Seguro de Errores y Exposición Mínima
* **RFC 7807 (`ProblemDetail`):** `GlobalExceptionHandler` y `SecurityErrorWriter` retornan respuestas `application/problem+json` estructuradas (`type`, `title`, `status`, `detail`, `instance`, `code`, `timestamp`, `errors`).
* **Sin filtración de trazas:** `application.yml` fija `server.error.include-stacktrace: never` y `server.error.include-message: never`, y en `qa`/`prod` `management.endpoint.health.show-details: never`.

### 1.6 Privacidad y Cumplimiento Ley 1581 de 2012 (Habeas Data)
* **Cero almacenamiento de datos financieros sensibles:** El servicio nunca recibe, procesa ni almacena números de tarjeta, CVV ni credenciales bancarias; el flujo de cobro se delega íntegramente al checkout seguro de Mercado Pago (`initPoint` de Preapproval).
* **Minimización de datos en eventos AMQP:** Los eventos publicados (`PaymentConfirmedEvent`, `SubscriptionSuspendedEvent`, `SubscriptionPlanUpdatedEvent`) no incluyen nombres de personas, correos electrónicos ni teléfonos.

### 1.7 Seguridad de Transporte y Contenedores
* **Cifrado en tránsito:** En `qa` y `prod`, RabbitMQ opera sobre AMQPS TLS (`5671`) validado con truststore PKCS12 (`rabbitmq-truststore.jks` y `verify-hostname: true`), Redis con SSL (`6380`) y PostgreSQL con `sslmode=require`.
* **Contenedor endurecido:** El `Dockerfile` ejecuta la aplicación bajo un usuario sin privilegios (`styleradar`, no `root`) sobre `eclipse-temurin:21-jre-alpine`.

---

## 2. Matriz de Mitigación OWASP Top 10:2025

| Categoría OWASP Top 10:2025 | Riesgo en Pasarela de Pagos | Controles Implementados | Evidencia en Código |
| :--- | :--- | :--- | :--- |
| **A01 · Broken Access Control** | Consulta o cancelación de suscripciones/pagos de terceros o acceso a rutas admin. | `SecurityConfig` restringe `/api/v1/admin/**` a roles admin; `Roles.isOwner` valida titularidad; `ServiceAuthenticationFilter` coteja `sub` vs `X-User-Id`. | `SecurityConfig.java`, `ServiceAuthenticationFilter.java`, `Roles.java` |
| **A02 · Cryptographic Failures** | Exposición de secretos, tokens o tráfico sin cifrar entre servicios. | JWT HS256/RSA, HMAC-SHA256 con `MessageDigest.isEqual`, AMQPS TLS `5671` con `verify-hostname: true`, Redis SSL, PostgreSQL SSL, secretos externalizados. | `MercadoPagoSignatureValidator.java`, `application-qa.yml`, `application-prod.yml` |
| **A03 · Injection** | Inyección SQL en consultas de pagos o planes. | Uso exclusivo de Spring Data JPA con consultas parametrizadas y migraciones Flyway tipadas (`V1`–`V9`); validación de entrada con Bean Validation (`@Valid`, `@Pattern`). | `PaymentOrderJpaRepository.java`, `StartSubscriptionRequest.java` |
| **A04 · Insecure Design** | Doble cobro o activación múltiple por webhooks repetidos o concurrencia. | Doble idempotencia (Redis `SETNX` + `UNIQUE` en BD), índice parcial `uq_active_pending_subscription`, patrón Transactional Outbox (`V8`). | `RedisIdempotencyAdapter.java`, `V6__english_schema.sql`, `V8__event_outbox.sql` |
| **A05 · Security Misconfiguration** | Filtración de stacktraces, detalles de Actuator o firma `dummy` en la nube. | `include-stacktrace: never`, `include-message: never`, `health.show-details: never` en QA/PROD, firma `dummy` bloqueada fuera de `dev`/`test`, usuario no-root en Docker. | `application.yml`, `application-qa.yml`, `Dockerfile` |
| **A06 · Vulnerable and Outdated Components** | Dependencias con vulnerabilidades conocidas. | Stack actualizado sobre Spring Boot `3.2.5`, Java `21 LTS`, JJWT `0.12.5`, Flyway `10.10.0`, Testcontainers `1.21.3` y análisis local con SonarQube. | `pom.xml` |
| **A07 · Identification and Authentication Failures** | Suplantación de identidad o uso de tokens expirados/inválidos. | Validación estricta de firma, emisor y expiración con JJWT; `require-token=true` en QA/PROD; rechazo inmediato con `401` vía `SecurityErrorWriter`. | `ServiceAuthenticationFilter.java`, `SecurityErrorWriter.java` |
| **A08 · Software and Data Integrity Failures** | Falsificación de notificaciones de pago o pérdida de eventos de negocio. | Verificación HMAC-SHA256 de `x-signature` en webhooks; persistencia atómica de eventos en `event_outbox` antes de publicar a RabbitMQ. | `WebhookController.java`, `OutboxEventPublisherAdapter.java` |
| **A09 · Security Logging and Monitoring Failures** | Falta de trazabilidad ante intentos de fraude o errores de integración. | Registro auditable de webhooks en `webhook_logs`, bitácora de intentos en `event_outbox` (`attempts`, `last_error`), logging estructurado sin exponer secretos. | `WebhookLogPersistenceAdapter.java`, `OutboxPublisherScheduler.java` |
| **A10 · Server-Side Request Forgery (SSRF)** | Redirección maliciosa hacia servicios internos desde llamadas salientes. | El cliente OpenFeign hacia Mercado Pago apunta exclusivamente a la URL configurada `MP_API_URL` (`https://api.mercadopago.com`) sin aceptar URLs arbitrarias del usuario. | `MercadoPagoFeignClient.java`, `application.yml` |

