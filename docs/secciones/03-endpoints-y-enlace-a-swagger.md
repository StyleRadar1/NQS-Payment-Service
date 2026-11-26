[⬅ Volver al README Principal](../../README.md)

---

## 3. ENDPOINTS Y ENLACE A SWAGGER

La documentación interactiva OpenAPI 3.0 y los contratos de ejecución se encuentran disponibles en los entornos del proyecto:

### Matriz Oficial de Enlaces por Entorno (PROD, QA, LOCAL)

| Entorno | Estado | Propósito | URL Base del Servicio | Swagger OpenAPI UI | Health Check |
| :--- | :---: | :--- | :--- | :--- | :--- |
| **Desarrollo Local (DEV)** | Active | Entorno local de desarrollo y Docker Compose (`styleradar_payments`) | `http://localhost:8400` | [Swagger UI Local](http://localhost:8400/swagger-ui.html) | [`/actuator/health`](http://localhost:8400/actuator/health) |
| **Calidad / Pruebas (QA)** | Active | Pruebas integradas y QA en Azure Container Apps | `https://ca-styleradar-pagos-juangv1.lemonplant-91cfa5ac.northcentralus.azurecontainerapps.io` | [`/swagger-ui/index.html`](https://ca-styleradar-pagos-juangv1.lemonplant-91cfa5ac.northcentralus.azurecontainerapps.io/swagger-ui/index.html) | [`/actuator/health`](https://ca-styleradar-pagos-juangv1.lemonplant-91cfa5ac.northcentralus.azurecontainerapps.io/actuator/health) |
| **Producción (PROD)** | Configured | Despliegue en Azure Container Apps PROD (`deploy-prod` sobre `master`; URL por confirmar) | *(Pendiente de confirmar dominio PROD)* | *(Pendiente de confirmar)* | *(Pendiente de confirmar)* |

* **Especificación OpenAPI (JSON Contrato):** `http://localhost:8400/v3/api-docs`

<div align="center">
  <img src="../images/swagger-ui.png" alt="Evidencia 01 - Swagger UI OpenAPI Activa" width="100%" style="border-radius: 8px; box-shadow: 0 4px 12px rgba(0,0,0,0.15);" />
  <p><sub><b>Figura 3.1:</b> Documentación interactiva Swagger OpenAPI UI activa evidenciando los contratos REST de suscripciones, pagos, planes, webhooks y administración.</sub></p>
</div>

### Tabla de Endpoints Expuestos

| Método | Endpoint | Descripción | Rol Requerido | Códigos HTTP |
| :---: | :--- | :--- | :--- | :---: |
| `POST` | `/api/v1/subscriptions/start` | Inicia una suscripción (`USER` o `STORE`) y retorna el enlace de pago en Mercado Pago (`SR-RF-67`, `SR-RF-93`, `SR-RF-100`) | Autenticado (`JWT` / `X-User-Id`) | `202`, `400`, `401`, `409`, `503` |
| `GET` | `/api/v1/subscriptions/stores/up-to-date` | NIT de las tiendas con suscripción `ACTIVE` y vigente. La usa `ms-geolocalizacion` para decidir qué tiendas muestra el mapa | Público (solo devuelve NIT) | `200` |
| `GET` | `/api/v1/subscriptions/{subscriberId}` | Consulta el estado, vigencia y plan de una suscripción (`SR-RF-102`) | Titular o `STYLERADAR_ADMIN` / `ADMIN_STYLERADAR` / `ADMIN` | `200`, `401`, `403`, `404` |
| `POST` | `/api/v1/subscriptions/{subscriberId}/cancel` | Cancela la renovación automática manteniendo el periodo pagado (`SR-RF-68`, `SR-RF-101`) | Titular o `STYLERADAR_ADMIN` / `ADMIN_STYLERADAR` / `ADMIN` | `200`, `400`, `401`, `403`, `404`, `503` |
| `GET` | `/api/v1/payments/history` | Consulta el historial de pagos del usuario autenticado (`SR-RF-97`) | Autenticado (`JWT` / `X-User-Id`) | `200`, `401` |
| `POST` | `/api/v1/payments/{orderId}/retry` | Crea un nuevo intento de pago para una orden rechazada (`SR-RF-96`) | Titular o `STYLERADAR_ADMIN` / `ADMIN_STYLERADAR` / `ADMIN` | `200`, `400`, `401`, `403`, `404`, `503` |
| `GET` | `/api/v1/admin/payments` | Lista todos los pagos con filtro opcional `?status=` (`PENDING`, `APPROVED`, `REJECTED`) (`SR-RF-75`) | `STYLERADAR_ADMIN`, `ADMIN_STYLERADAR`, `ADMIN` | `200`, `401`, `403` |
| `GET` | `/api/v1/admin/payments/{id}` | Consulta el detalle de un pago específico por ID (`SR-RF-75`) | `STYLERADAR_ADMIN`, `ADMIN_STYLERADAR`, `ADMIN` | `200`, `401`, `403`, `404` |
| `GET` | `/api/v1/plans` | Lista los planes activos con su precio y límites (`SR-RF-102`) | Autenticado (`JWT` / `X-User-Id`) | `200`, `401` |
| `POST` | `/api/v1/webhooks/mercadopago` | Recibe y procesa notificaciones firmadas de Mercado Pago (`SR-RF-94`, `SR-RF-95`) | Firma HMAC-SHA256 `x-signature` | `200`, `400`, `401` |
| `GET` | `/api/v1/health` | Verifica el estado operativo básico del microservicio | Público | `200` |
| `GET` | `/actuator/health` | Health Check de Spring Boot Actuator (BD, Redis y RabbitMQ) | Público | `200`, `503` |

<details>
<summary><b>Click para ver Ejemplos Interactivos de Request y Response JSON (Payloads)</b></summary>

<br>

#### Ejemplo 1: Iniciar Suscripción de Usuario (`POST /api/v1/subscriptions/start`)

```bash
curl -X POST "http://localhost:8400/api/v1/subscriptions/start" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <JWT_TOKEN>" \
  -H "X-User-Id: 42" \
  -d '{
    "subscriberType": "USER",
    "payerEmail": "test_user_117646358391238183361@testuser.com",
    "planId": 2
  }'
```

##### Respuesta Exitosa (`202 Accepted`):

```json
{
  "subscriptionId": 100,
  "orderId": 10,
  "status": "PENDING",
  "initPoint": "https://www.mercadopago.com.co/subscriptions/checkout?preapproval_id=...",
  "message": "Continúa a Mercado Pago para activar Radar+."
}
```

#### Ejemplo 2: Iniciar Suscripción de Almacén (`POST /api/v1/subscriptions/start`)

```bash
curl -X POST "http://localhost:8400/api/v1/subscriptions/start" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <JWT_TOKEN>" \
  -H "X-User-Id: store-admin-17" \
  -d '{
    "subscriberType": "STORE",
    "payerEmail": "test_user_store@testuser.com",
    "taxId": "900123456-7",
    "storeId": "a1111111-1111-1111-1111-111111111111"
  }'
```

> [!NOTE]
> Para `subscriberType = "STORE"`, **`taxId`** (NIT de 9 dígitos, con o sin dígito de verificación) es obligatorio y actúa como identificador canónico de la tienda. **`storeId`** es opcional y se conserva como dato legado (UUID) para compatibilidad con integraciones anteriores.

#### Ejemplo 3: Respuesta de Error Estándar RFC 7807 (`ProblemDetail`)

```json
{
  "type": "about:blank",
  "title": "Bad Request",
  "status": 400,
  "detail": "La solicitud contiene datos inválidos",
  "instance": "/api/v1/subscriptions/start",
  "code": "VALIDATION_ERROR",
  "timestamp": "2026-11-25T23:15:30.123456",
  "errors": {
    "payerEmail": "Ingresa un correo electrónico válido para realizar el pago."
  }
}
```

</details>

---

[⬅ Volver al README Principal](../../README.md)
