[⬅ Volver al README Principal](../../README.md)

---
<a id="1-nombre-del-servicio-y-su-bounded-context"></a>

## <img src="https://img.shields.io/badge/01-5600EF?style=for-the-badge" height="20" align="absmiddle" /> 1. NOMBRE DEL SERVICIO Y SU BOUNDED CONTEXT

* **Identificador de Servicio:** `NQS-Payment-Service` (`ms-pagos-suscripciones`)
* **Paquete Arquitectónico:** `Paquete 4: Pagos y Estadísticas`
* **Puerto de Escucha Oficial:** `8400`
* **Base de Datos Asignada:** `PostgreSQL 16` (`styleradar_payments` en el puerto `5432`), aislada bajo el patrón *Database-per-Service*.
* **Caché en Memoria:** `Redis 7` (puerto host `6389`) para la idempotencia de webhooks.
* **Bounded Context:** `Pagos de Suscripciones`

### Responsabilidades del Dominio

Este microservicio encapsula las responsabilidades relacionadas con la gestión de pagos asociados a las suscripciones de StyleRadar.

| Qué HACE este Microservicio (Alcance)                             | Qué NO HACE este Microservicio (Límites)                                          |
| :----------------------------------------------------------------- | :---------------------------------------------------------------------------------- |
| • Gestiona el proceso de pago de una suscripción mediante la pasarela de Mercado Pago (`SR-RF-67`, `SR-RF-68`, `SR-RF-100`, `SR-RF-101`).| • NO autentica usuarios (delegado a `ms-auth` en `:8100`).                     |
| • Consulta los pagos de las suscripciones. (`SR-RF-75`, `SR-RF-`)                         | • NO administra el catálogo de prendas ni el inventario de las tiendas.     |
| • Renueva automáticamente la suscripción de los usuarios y tiendas al finalizar cada periodo. (`SR-RF-95`)| • NO envía correos ni WebSockets (delegado a `ms-notificaciones` en `:8300`). |
| • Habilita o limita las funciones de los usuarios de acuerdo con el plan activo. (`SR-RF-102`) | • NO accede directamente a bases de datos de otros microservicios.                 |

### Catálogo de Requerimientos Funcionales Oficiales (Versión 7.0)

El microservicio `ms-pagos-suscripciones` implementa los siguientes requerimientos funcionales oficiales del análisis de requerimientos:

1. **`SR-RF-67`**: *Suscribir almacén*.
2. **`SR-RF-68`**: *Desactivar renovación automática de suscripción*.
3. **`SR-RF-75`**: *Consultar pagos de suscripciones*.
4. **`SR-RF-93`**: *Pagar suscripción mediante pasarela de pagos*.
5. **`SR-RF-94`**: *Confirmar el estado del pago*.
6. **`SR-RF-95`**: *Renovar suscripción con cobro automático*.
7. **`SR-RF-96`**: *Reintentar pago rechazado*.
8. **`SR-RF-97`**: *Consultar historial de pagos propios*.
9. **`SR-RF-99`**: *Suspender catálogo por falta de pago*.
10. **`SR-RF-100`**: *Suscribirse a un plan de usuario*.
11. **`SR-RF-101`**: *Cancelar plan de usuario*.
12. **`SR-RF-102`**: *Aplicar beneficios según el plan del usuario*.
13. **`SR-RF-103`**: *Volver al plan gratuito por falta de pago*.

### Arquetipos de Moda y Personajes de StyleRadar

<div align="center">

| Antonia (Sastre) | Mateo (Calle) | Aura (Futuro) | Nora (Rebelde) | Tomás (Esencial) |
| :---: | :---: | :---: | :---: | :---: |
| <img src="https://raw.githubusercontent.com/JUANNICOLASA/Frontend-Style-Radar/main/styleradar-frontend-v3/assets/mascota/antonia.jpg" width="110" style="border-radius:8px;" alt="Antonia" /><br>**Sastre**<br><sub>Tiendas & Descubrimiento</sub> | <img src="https://raw.githubusercontent.com/JUANNICOLASA/Frontend-Style-Radar/main/styleradar-frontend-v3/assets/mascota/mateo.jpg" width="110" style="border-radius:8px;" alt="Mateo" /><br>**Calle**<br><sub>Playlists & Cultura Urbana</sub> | <img src="https://raw.githubusercontent.com/JUANNICOLASA/Frontend-Style-Radar/main/styleradar-frontend-v3/assets/mascota/aura.jpg" width="110" style="border-radius:8px;" alt="Aura" /><br>**Futuro**<br><sub>Probador Virtual AI</sub> | <img src="https://raw.githubusercontent.com/JUANNICOLASA/Frontend-Style-Radar/main/styleradar-frontend-v3/assets/mascota/nora.jpg" width="110" style="border-radius:8px;" alt="Nora" /><br>**Rebelde**<br><sub>Segunda Vida & Vintage</sub> | <img src="https://raw.githubusercontent.com/JUANNICOLASA/Frontend-Style-Radar/main/styleradar-frontend-v3/assets/mascota/tomas.jpg" width="110" style="border-radius:8px;" alt="Tomás" /><br>**Esencial**<br><sub>Fundaciones & Donaciones</sub> |

</div>

---

[⬅ Volver al README Principal](../../README.md)