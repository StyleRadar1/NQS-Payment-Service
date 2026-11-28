# Contratos de Eventos RabbitMQ — `ms-estadisticas` (`:8420`)

> **Audiencia:** Equipos de los microservicios productores de eventos y equipo transversal de seguridad (`ms-auth` / `api-gateway`).  
> **Objetivo:** Definir exactamente qué eventos, routing keys y campos JSON necesita `ms-estadisticas` para alimentar los **9 indicadores** de la plataforma.

---

## 1. Convenciones Generales de Integración

- **Broker:** RabbitMQ (`5672` local / `5671` AMQPS TLS en QA y PROD).
- **Exchange:** `styleradar.exchange` (tipo `topic`, `durable = true`).
- **Cola consumidora:** `styleradar.estadisticas.eventos-dominio.q` (con DLQ `styleradar.estadisticas.dlq`).
- **Formato:** JSON UTF-8 (`content_type: application/json`).
- **Idempotencia obligatoria:** Todo mensaje debe incluir un **`eventId`** (o `eventoId`) único (UUID). Mensajes repetidos con el mismo `eventId` son descartados sin error.
- **Fecha del hecho:** Incluir **`occurredAt`** (también se aceptan `timestamp`, `fechaEvento`, `fechaCreacion`, `occurredOn`) en formato **ISO-8601 UTC** (ej. `2026-11-24T15:00:00Z`). `ms-estadisticas` convierte la fecha a zona `America/Bogota` para agrupar días y meses.
- **Privacidad:** Nunca incluir nombres de personas, correos, documentos ni teléfonos en los payloads.
- **Bindings exactos:** `ms-estadisticas` ya **no** usa bindings comodín (`#`); solo escucha las **7 routing keys** descritas en la sección 3.

---

## 2. Resumen por Microservicio

| Microservicio | Puerto | ¿Debe enviar eventos? | Routing Key(s) requerida(s) | Indicadores que alimenta |
|---|---:|---|---|---|
| `ms-segunda-vida-donaciones` | `:8520` | **Sí** | `styleradar.donaciones.completada` | Prendas recibidas por fundación + Donaciones del mes |
| `ms-item-catalogo` | `:8220` | **Sí** | `styleradar.catalogo.prenda.publicada` | Total prendas publicadas |
| `ms-playlist` | `:8260` | **Sí** | `styleradar.playlist.creada` | Total playlist creadas |
| `ms-usuarios` | `:8120` | **Sí** | `styleradar.usuarios.usuario.registrado` | Registros diarios de usuarios |
| `ms-inventario` | `:8200` | **Sí** | `styleradar.inventario.actualizado` | Inventario por estado |
| `ms-pagos-suscripciones` | `:8400` | **Sí** | `styleradar.pagos.pago.confirmado`<br>`styleradar.pagos.suscripcion.plan.actualizado` | Histórico mensual de ingresos + Cuentas de compradores y almacenes por plan |
| `ms-chat` | `:8320` | **No** *(liberado)* | — | — |
| `ms-notificaciones` | `:8300` | **No** *(liberado)* | — | — |
| `ms-tiendas` | `:8140` | **No** *(liberado)* | — | — |
| `ms-probador-virtual` | — | **No** *(fuera de alcance)* | — | — |

---

## 3. Contratos Detallados por Microservicio Productor

### 3.1 `ms-segunda-vida-donaciones` (`:8520`)

- **Indicadores:** `FOUNDATION_GARMENTS_RECEIVED` (acumulado por fundación) y `PLATFORM_MONTHLY_DONATIONS` (donaciones del mes en plataforma).
- **Qué cambia respecto a antes:** Ya no necesitamos eventos de solicitudes creadas, aprobadas/rechazadas, origen ni tipo de prenda. Únicamente el evento cuando la donación **confirma su recepción/entrega**.
- **Ajuste necesario en el productor:** Publicar en `styleradar.exchange` (en lugar de `donaciones.exchange`) con `eventId` único y `fundacionId`.
- **Routing key:** `styleradar.donaciones.completada`
- **Campos obligatorios:** `eventId`, `occurredAt` (o `timestamp`), `data.fundacionId` (o `data.foundationId`).
- **Campos opcionales:** `data.cantidad` (entero positivo; si se omite, se asume `1`).

