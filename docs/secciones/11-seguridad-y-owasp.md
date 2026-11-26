[⬅ Volver al README Principal](../../README.md)

---

## 11. SEGURIDAD, HABEAS DATA Y ANÁLISIS OWASP TOP 10:2025

El microservicio `NQS-Payment-Service` (`ms-pagos-suscripciones`) implementa las directrices oficiales de seguridad de StyleRadar para pasarelas de pago, minimización de datos personales (Ley 1581 de 2012 / Habeas Data) y mitigación de vulnerabilidades OWASP Top 10:2025.

* **Documento Técnico Oficial:** [`../security/SR_NQS_Seguridad_y_OWASP_2025.md`](../security/SR_NQS_Seguridad_y_OWASP_2025.md)

### Puntos de Control y Cumplimiento Normativo

1. **Autenticación JWT Stateless (`ServiceAuthenticationFilter`):**
   * Validación criptográfica de tokens Bearer (`HS256` vía `JWT_SECRET_BASE64` en QA/PROD, con soporte opcional para clave pública RSA `JWT_PUBLIC_KEY`).
   * Activa y obligatoria en `qa` y `prod` (`enabled=true`, `require-token=true`); desactivada en `dev` y `test` para desarrollo local.
   * Verificación cruzada entre el claim `sub` del JWT y la cabecera `X-User-Id` para impedir suplantación de identidad.

2. **Control de Acceso Basado en Roles y Titularidad (A01 - Broken Access Control):**
   * Todas las rutas administrativas (`/api/v1/admin/**`) están restringidas en `SecurityConfig` a los roles `STYLERADAR_ADMIN`, `ADMIN_STYLERADAR` y `ADMIN`.
   * Las operaciones sobre suscripciones y reintentos de pago verifican titularidad (`Roles.isOwner`) o rol administrativo, respondiendo `403 Forbidden` ante accesos de terceros.

3. **Integridad y Autenticidad de Webhooks Mercado Pago (A08):**
   * `MercadoPagoSignatureValidator` verifica la firma HMAC-SHA256 de la cabecera `x-signature` (`ts`, `v1`) contra el manifiesto `id:{dataId};request-id:{xRequestId};ts:{ts};` usando comparación en tiempo constante (`MessageDigest.isEqual`).
   * La firma `dummy` está bloqueada fuera de los perfiles `dev` y `test`; en `qa` y `prod` cualquier firma inválida es rechazada con `401 Unauthorized`.

4. **Doble Idempotencia y Transactional Outbox (A04):**
   * Primera barrera en memoria con Redis `SETNX` (`RedisIdempotencyAdapter`) y segunda barrera en PostgreSQL mediante restricciones `UNIQUE` sobre `external_payment_id` e índice parcial `uq_active_pending_subscription`.
   * Publicación de eventos respaldada por `event_outbox` (`V8`) y `OutboxPublisherScheduler`, evitando inconsistencias entre la base de datos y RabbitMQ.

5. **Manejo Seguro de Errores (RFC 7807) y Minimización de Datos (Ley 1581):**
   * Respuestas de error normalizadas en `ProblemDetail` (`GlobalExceptionHandler` y `SecurityErrorWriter`), con `include-stacktrace: never`, `include-message: never` y `health.show-details: never` en QA/PROD.
   * El servicio no almacena datos de tarjetas ni CVV (delegado al checkout de Mercado Pago) y omite PII (nombres, correos, teléfonos) en los eventos AMQP.

6. **Seguridad de Transporte y Contenedor Endurecido (A02 / A05):**
   * RabbitMQ opera sobre AMQPS TLS (`5671`) con truststore PKCS12 y `verify-hostname: true`, Redis con SSL (`6380`) y PostgreSQL con `sslmode=require` en la nube.
   * El `Dockerfile` ejecuta el servicio bajo un usuario sin privilegios (`styleradar`).

---

[⬅ Volver al README Principal](../../README.md)
