# Certificados TLS para RabbitMQ (`certs/`)

El `Dockerfile` copia el contenido de esta carpeta al contenedor:

```dockerfile
COPY certs/ /app/certs/
```

Por seguridad y buenas prácticas, los archivos `.jks`, `.p12`, `.pem` y `.key` están excluidos del control de versiones en `.gitignore`, manteniendo únicamente este `README.md` (`!certs/README.md`) para que el directorio exista en el repositorio y `docker compose --profile app up --build` no falle al construir la imagen en local.

## Cómo funciona en cada entorno

1. **Desarrollo local (`dev`):**
   - `RABBITMQ_SSL_ENABLED=false` y RabbitMQ corre en el puerto `5672` sin TLS, por lo que no se requiere `rabbitmq-truststore.jks` dentro de `certs/`.
2. **CI/CD (`.github/workflows/ci.yml`):**
   - El job `build-and-push` decodifica el secreto `RABBITMQ_TRUSTSTORE_BASE64` y genera `certs/rabbitmq-truststore.jks` antes de construir la imagen Docker.
3. **Pruebas locales contra RabbitMQ con TLS (AMQPS `5671`):**
   - Si necesitas conectarte desde local al broker QA/PROD, solicita el archivo `rabbitmq-truststore.jks` del equipo o impórtalo con `keytool` en `certs/rabbitmq-truststore.jks` (no será versionado por Git).