```json
{
  "eventId": "550e8400-e29b-41d4-a716-446655440010",
  "eventType": "styleradar.donaciones.completada",
  "occurredAt": "2026-11-24T15:00:00Z",
  "data": {
    "fundacionId": "3",
    "cantidad": 1
  }
}
```

---

### 3.2 `ms-item-catalogo` (`:8220`)

- **Indicador:** `PLATFORM_GARMENT_PUBLICATIONS` (total acumulado de prendas publicadas).
- **Qué cambia respecto a antes:** Ya no necesitamos eventos de retiro (`prenda.retirada`), moderación (`moderacion.cambiada`, `prenda.moderacion.pendiente`) ni búsquedas (`busqueda.ejecutada`).
- **Routing key:** `styleradar.catalogo.prenda.publicada`
- **Campos obligatorios:** `eventId` (o `eventoId`), `occurredAt` (o `fechaEvento`).
- **Campos opcionales:** `data.itemId`, `data.tiendaId`, `cantidad` (por defecto `1`).

```json
{
  "eventoId": "evt-cat-pub-5001",
  "eventType": "styleradar.catalogo.prenda.publicada",
  "fechaEvento": "2026-11-24T15:40:00Z",
  "data": {
    "itemId": "garment-101",
    "tiendaId": "store-17"
  }
}
```

---

### 3.3 `ms-playlist` (`:8260`)

- **Indicador:** `PLATFORM_PLAYLISTS_CREATED` (total acumulado de playlists creadas).
- **Qué cambia respecto a antes:** Ya no necesitamos el evento de prenda guardada en playlist (`prenda.guardada`).
- **Routing key:** `styleradar.playlist.creada`
- **Campos obligatorios:** `eventId` (o `eventoId`), `occurredAt` (o `fechaCreacion`).

```json
{
  "eventId": "evt-pl-creada-9101",
  "eventType": "styleradar.playlist.creada",
  "occurredAt": "2026-11-24T16:15:00Z"
}
```

---

### 3.4 `ms-usuarios` (`:8120`)

- **Indicador:** `PLATFORM_USER_REGISTRATIONS` (registros diarios de usuarios).
- **Qué cambia respecto a antes:** Ya no necesitamos eventos de sesiones en tiempo real (`sesion.actualizada`) ni estado de fundaciones (`fundacion.estado`). La gestión de cuentas de compradores por plan (`PLATFORM_BUYERS_BY_PLAN`) queda bajo responsabilidad de `ms-pagos-suscripciones` (sección 3.6) y `ms-estadisticas`.
- **Nuevo evento requerido:** Emitir un evento cada vez que se registra una cuenta nueva en la plataforma.
- **Routing key:** `styleradar.usuarios.usuario.registrado`
- **Campos obligatorios:** `eventId`, `occurredAt`.
- **Campos opcionales:** `data.rol` (`BUYER` / `COMPRADOR` / `STORE_ADMIN` / `FOUNDATION_REPRESENTATIVE` / `STYLERADAR_ADMIN`).

```json
{
  "eventId": "550e8400-e29b-41d4-a716-446655440020",
  "eventType": "styleradar.usuarios.usuario.registrado",
  "occurredAt": "2026-11-24T16:30:00Z",
  "data": {
    "rol": "BUYER"
  }
}
```

> **Regla de negocio:** Todos los registros suman `+1` en `PLATFORM_USER_REGISTRATIONS` para el día correspondiente en `America/Bogota`.

---

### 3.5 `ms-inventario` (`:8200`)

- **Indicador:** `STORE_INVENTORY_BY_STATE` (`AVAILABLE`, `LOW_STOCK`, `OUT_OF_STOCK`).
- **Qué cambia respecto a antes:** Ya no necesitamos las alertas separadas `stock.ultimas-unidades` ni `stock.agotado`.
- **Ajustes necesarios en el productor:**
  1. Incluir **`data.tiendaId`** con el mismo ID de tienda que viaja en el JWT del `STORE_ADMIN` (si no se envía `tiendaId`, `ms-estadisticas` usa `almacenId` o `sedeId` como respaldo).
  2. Enviar **`data.stockAnterior: null`** cuando sea la **creación inicial** de la prenda/talla; enviar un entero `>= 0` solo cuando sea una actualización de stock existente.
