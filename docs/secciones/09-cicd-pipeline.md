[⬅ Volver al README Principal](../../README.md)

---

## 9. CI/CD CON EL BADGE DEL PIPELINE

El ciclo de vida del microservicio implementa integración y entrega continua automatizada mediante **GitHub Actions** (`.github/workflows/ci.yml`), construyendo y publicando la imagen OCI en Docker Hub y desplegando hacia **Azure Container Apps**.

### Badges de Pipelines y Calidad (GitHub Actions)

<p align="center">
  <a href="https://github.com/StyleRadar1/NQS-Payment-Service/actions/workflows/ci.yml"><img src="https://img.shields.io/badge/CI_Pipeline-passing-16A34A?style=for-the-badge&logo=githubactions&logoColor=white" alt="CI Pipeline" /></a>
  <a href="https://github.com/StyleRadar1/NQS-Payment-Service/actions/workflows/ci.yml"><img src="https://img.shields.io/badge/CD_QA_Container_Apps-active-5600EF?style=for-the-badge&logo=githubactions&logoColor=white" alt="CD QA Pipeline" /></a>
  <a href="https://github.com/StyleRadar1/NQS-Payment-Service/actions/workflows/ci.yml"><img src="https://img.shields.io/badge/CD_PROD_Container_Apps-configured-12063A?style=for-the-badge&logo=githubactions&logoColor=white" alt="CD PROD Pipeline" /></a>
  <a href="08-pruebas-y-cobertura.md"><img src="https://img.shields.io/badge/JaCoCo_Gate-%E2%89%A5_85%25-16A34A?style=for-the-badge&logo=codecov&logoColor=white" alt="JaCoCo Gate" /></a>
</p>

### Matriz de Pipelines por Rama Protegida

| Job (`ci.yml`) | Rama / Entorno | Trigger de Ejecución | Qué Hace / Quality Gate | Estado |
| :--- | :---: | :--- | :--- | :---: |
| **`build-and-push`** | `develop`, `master` | `push` a `develop` o `master`, y `pull_request` hacia `develop` | Configura JDK 21 Temurin, inyecta truststore RabbitMQ, ejecuta `mvn -B clean verify` (tests + JaCoCo ≥ 85%), construye imagen Docker multi-stage y publica `juangv1/styleradar-ms-pagos:{sha, latest}` | [![CI Pipeline](https://img.shields.io/badge/build--and--push-passing-16A34A?style=flat-square&logo=githubactions&logoColor=white)](https://github.com/StyleRadar1/NQS-Payment-Service/actions/workflows/ci.yml) |
| **`deploy`** | `develop` (Azure Container Apps **QA**, env `qa`) | `push` a `develop` tras `build-and-push` | Inicia sesión en Azure y ejecuta `az containerapp update` sobre la Container App de QA con la imagen del commit | [![CD QA](https://img.shields.io/badge/deploy_QA-active-5600EF?style=flat-square&logo=githubactions&logoColor=white)](https://github.com/StyleRadar1/NQS-Payment-Service/actions/workflows/ci.yml) |
| **`deploy-prod`** | `master` (Azure Container Apps **PROD**, env `production`) | `push` a `master` tras `build-and-push` | Inicia sesión en Azure y ejecuta `az containerapp update` sobre la Container App de PROD con la imagen del commit | [![CD PROD](https://img.shields.io/badge/deploy_PROD-configured-12063A?style=flat-square&logo=githubactions&logoColor=white)](https://github.com/StyleRadar1/NQS-Payment-Service/actions/workflows/ci.yml) |

> [!NOTE]
> El análisis de **SonarQube** (`sonar-maven-plugin`) está configurado en `pom.xml` para ejecución local (`mvn sonar:sonar`); actualmente el workflow `.github/workflows/ci.yml` valida el Quality Gate de compilación, pruebas y cobertura JaCoCo ≥ 85% en cada ejecución.

### Diagrama del Flujo Automatizado (Pipeline Stages)

```mermaid
flowchart LR
    A["Push / PR<br>(develop / master)"] --> B["Checkout & JDK 21<br>(Temurin + Cache Maven)"]
    B --> C["Preparar Truststore<br>(certs/rabbitmq-truststore.jks)"]
    C --> D["Build, Tests & JaCoCo<br>(mvn -B clean verify ≥ 85%)"]
    D --> E["Docker Build & Push<br>(juangv1/styleradar-ms-pagos)"]
    E -->|push a develop| F["Deploy QA<br>(Azure Container Apps)"]
    E -->|push a master| G["Deploy PROD<br>(Azure Container Apps)"]

    classDef step fill:#12063A,stroke:#5600EF,stroke-width:2px,color:#FFFFFF;
    classDef pass fill:#DCFCE7,stroke:#16A34A,stroke-width:2px,color:#0C0C12;

    class A,B,C,E,F,G step;
    class D pass;
```

### Estructura de Jobs en `.github/workflows/ci.yml`

1. **`build-and-push` (`Build · Tests · Docker Hub`):**
   - Se ejecuta sobre `ubuntu-latest` con JDK 21 Temurin y caché de dependencias Maven.
   - Decodifica el secreto `RABBITMQ_TRUSTSTORE_BASE64` hacia `certs/rabbitmq-truststore.jks` para que la imagen OCI incluya el certificado TLS de RabbitMQ.
   - Ejecuta `mvn -B clean verify` garantizando la suite de pruebas y el umbral JaCoCo `INSTRUCTION >= 85%`.
   - Construye y publica la imagen en Docker Hub (`juangv1/styleradar-ms-pagos:${{ github.sha }}` y `:latest`).
2. **`deploy` (`Deploy Container App QA`):**
   - Depende de `build-and-push` y se ejecuta automáticamente en `push` a `develop` (environment `qa`).
   - Actualiza la Container App de QA en Azure mediante `az containerapp update`.
3. **`deploy-prod` (`Deploy PROD`):**
   - Depende de `build-and-push` y se ejecuta en `push` a `master` (environment `production`).
   - Actualiza la Container App productiva en Azure mediante `az containerapp update`.

<div align="center">
  <img src="../images/pipeline-ci-cd.png" alt="Evidencia 04 - Pipeline CI/CD en GitHub Actions" width="100%" style="border-radius: 8px; box-shadow: 0 4px 12px rgba(0,0,0,0.15);" />
  <p><sub><b>Figura 9.1:</b> Ejecución exitosa del pipeline automatizado de CI/CD en GitHub Actions (<code>.github/workflows/ci.yml</code>), evidenciando compilación con JDK 21, verificación de pruebas y JaCoCo, construcción de la imagen OCI y despliegue en Azure Container Apps.</sub></p>
</div>

---

[⬅ Volver al README Principal](../../README.md)
