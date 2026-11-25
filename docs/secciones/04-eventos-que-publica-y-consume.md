[⬅ Volver al README Principal](../../README.md)

---

## <img src="https://img.shields.io/badge/04-5600EF?style=for-the-badge" height="20" align="absmiddle" /> 4. EVENTOS QUE PUBLICA Y CONSUME (CORREGIR)

La comunicación asíncrona entre microservicios se orquesta a través de **RabbitMQ 3.12**. Este microservicio actúa como **productor** del evento de confirmación de pago.
 
- **Exchange:** `styleradar.exchange` (topic, durable).
- **Dead Letter Exchange:** `styleradar.dlx`.
- **Rol:** productor. No consume eventos de otros microservicios.
- **Formato:** JSON UTF-8 con sobre `{eventId, eventType, version, timestamp, producer, correlationId, data}`.

### Matriz de Eventos AMQP
| Tipo | Routing key | Cuándo | Consumidores |
| :---: | :--- | :--- | :--- |
| **PUBLICA** | `styleradar.pagos.pago.confirmado` | Pago aprobado y suscripción activada/extendida | Catálogo (pendiente reactivación); Usuarios/Tiendas/Probador/Notificaciones (pendientes) |
| **PUBLICA** | `styleradar.pagos.suscripcion.suspendida` | Vencimiento sin pago; job 00:10 | Catálogo (implementado); Inventario/Notificaciones (pendientes de key/payload) |
| **CONSUME** | N/A | — | — |

---

[⬅ Volver al README Principal](../../README.md)