- **Routing key:** `styleradar.inventario.actualizado`
- **Clasificación automática en `ms-estadisticas`:** `0` → `OUT_OF_STOCK`, `1..3` → `LOW_STOCK`, `>= 4` → `AVAILABLE`.

```json
{
  "eventId": "9c2e8a74-1f3b-4c5d-8e9a-0b1c2d3e4f5a",
  "eventType": "styleradar.inventario.actualizado",
  "timestamp": "2026-11-24T23:00:00Z",
  "data": {
    "tiendaId": "store-17",
    "sedeId": "fc942335-ab30-350a-b6a5-577cf0ca2fb5",
    "stockAnterior": null,
    "stockDisponible": 10
  }
}
```

---

### 3.6 `ms-pagos-suscripciones` (`:8400`)

Alimenta **3 indicadores** (`PLATFORM_SUBSCRIPTION_REVENUE`, `PLATFORM_BUYERS_BY_PLAN` y `PLATFORM_STORES_BY_PLAN`) en coordinación con `ms-estadisticas` mediante **2 eventos** (asumiendo también la responsabilidad completa de los usuarios de pago y planes de compradores):

#### A. Pago de suscripción confirmado (Ingresos mensuales)
- **Ajuste necesario en el productor:** Agregar **`data.monto`** (entero/numérico en COP) y **`data.estado`** (`APPROVED` / `APROBADO`). Sin `monto`, el pago se ignora para ingresos.
- **Routing key:** `styleradar.pagos.pago.confirmado`

```json
{
  "eventId": "550e8400-e29b-41d4-a716-446655440000",
  "eventType": "styleradar.pagos.pago.confirmado",
  "timestamp": "2026-11-24T15:00:00Z",
  "data": {
    "estado": "APPROVED",
    "monto": 14900,
    "moneda": "COP"
  }
}
```

#### B. Cambio o activación de plan de suscripción (Cuentas por plan)
- **Nuevo evento requerido:** Emitir **únicamente** cuando una cuenta **activa por primera vez, cambia de plan o suspende/pierde su suscripción** (no emitir en renovaciones mensuales del mismo plan).
- **Routing key:** `styleradar.pagos.suscripcion.plan.actualizado`
- **Valores aceptados en `planAnterior` / `planActual`:**
  - Compradores: `PLAN_RADAR`, `PLAN_RADAR_PLUS` (o `RADAR`, `RADAR_PLUS`), o `null` si es alta inicial.
  - Almacenes: `PLAN_STORE_MONTHLY` (o `STORE`, `ALMACEN`), `SUSPENDED` (al suspender/vencer), o `null` si es alta inicial.

```json
{
  "eventId": "550e8400-e29b-41d4-a716-446655440099",
  "eventType": "styleradar.pagos.suscripcion.plan.actualizado",
  "timestamp": "2026-11-24T15:00:05Z",
  "data": {
    "tipoSuscriptor": "BUYER",
    "planAnterior": "PLAN_RADAR",
    "planActual": "PLAN_RADAR_PLUS"
  }
}
```

---

## 4. Contrato Transversal de Seguridad (`ms-auth` + `api-gateway`)

Para que los usuarios puedan consultar los 3 endpoints de `ms-estadisticas`:

- **Secreto compartido:** `JWT_SECRET` (HS256, codificado en Base64) idéntico entre `ms-auth` y `ms-estadisticas`.
- **Claims del JWT / Cabeceras del Gateway:**
  - `role` / `roles` (o `X-User-Role`):
    - Tienda: `STORE_ADMIN` / `ADMIN_ALMACEN` + claim `storeId` (o `idTienda`, `store_id`, `nit`) / cabecera `X-Store-Id`.
    - Fundación: `FOUNDATION_REPRESENTATIVE` / `FOUNDATION_ADMIN` / `REPRESENTANTE_FUNDACION` + claim `foundationId` (o `idFundacion`) / cabecera `X-Foundation-Id`.
    - Plataforma: `STYLERADAR_ADMIN` / `PLATFORM_ADMIN` / `ADMIN_STYLERADAR`.
  - `BUYER` / `COMPRADOR`: autentica el token, pero recibe `403 Forbidden` al consultar estadísticas.
