[⬅ Volver al README Principal](../../README.md)

---

## 7. VARIABLES DE ENTORNO

Todas las configuraciones sensibles y dependientes de entorno se externalizan mediante variables de sistema conformes al principio III de **Twelve-Factor App**.

> [!IMPORTANT]
> **Configuración de Conexión según el Entorno donde corre Spring Boot:**
> * **Si Spring Boot se ejecuta desde el equipo con `mvn` (Modo Host Local):**
>   - **PostgreSQL (`styleradar_payments`):** `localhost:5432` (`DB_HOST=localhost`, `DB_PORT=5432`).
>   - **Redis:** `localhost:6379` (`REDIS_HOST=localhost`, `REDIS_PORT=6379`).
>   - **RabbitMQ:** `localhost:5672` (`RABBITMQ_HOST=localhost`, `RABBITMQ_PORT=5672`).
> * **Si Spring Boot se ejecuta dentro de Docker Compose (Modo Contenerizado):**
>   - **PostgreSQL:** `postgres:5432` (`SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/styleradar_payments`).
>   - **Redis:** `redis:6379` (`SPRING_DATA_REDIS_HOST=redis`, `SPRING_DATA_REDIS_PORT=6379`).
>   - **RabbitMQ:** `rabbitmq:5672` (`RABBITMQ_HOST=rabbitmq`, `RABBITMQ_PORT=5672`).

