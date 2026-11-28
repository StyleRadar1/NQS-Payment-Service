[⬅ Volver al README Principal](../../README.md)

---

## 10. RAMAS Y CÓMO CONTRIBUIR

El equipo de **NQS (No Queremos Scrum)** adopta el modelo de trabajo **GitFlow adaptado para microservicios**. Las ramas `master` y `develop` están protegidas y reciben cambios exclusivamente mediante Pull Requests validados por el pipeline automatizado de GitHub Actions.

### Estructura Oficial de Ramas

```text
master                  <-- Código en producción (Desplegado en Azure Container Apps PROD)
└── develop             <-- Rama de integración continua (Desplegado en Azure Container Apps QA)
    ├── feature/<descripcion>                  <-- Nuevas funcionalidades (ej. feature/eventos-estadisticas)
    ├── feature/SR-<ID>-<descripcion>          <-- Funcionalidades trazadas a requerimiento
    ├── bugfix/<descripcion>                   <-- Corrección de defectos en QA/develop
    ├── docs/<descripcion>                     <-- Documentación y modularización de README
    └── hotfix/<descripcion-urgente>           <-- Parche crítico directo a producción
```

### Convención de Commits (*Conventional Commits*)

Todo mensaje de commit debe seguir la estructura estándar:

```text
<tipo>(<alcance>): <descripción imperativa en español> [#NQS-XX]

Ejemplos válidos en ms-pagos-suscripciones:
feat(suscripciones): iniciar suscripción de almacén con taxId y preapproval [#NQS-33]
fix(webhook): validar firma HMAC-SHA256 e idempotencia en Redis SETNX [#NQS-53]
test(dominio): agregar pruebas unitarias de PaymentOrder y Subscription [#NQS-55]
docs(readme): modularizar secciones y actualizar contratos de eventos
```

### Checklist Obligatorio para Pull Requests

- [ ] La rama parte de la versión más reciente de `develop`.
- [ ] El código compila limpiamente sin advertencias críticas con `mvn clean compile`.
- [ ] Todas las pruebas automatizadas pasan en verde y JaCoCo supera el 85% (`mvn clean verify`).
- [ ] No se introducen credenciales, secretos, certificados `.jks` ni contraseñas en el código (uso estricto de `.env.example` y GitHub Secrets).
- [ ] Los contenedores OCI se construyen exitosamente con `docker compose --profile app build`.
- [ ] El PR cuenta con al menos un revisor aprobado del squad antes de mezclarse a `develop`.

---

[⬅ Volver al README Principal](../../README.md)
