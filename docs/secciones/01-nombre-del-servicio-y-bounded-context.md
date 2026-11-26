[⬅ Volver al README Principal](../../README.md)

---

## 1. NOMBRE DEL SERVICIO Y SU BOUNDED CONTEXT

* **Identificador de Servicio:** `NQS-Payment-Service` (`ms-pagos-suscripciones`)
* **Paquete Arquitectónico:** Paquete 4: Financiero y Analítica (Pagos y Estadísticas)
* **Puerto de Escucha Oficial:** `8400` (asignado en la matriz sin colisiones de StyleRadar)
* **Base de Datos Asignada:** `PostgreSQL 16` (`styleradar_payments` en puerto host `5432`), aislada bajo el patrón *Database-per-Service*.
* **Caché en Memoria:** `Redis 7` (puerto host `6379`) para la idempotencia de webhooks mediante `SETNX`.
* **Bounded Context:** `Pagos y Suscripciones`

### Responsabilidades del Dominio

Este microservicio encapsula exclusivamente la lógica de negocio asociada al ciclo de vida de suscripciones de usuarios compradores (`USER`) y almacenes aliados (`STORE`), el procesamiento de pagos mediante Mercado Pago, la recepción idempotente de webhooks y la publicación confiable de eventos de pago.

| Qué HACE este Microservicio (Alcance) | Qué NO HACE este Microservicio (Límites) |
| :------------------------------------------------------------------------------------------------------------------------------------------ | :-------------------------------------------------------------------------------------------------------------------------- |
| • Gestiona el inicio, consulta y cancelación de renovación automática de suscripciones de almacenes y usuarios (`SR-RF-67`, `SR-RF-68`, `SR-RF-100`, `SR-RF-101`). | • NO autentica usuarios ni emite tokens JWT (delegado a `ms-auth` en `:8100`). |
| • Procesa pagos mediante pasarela Mercado Pago y confirma su estado vía webhooks firmados e idempotentes (`SR-RF-93`, `SR-RF-94`). | • NO administra el catálogo de prendas ni el inventario de las tiendas (delegado a `ms-item-catalogo` en `:8220` y `ms-inventario` en `:8200`). |
| • Renueva suscripciones aprobadas, permite reintentar pagos rechazados y suspende suscripciones vencidas (`SR-RF-95`, `SR-RF-96`, `SR-RF-99`, `SR-RF-103`). | • NO envía correos ni notificaciones push o WebSockets (delegado a `ms-notificaciones` en `:8300`). |
| • Expone el catálogo de planes y consulta el historial de pagos propios y globales para administración (`SR-RF-75`, `SR-RF-97`, `SR-RF-102`). | • NO gestiona datos maestros de perfiles ni tiendas (delegado a `ms-usuarios` en `:8120` y `ms-tiendas` en `:8140`). |
| • Garantiza persistencia ACID, doble idempotencia (Redis + PostgreSQL) y entrega confiable mediante Transactional Outbox. | • NO accede directamente a bases de datos de otros microservicios bajo el principio *Database-per-Service*. |

### Catálogo de Requerimientos Funcionales Oficiales (Versión 5.0)

El microservicio `ms-pagos-suscripciones` implementa los 13 requerimientos funcionales vigentes de su dominio (el requerimiento `SR-RF-98` de reembolso fue retirado en la migración `V9` por alcance del producto):

1. **`SR-RF-67`**: *Suscribir almacén aliado* (`POST /api/v1/subscriptions/start` con `subscriberType = STORE`, `taxId` y `storeId` opcional).
2. **`SR-RF-68`**: *Cancelar renovación automática de suscripción de almacén* (`POST /api/v1/subscriptions/{subscriberId}/cancel`).
3. **`SR-RF-75`**: *Consultar pagos de suscripciones (administración)* (`GET /api/v1/admin/payments` y `GET /api/v1/admin/payments/{id}`).
4. **`SR-RF-93`**: *Pagar suscripción mediante pasarela de pagos Mercado Pago* (`POST /api/v1/subscriptions/start` → `202 Accepted` + `initPoint`).
5. **`SR-RF-94`**: *Confirmar el estado del pago vía webhook* (`POST /api/v1/webhooks/mercadopago`).
6. **`SR-RF-95`**: *Renovar suscripción con cobro automático* (procesamiento de notificaciones `subscription_authorized_payment` y `SubscriptionActivationService`).
7. **`SR-RF-96`**: *Reintentar pago rechazado* (`POST /api/v1/payments/{orderId}/retry`).
8. **`SR-RF-97`**: *Consultar historial de pagos propios* (`GET /api/v1/payments/history`).
9. **`SR-RF-99`**: *Suspender catálogo de almacén por falta de pago* (job diario `SubscriptionScheduler` a las `00:10` + evento `styleradar.pagos.suscripcion.suspendida`).
10. **`SR-RF-100`**: *Suscribirse a un plan de usuario (`Radar+`)* (`POST /api/v1/subscriptions/start` con `subscriberType = USER`).
11. **`SR-RF-101`**: *Cancelar renovación automática de plan de usuario* (`POST /api/v1/subscriptions/{subscriberId}/cancel`).
12. **`SR-RF-102`**: *Consultar planes, beneficios y vigencia del plan activo* (`GET /api/v1/plans` y `GET /api/v1/subscriptions/{subscriberId}`).
13. **`SR-RF-103`**: *Volver al plan gratuito por falta de pago* (job diario `SubscriptionScheduler` + eventos `styleradar.pagos.suscripcion.suspendida` y `styleradar.pagos.suscripcion.plan.actualizado`).

### Arquetipos de Moda y Personajes de StyleRadar

Dentro del ecosistema StyleRadar, `ms-pagos-suscripciones` habilita transversalmente la experiencia de los arquetipos **Antonia (Sastre)** —al gestionar las suscripciones mensuales de las tiendas aliadas que publican su catálogo— y **Aura (Futuro)** —al habilitar la suscripción **Radar+** que desbloquea beneficios avanzados como el probador virtual AI y alertas de disponibilidad—.

---

[⬅ Volver al README Principal](../../README.md)