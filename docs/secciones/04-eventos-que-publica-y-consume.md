[⬅ Volver al README Principal](../../README.md)

---

## 4. EVENTOS QUE PUBLICA Y CONSUME

La comunicación asíncrona entre microservicios se realiza a través de **RabbitMQ 3.13** utilizando un Topic Exchange durable. Este microservicio actúa exclusivamente como **productor** de eventos de dominio (no consume eventos de otros microservicios).

* **Exchange Principal:** `styleradar.exchange` (`topic`, `durable = true`, declarado en `RabbitMQConfig.java`).
* **Colas y DLQ:** Las colas consumidoras y sus Dead Letter Queues (`DLQ`) son declaradas por cada microservicio consumidor (por ejemplo, `styleradar.estadisticas.eventos-dominio.q` en `ms-estadisticas`).
* **Patrón Transactional Outbox (`V8__event_outbox.sql`):** `OutboxEventPublisherAdapter` (`@Primary`) persiste cada evento en la tabla `event_outbox` dentro de la misma transacción ACID del cambio de pago o suscripción. El scheduler `OutboxPublisherScheduler` consulta cada `5000 ms` (`${styleradar.outbox.poll-ms:5000}`) hasta 50 eventos `PENDING`/`FAILED` y los publica a RabbitMQ con backoff exponencial (máximo `300 s`, hasta `10` intentos), evitando pérdida de eventos ante caídas temporales del broker.
* **Formato:** JSON UTF-8 con sobre `{eventId, eventType, version, timestamp, producer, correlationId, data}`.

### Matriz de Eventos AMQP

| Tipo | Routing Key | Cola (Queue) | Microservicio Emisor → Receptor | Cuándo se Dispara / Acción |
| :---: | :--- | :--- | :--- | :--- |
| **PUBLICA** | `styleradar.pagos.pago.confirmado` | Declarada por cada consumidor | `ms-pagos-suscripciones` → Bus (`ms-estadisticas`, `ms-item-catalogo`, `ms-notificaciones`) | Pago aprobado (`APPROVED`) y suscripción activada o extendida vía webhook o conciliación. |
| **PUBLICA** | `styleradar.pagos.suscripcion.suspendida` | Declarada por cada consumidor | `ms-pagos-suscripciones` → Bus (`ms-item-catalogo`, `ms-inventario`, `ms-notificaciones`) | Suscripción vencida sin pago; emitido por el job diario `SubscriptionScheduler` a las `00:10`. |
| **PUBLICA** | `styleradar.pagos.suscripcion.plan.actualizado` | Declarada por cada consumidor | `ms-pagos-suscripciones` → Bus (`ms-estadisticas`) | Alta inicial, cambio de plan o suspensión de suscripción (no se emite en renovaciones del mismo plan). |
| **CONSUME** | `N/A` | — | — | Este microservicio no consume eventos AMQP de otros microservicios. |

<details>
<summary><b>Click para ver Esquema JSON de los Eventos Publicados</b></summary>

<br>

#### 1. `PaymentConfirmedEvent` (`styleradar.pagos.pago.confirmado`)

```json
{
  "eventId": "550e8400-e29b-41d4-a716-446655440000",
  "eventType": "styleradar.pagos.pago.confirmado",
  "version": 1,
  "timestamp": "2026-11-24T15:00:00Z",
  "producer": "ms-pagos-suscripciones",
  "correlationId": "corr-uuid-1234",
  "data": {
    "ordenId": 10,
    "suscripcionId": 100,
    "suscriptorId": "42",
    "usuarioId": 42,
    "tipoSuscriptor": "USUARIO",
    "planId": 2,
    "nit": null,
    "almacenId": null,
    "estado": "APPROVED",
    "estadoSuscripcion": "ACTIVA",
    "monto": 14900.00,
    "moneda": "COP"
  }
}
```

#### 2. `SubscriptionPlanUpdatedEvent` (`styleradar.pagos.suscripcion.plan.actualizado`)

```json
{
  "eventId": "550e8400-e29b-41d4-a716-446655440099",
  "eventType": "styleradar.pagos.suscripcion.plan.actualizado",
  "version": 1,
  "timestamp": "2026-11-24T15:00:05Z",
  "producer": "ms-pagos-suscripciones",
  "correlationId": "corr-uuid-1234",
  "data": {
    "tipoSuscriptor": "BUYER",
    "planAnterior": null,
    "planActual": "PLAN_RADAR_PLUS"
  }
}
```

</details>

---

[⬅ Volver al README Principal](../../README.md)