| Variable | Descripción | Valor Local con `mvn` | Valor en Docker Compose | Requerido en QA / Prod |
| :--- | :--- | :--- | :--- | :---: |
| `SERVER_PORT` | Puerto HTTP del microservicio | `8400` | `8400` | Sí |
| `SPRING_PROFILES_ACTIVE` | Perfil activo de Spring Boot (`dev`, `test`, `qa`, `prod`) | `dev` | `dev` | Sí |
| `DB_HOST` | Hostname de PostgreSQL en perfil `dev` | `localhost` | *(Usa `SPRING_DATASOURCE_URL`)* | No |
| `DB_PORT` | Puerto de PostgreSQL en perfil `dev` | `5432` | *(Usa `SPRING_DATASOURCE_URL`)* | No |
| `DB_NAME` | Nombre de la base de datos en perfil `dev` | `styleradar_payments` | *(Usa `SPRING_DATASOURCE_URL`)* | No |
| `DB_USER` | Usuario de PostgreSQL en perfil `dev` | `sr_user` | *(Usa `SPRING_DATASOURCE_USERNAME`)* | No |
| `DB_PASSWORD` | Contraseña de PostgreSQL en perfil `dev` | `sr_password` | *(Usa `SPRING_DATASOURCE_PASSWORD`)* | No |
| `SPRING_DATASOURCE_URL` | URL JDBC canónica de PostgreSQL | `jdbc:postgresql://localhost:5432/styleradar_payments` | `jdbc:postgresql://postgres:5432/styleradar_payments` | Sí |
| `SPRING_DATASOURCE_USERNAME` | Usuario JDBC canónico de PostgreSQL | `sr_user` | `sr_user` | Sí |
| `SPRING_DATASOURCE_PASSWORD` | Contraseña JDBC canónica de PostgreSQL | `sr_password` | `sr_password` | Sí |
| `DB_POOL_SIZE` | Tamaño máximo del pool HikariCP | `5` | `5` | No |
| `REDIS_HOST` | Hostname de Redis en perfil `dev` | `localhost` | *(Usa `SPRING_DATA_REDIS_HOST`)* | No |
| `REDIS_PORT` | Puerto de Redis en perfil `dev` | `6379` | *(Usa `SPRING_DATA_REDIS_PORT`)* | No |
| `SPRING_DATA_REDIS_HOST` | Hostname canónico de Redis | `localhost` | `redis` | Sí |
| `SPRING_DATA_REDIS_PORT` | Puerto canónico de Redis (`6380` SSL en nube) | `6379` | `6379` | Sí |
| `SPRING_DATA_REDIS_PASSWORD` | Contraseña / Access Key de Redis | *(Vacío en local)* | *(Vacío en local)* | Sí |
| `SPRING_DATA_REDIS_SSL_ENABLED` | Habilita TLS/SSL hacia Redis | `false` | `false` | Sí |
| `SPRING_DATA_REDIS_DATABASE` | Base lógica Redis (`0` QA / `1` PROD) | `0` | `0` | No |
| `RABBIT_ENABLED` | Habilita publicación Outbox/RabbitMQ | `true` | `true` | No |
| `RABBIT_EXCHANGE` | Topic Exchange de eventos | `styleradar.exchange` | `styleradar.exchange` | No |
| `RABBITMQ_HOST` | Hostname del broker RabbitMQ | `localhost` | `rabbitmq` | Sí |
| `RABBITMQ_PORT` | Puerto AMQP (`5672` local / `5671` TLS en QA/PROD) | `5672` | `5672` | Sí |
| `RABBITMQ_USER` | Usuario de conexión RabbitMQ | `guest` | `guest` | Sí |
| `RABBITMQ_PASSWORD` | Contraseña de conexión RabbitMQ | `guest` | `guest` | Sí |
| `RABBITMQ_VHOST` | Virtual host de RabbitMQ | `/` | `/` | No |
| `RABBITMQ_SSL_ENABLED` | Habilita AMQPS TLS hacia RabbitMQ | `false` | `false` | Sí |
| `RABBITMQ_TRUST_STORE` | Ruta al truststore PKCS12 (`file:/app/certs/rabbitmq-truststore.jks`) | *(No requerido en `dev`)* | *(No requerido en `dev`)* | Sí (QA/PROD) |
| `RABBITMQ_TRUSTSTORE_PASSWORD` | Contraseña del truststore JKS/PKCS12 | `changeit` | `changeit` | Sí (QA/PROD) |
| `RABBITMQ_TRUST_STORE_TYPE` | Tipo de almacén de certificados | `PKCS12` | `PKCS12` | No |
| `JWT_VALIDATION_ENABLED` | Activa validación JWT en el filtro | `false` | `false` | Sí (`true`) |
| `JWT_REQUIRE_TOKEN` | Exige token Bearer obligatorio | `false` | `false` | Sí (`true`) |
| `JWT_SECRET_BASE64` | Secreto HMAC-SHA256 en Base64 compartido con `ms-auth` | *(Vacío en `dev`)* | *(Vacío en `dev`)* | Sí |
| `JWT_PUBLIC_KEY` | Clave pública RSA PEM (alternativa a `JWT_SECRET_BASE64`) | *(Vacío)* | *(Vacío)* | No |
| `JWT_ISSUER` | Emisor esperado del token JWT | `styleradar` | `styleradar` | No |
| `MP_ACCESS_TOKEN` | Access Token de Mercado Pago | `TEST-MOCK` | `TEST-MOCK` | Sí |
| `MP_PUBLIC_KEY` | Public Key de Mercado Pago | `TEST-MOCK` | `TEST-MOCK` | Sí |
| `MP_WEBHOOK_SECRET` | Secreto HMAC-SHA256 para validar webhooks | `dev-webhook-secret` | `dev-webhook-secret` | Sí |
| `MP_WEBHOOK_URL` | URL pública de recepción de webhooks | `http://localhost:8400/api/v1/webhooks/mercadopago` | `http://localhost:8400/api/v1/webhooks/mercadopago` | Sí |
| `MP_BACK_URL` | URL de retorno del checkout al frontend | `http://localhost:4200/subscriptions` | `http://localhost:4200/subscriptions` | Sí |
| `MP_API_URL` | URL base de la API de Mercado Pago | `https://api.mercadopago.com` | `https://api.mercadopago.com` | No |
| `SCHEDULING_ENABLED` | Habilita schedulers (`00:10`, `03:00` y Outbox `5s`) | `true` | `true` | No |
| `HEALTH_SHOW_DETAILS` | Detalle de `/actuator/health` (`never` en QA/PROD) | `always` | `always` | No |
| `LOG_LEVEL` | Nivel de logging del paquete `payments` | `DEBUG` | `DEBUG` | No |

<details>
<summary><b>Click para ver la Plantilla de Archivo .env.example Completa</b></summary>

<br>

