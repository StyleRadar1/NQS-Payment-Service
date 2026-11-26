[⬅ Volver al README Principal](../../README.md)

---

## 7. VARIABLES DE ENTORNO

Todas las configuraciones sensibles y dependientes de entorno se externalizan mediante variables de sistema conformes al estándar **Twelve-Factor App**.

| Variable | Descripción | Ejemplo / default |
| :--- | :--- | :--- |
| `SERVER_PORT` | Puerto HTTP | `8400` |
| `SPRING_PROFILES_ACTIVE` | Perfil | `dev`, `qa`, `prod` |
| `SPRING_DATASOURCE_URL` | JDBC PostgreSQL | `jdbc:postgresql://host:5432/styleradar_pagos?sslmode=require` |
| `SPRING_DATASOURCE_USERNAME` | Usuario DB | `styleradaradmin` |
| `SPRING_DATASOURCE_PASSWORD` | Password DB | secreto |
| `SPRING_DATA_REDIS_HOST` | Host Redis | `redis-...redis.cache.windows.net` |
| `SPRING_DATA_REDIS_PORT` | Puerto Redis | `6380` |
| `SPRING_DATA_REDIS_PASSWORD` | Key Redis | secreto |
| `SPRING_DATA_REDIS_SSL_ENABLED` | TLS Redis | `true` |
| `SPRING_DATA_REDIS_DATABASE` | DB lógica Redis | `0` QA / `1` PROD |
| `RABBITMQ_HOST` | Host Rabbit | `styleradar-rabbitmq-qa...` |
| `RABBITMQ_PORT` | Puerto AMQPS | `5671` |
| `RABBITMQ_USER` | Usuario | `ms_pagos` |
| `RABBITMQ_PASSWORD` | Password | secreto |
| `RABBITMQ_VHOST` | Vhost | `/` |
| `RABBITMQ_SSL_ENABLED` | TLS | `true` |
| `RABBITMQ_TRUST_STORE` | JKS | `file:/app/certs/rabbitmq-truststore.jks` |
| `RABBITMQ_TRUSTSTORE_PASSWORD` | Password JKS | `changeit` |
| `RABBITMQ_TRUST_STORE_TYPE` | Tipo | `PKCS12` |
| `SPRING_RABBITMQ_SSL_ENABLED` | Propiedad canónica Spring | `true` |
| `SPRING_RABBITMQ_SSL_TRUST_STORE` | Truststore canónico | `file:/app/certs/rabbitmq-truststore.jks` |
| `SPRING_RABBITMQ_SSL_VERIFY_HOSTNAME` | Verificación hostname | `false` QA / `true` PROD |
| `JWT_VALIDATION_ENABLED` | Validar JWT | `true` QA/PROD |
| `JWT_REQUIRE_TOKEN` | Exigir token | `true` QA/PROD |
| `JWT_SECRET_BASE64` | Secreto HS256 | secreto de ms-auth |
| `MP_ACCESS_TOKEN` | Token MP | secreto backend |
| `MP_PUBLIC_KEY` | Public key MP | frontend |
| `MP_WEBHOOK_SECRET` | Secreto HMAC | secreto backend |
| `MP_WEBHOOK_URL` | URL webhook | `https://.../api/v1/webhooks/mercadopago` |
| `MP_BACK_URL` | Retorno frontend | `https://frontend/suscripciones` |

<details>
<summary><b>Click para copiar la Plantilla de Archivo .env.example</b></summary>

<br>

```dotenv
# ==============================================================================
# STYLERADAR · VARIABLES DE ENTORNO LOCALES (.env)
# ==============================================================================
SERVER_PORT=8400
SPRING_PROFILES_ACTIVE=dev

# Persistencia PostgreSQL
DB_HOST=localhost
DB_PORT=5432
DB_NAME=styleradar_payments
DB_USER=styleradar_user
DB_PASSWORD=styleradar_secret

# Event Broker RabbitMQ
RABBITMQ_HOST=localhost
RABBITMQ_PORT=5672
RABBITMQ_USERNAME=guest
RABBITMQ_PASSWORD=guest

# Caché Redis (idempotencia de webhooks)
REDIS_HOST=localhost
REDIS_PORT=6379
 
# Mercado Pago
MP_ACCESS_TOKEN=<MP_SANDBOX_TOKEN>
MP_WEBHOOK_SECRET=<MP_WEBHOOK_SECRET>
 
# Seguridad JWT
JWT_PUBLIC_KEY=classpath:keys/public-key.pem
```

</details>

---

[⬅ Volver al README Principal](../../README.md)
