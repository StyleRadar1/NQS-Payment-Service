[⬅ Volver al README Principal](../../README.md)

---

## 6. CÓMO EJECUTAR LOCAL Y CON DOCKER

### Prerrequisitos de Entorno

* **Java Development Kit (JDK):** Versión 21 LTS instalada (`java -version`).
* **Apache Maven:** Versión 3.9+ instalada (`mvn -version`).
* **Docker Engine & Docker Compose:** Versión 24+ en ejecución (`docker --version`).

### Ejecución Local con Docker Compose
```bash
git clone https://github.com/StyleRadar1/NQS-Payment-Service.git
cd NQS-Payment-Service
cp .env.example .env
docker compose --profile app up --build
```

### Ejecución Local con Maven
```bash
docker compose up -d postgres redis rabbitmq
mvn clean spring-boot:run -Dspring-boot.run.profiles=dev
```
- Perfil `dev`: JWT desactivado, Mercado Pago mock, Rabbit local sin TLS.
- Perfil `test`: H2 en memoria, MP mock, sin Rabbit, JWT desactivado. Útil para probar endpoints sin ms-auth:
  ```bash
  mvn spring-boot:run -Dspring-boot.run.profiles=test
  ```
- Perfil `qa`: PostgreSQL/Redis/Rabbit reales, JWT HS256, Mercado Pago sandbox.

### Health local

```bash
curl -s http://localhost:8400/actuator/health
curl -s http://localhost:8400/api/v1/health
```
---

[⬅ Volver al README Principal](../../README.md)