```dotenv
# ==============================================================================
# STYLERADAR · VARIABLES DE ENTORNO LOCALES (.env)
# Microservicio: NQS-Payment-Service (ms-pagos-suscripciones) · Puerto 8400
# ==============================================================================
SERVER_PORT=8400
SPRING_PROFILES_ACTIVE=dev

# ------------------------------------------------------------------------------
# Persistencia PostgreSQL (styleradar_payments)
# Si ejecutas con mvn (host) -> DB_HOST=localhost, DB_PORT=5432
# Si ejecutas con Docker Compose -> SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/styleradar_payments
# ------------------------------------------------------------------------------
DB_HOST=localhost
DB_PORT=5432
DB_NAME=styleradar_payments
DB_USER=sr_user
DB_PASSWORD=sr_password
DB_POOL_SIZE=5
# SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/styleradar_payments
# SPRING_DATASOURCE_USERNAME=sr_user
# SPRING_DATASOURCE_PASSWORD=sr_password

# ------------------------------------------------------------------------------
# Caché en Memoria Redis (idempotencia de webhooks)
# Si ejecutas con mvn (host) -> REDIS_HOST=localhost, REDIS_PORT=6379
# Si ejecutas con Docker Compose -> SPRING_DATA_REDIS_HOST=redis, SPRING_DATA_REDIS_PORT=6379
# ------------------------------------------------------------------------------
REDIS_HOST=localhost
REDIS_PORT=6379
# SPRING_DATA_REDIS_HOST=redis
# SPRING_DATA_REDIS_PORT=6379
# SPRING_DATA_REDIS_PASSWORD=
# SPRING_DATA_REDIS_SSL_ENABLED=false
# SPRING_DATA_REDIS_DATABASE=0

# ------------------------------------------------------------------------------
# Broker de Eventos RabbitMQ
# Si ejecutas con mvn (host) -> RABBITMQ_HOST=localhost, RABBITMQ_PORT=5672
# Si ejecutas con Docker Compose -> RABBITMQ_HOST=rabbitmq, RABBITMQ_PORT=5672
# En QA/PROD -> RABBITMQ_PORT=5671, RABBITMQ_SSL_ENABLED=true
# ------------------------------------------------------------------------------
RABBIT_ENABLED=true
RABBIT_EXCHANGE=styleradar.exchange
RABBITMQ_HOST=localhost
RABBITMQ_PORT=5672
RABBITMQ_USER=guest
RABBITMQ_PASSWORD=guest
RABBITMQ_VHOST=/
RABBITMQ_SSL_ENABLED=false
# RABBITMQ_TRUST_STORE=file:/app/certs/rabbitmq-truststore.jks
# RABBITMQ_TRUSTSTORE_PASSWORD=changeit
# RABBITMQ_TRUST_STORE_TYPE=PKCS12

# ------------------------------------------------------------------------------
# Pasarela Mercado Pago
# En dev el adaptador mock permite usar TEST-MOCK y firma dummy
# ------------------------------------------------------------------------------
MP_ACCESS_TOKEN=TEST-MOCK
MP_PUBLIC_KEY=TEST-MOCK
MP_WEBHOOK_SECRET=dev-webhook-secret
MP_WEBHOOK_URL=http://localhost:8400/api/v1/webhooks/mercadopago
MP_BACK_URL=http://localhost:4200/subscriptions
MP_API_URL=https://api.mercadopago.com

# ------------------------------------------------------------------------------
# Seguridad JWT
# En dev/test JWT_VALIDATION_ENABLED=false; en QA/PROD true con JWT_SECRET_BASE64 (HS256)
# ------------------------------------------------------------------------------
JWT_VALIDATION_ENABLED=false
JWT_REQUIRE_TOKEN=false
JWT_SECRET_BASE64=
JWT_PUBLIC_KEY=
JWT_ISSUER=styleradar

# ------------------------------------------------------------------------------
# Tareas Programadas y Observabilidad
# ------------------------------------------------------------------------------
SCHEDULING_ENABLED=true
HEALTH_SHOW_DETAILS=always
LOG_LEVEL=DEBUG
```

</details>

---

[⬅ Volver al README Principal](../../README.md)
