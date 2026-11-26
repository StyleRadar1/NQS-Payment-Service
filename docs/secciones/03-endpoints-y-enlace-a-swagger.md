[⬅ Volver al README Principal](../../README.md)

---

## 3. ENDPOINTS Y ENLACE A SWAGGER 
La documentación interactiva OpenAPI 3.0 y los contratos de ejecución se encuentran desplegados y clasificados por entorno:

### Matriz Oficial de Enlaces por Entorno (PROD, QA, LOCAL)

| Entorno | Estado | URL base | Swagger UI | Health |
| :--- | :---: | :--- | :--- | :--- |
| **QA** | Activo | `https://ca-styleradar-pagos-juangv1.lemonplant-91cfa5ac.northcentralus.azurecontainerapps.io` | [Swagger QA](https://ca-styleradar-pagos-juangv1.lemonplant-91cfa5ac.northcentralus.azurecontainerapps.io/swagger-ui/index.html) | [`/actuator/health`](https://ca-styleradar-pagos-juangv1.lemonplant-91cfa5ac.northcentralus.azurecontainerapps.io/actuator/health) |
| **PROD** | Pendiente | Pendiente de despliegue | Pendiente | Pendiente |
| **Local** | Local | `http://localhost:8400` | `http://localhost:8400/swagger-ui.html` | `http://localhost:8400/actuator/health` |

* **Especificación OpenAPI (Contrato JSON Schema):** `http://localhost:8400/v3/api-docs` (o `/v3/api-docs` en los dominios de PROD y QA).


<div align="center">
  
<img src="../evidencias/swagger-ui.png" width="90%" alt="Evidencia Swagger UI" />
</div>

### Tabla de Endpoints Expuestos

| Método | Endpoint | Autorización | Éxito | Errores |
| :---: | :--- | :--- | :---: | :--- |
| `POST` | `/api/v1/subscriptions/start` | JWT / `X-User-Id` | `202` | `400`, `403`, `422` |
| `GET` | `/api/v1/subscriptions/{subscriberId}` | Titular o `ADMIN_STYLERADAR` | `200` | `401`, `403`, `404` |
| `POST` | `/api/v1/subscriptions/{subscriberId}/cancel` | Titular | `200` | `400`, `403`, `404`, `422` |
| `GET` | `/api/v1/payments/history` | JWT / `X-User-Id` | `200` | `401` |
| `POST` | `/api/v1/payments/{orderId}/retry` | Titular | `200` | `400`, `403`, `404`, `422` |
| `GET` | `/api/v1/admin/payments` | `ADMIN_STYLERADAR` | `200` | `403` |
| `GET` | `/api/v1/admin/payments/{id}` | `ADMIN_STYLERADAR` | `200` | `403`, `404` |
| `GET` | `/api/v1/plans` | Autenticado | `200` | `401` |
| `POST` | `/api/v1/webhooks/mercadopago` | HMAC `x-signature` | `200` | `400` |
| `GET` | `/api/v1/health` | Público | `200` | — |
| `GET` | `/actuator/health` | Público | `200`/`503` | — |
<details>
<summary><b>Ejemplos de request/response</b></summary>

<br>

#### Ejemplo 1: Iniciar pago de tienda (`POST /api/v1/subscriptions/start`)

```bash
curl -i -X POST "$BASE_URL/api/v1/subscriptions/start" \
  -H "Authorization: Bearer $TOKEN" \
  -H "X-User-Id: 3" \
  -H "Accept: application/json" \
  -H "Content-Type: application/json" \
  -d '{
    "subscriberType": "STORE",
    "payerEmail": "test_user_8378736833666739761@testuser.com",
    "planId": 3,
    "taxId": "900555777"
  }'
```
##### Respuesta Exitosa (`202 Accepted`):

```json
{
  "subscriptionId": 26,
  "orderId": 30,
  "status": "PENDING",
  "initPoint": "https://www.mercadopago.com.co/subscriptions/checkout?preapproval_id=...",
  "message": "Continúa a Mercado Pago para activar tu almacén."
}
```
#### Ejemplo 2: Consultar suscripcion (`GET /api/v1/subscriptions/{subscriberId}`)

```bash
curl -i -X GET "$BASE_URL/api/v1/subscriptions/3" \
  -H "Authorization: Bearer $TOKEN" \
  -H "X-User-Id: 3" \
  -H "Accept: application/json"
```
##### Respuesta (`200 OK`):

```json
{
  "id": 26,
  "subscriberId": "3",
  "subscriberType": "STORE",
  "plan": {
    "id": 3,
    "name": "Store Basic",
    "subscriberType": "STORE",
    "price": {
      "value": 89000.00,
      "currency": "COP"
    },
    "limits": {
      "max_products": 50
    },
    "active": true
  },
  "status": "ACTIVE",
  "startDate": "2026-11-25",
  "endDate": "2026-12-25",
  "autoRenewal": false,
  "access": true,
  "taxId": "900555777",
  "storeId": null
}
```
#### Ejemplo 3: Cancelar renovación (`POST /api/v1/subscriptions/{subscriberId}/cancel`)

```bash
curl -i -X POST "$BASE_URL/api/v1/subscriptions/3/cancel" \
  -H "Authorization: Bearer $TOKEN" \
  -H "X-User-Id: 3" \
  -H "Accept: application/json" \
  -H "Content-Type: application/json" \
  -d '{
    "confirmed": true
  }'
```
##### Respuesta (`200 OK`):

```json
{
  "subscriptionId": 26,
  "autoRenewal": false,
  "status": "ACTIVE",
  "endDate": "2026-12-25",
  "cancellationConfirmed": true,
  "message": "La renovación de tu suscripción fue cancelada. Mantienes el acceso hasta 2026-12-25."
}
```

#### Ejemplo 4: Reintentar pago rechazado (`POST /api/v1/payments/{orderId}/retry`)

```bash
curl -i -X POST "$BASE_URL/api/v1/payments/30/retry" \
  -H "Authorization: Bearer $TOKEN" \
  -H "X-User-Id: 3" \
  -H "Accept: application/json" \
  -H "Content-Type: application/json" \
  -d '{
    "payerEmail": "test_user_8378736833666739761@testuser.com"
  }'
```
##### Respuesta (`200 OK`):

```json
{
  "orderId": 31,
  "status": "PENDING",
  "initPoint": "[https://www.mercadopago.com.co/subscriptions/checkout?preapproval_id=](https://www.mercadopago.com.co/subscriptions/checkout?preapproval_id=)...",
  "message": "Nuevo intento de pago generado. Continúa en Mercado Pago."
}
```

#### Ejemplo 5: Error de negocio en GlobalExceptionHandler (`POST /api/v1/subscriptions/start`)

```bash
curl -i -X POST "$BASE_URL/api/v1/subscriptions/start" \
  -H "Authorization: Bearer $TOKEN" \
  -H "X-User-Id: 3" \
  -H "Accept: application/json" \
  -H "Content-Type: application/json" \
  -d '{
    "planId": 3,
    "subscriberType": "STORE",
    "taxId": "900555777"
  }'
```
##### Respuesta (`409 Conflict`):

```json
{
  "type": "about:blank",
  "title": "Conflict",
  "status": 409,
  "detail": "Tu almacén ya tiene una suscripción activa o un pago pendiente.",
  "instance": "/api/v1/subscriptions/start",
  "code": "SUBSCRIPTION_ALREADY_EXISTS",
  "timestamp": "2026-11-25T12:30:00"
}
```

</details>

---

[⬅ Volver al README Principal](../../README.md)