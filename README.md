<!-- ==============================================================================
  STYLE RADAR 
  Asignatura: DOSW · 2026
  Estándar obligatorio unificado para los 13 microservicios del proyecto StyleRadar.

  INSTRUCCIONES DE USO:
  1. Reemplaza todos los campos entre <ángulos> o [corchetes] con los datos de tu microservicio.
  2. Mantén estrictamente el orden de las 12 secciones oficiales del curso.
  3. No agregues emojis para preservar la seriedad y el estándar de ingeniería.
  4. Los bloques <details> proporcionan interactividad nativa en GitHub / GitLab / Bitbucket.
============================================================================== -->

<!-- ==============================================================================
  STYLE RADAR · TIPOGRAFÍA Y SISTEMA DE DISEÑO OFICIAL
  Tipografía Oficial: Poppins (Manual de Identidad & Arquitectura Frontend)
  - Títulos Display (Black / Bold): Poppins 900 / 800 / 700
  - Subtítulos (SemiBold): Poppins 600
  - Cuerpo y Tablas (Regular): Poppins 400
  - Etiquetas y Badges (Medium): Poppins 500
  - Apoyo y Notas (Light): Poppins 300
  - Código y Terminal: JetBrains Mono
============================================================================== -->
<!-- ==============================================================================
  STYLE RADAR · RECURSOS 100% IMPORTADOS VÍA CDN / HTTPS (PORTABILIDAD TOTAL)
  Todos los logos, banners, fuentes y badges se importan desde la nube para que
  al copiar y pegar este README en cualquier repositorio nuevo se renderice
  a la perfección sin requerir archivos locales ni producir enlaces rotos.
============================================================================== -->

<div align="center">

  <a href="https://github.com/StyleRadar">
    <img src="https://raw.githubusercontent.com/JUANNICOLASA/Frontend-Style-Radar/main/styleradar-frontend-v3/assets/logo/styleradar-logo.svg" width="340" alt="StyleRadar Logo Oficial" />
  </a>

  <h1>STYLE<font color="#5600EF">RADAR</font> &nbsp;·&nbsp; <code>ms-pagos-suscripciones</code></h1>

  <p align="center">
    <img src="https://img.shields.io/badge/BOUNDED_CONTEXT-PAGOS DE SUSCRIPCIONES-12063A?style=for-the-badge" alt="Context" />
    <img src="https://img.shields.io/badge/PUERTO_OFICIAL-8090-5600EF?style=for-the-badge" alt="Port" />
    <img src="https://img.shields.io/badge/ARQUITECTURA-HEXAGONAL-16A34A?style=for-the-badge" alt="Hexagonal" />
  </p>

  <p align="center">
    <strong><font color="#5600EF">Descubre tu estilo. En tu ciudad.</font></strong><br />
    <em>Plataforma de moda local con inventario multi-sede geolocalizado en tiempo real y probador virtual para Bogotá.</em>
  </p>

  <p align="center">
    <a href="https://styleradar-backend-prod.azurewebsites.net/swagger-ui/index.html">
      <img src="https://img.shields.io/badge/PROD-Active-16A34A?style=for-the-badge&logo=azure-app-services&logoColor=white" alt="PROD Deployment" />
    </a>
    <a href="https://styleradar-backend-qa.azurewebsites.net/swagger-ui/index.html">
      <img src="https://img.shields.io/badge/QA-Active-5600EF?style=for-the-badge&logo=azure-app-services&logoColor=white" alt="QA Deployment" />
    </a>
    <a href="<URL_SONARQUBE>">
      <img src="https://img.shields.io/badge/SonarQube-Quality_Gate_Passed-16A34A?style=for-the-badge&logo=sonarqube&logoColor=white" alt="Quality Gate" />
    </a>
    <a href="target/site/jacoco/index.html">
      <img src="https://img.shields.io/badge/JaCoCo_Coverage-%E2%89%A5_85%25-16A34A?style=for-the-badge&logo=codecov&logoColor=white" alt="Coverage" />
    </a>
    <a href="#6-tecnologías-y-dependencias">
      <img src="https://img.shields.io/badge/Java-21_LTS-12063A?style=for-the-badge&logo=openjdk&logoColor=C4B5FD" alt="Java 21" />
    </a>
    <a href="#6-tecnologías-y-dependencias">
      <img src="https://img.shields.io/badge/Spring_Boot-3.2.9-12063A?style=for-the-badge&logo=spring&logoColor=C4B5FD" alt="Spring Boot 3.2" />
    </a>
    <a href="#7-cómo-ejecutar-local-y-con-docker">
      <img src="https://img.shields.io/badge/Docker-OCI_Container-12063A?style=for-the-badge&logo=docker&logoColor=C4B5FD" alt="Docker OCI" />
    </a>
  </p>

</div>


---

## <img src="https://img.shields.io/badge/ÍNDICE-5600EF?style=for-the-badge" height="20" align="absmiddle" /> ÍNDICE DE CONTENIDOS

1. [Nombre del servicio y su bounded context](#1-nombre-del-servicio-y-su-bounded-context)
2. [Equipo y responsables](#2-equipo-y-responsables)
3. [Arquitectura hexagonal con su diagrama](#3-arquitectura-hexagonal-con-su-diagrama)
4. [Endpoints y enlace a Swagger](#4-endpoints-y-enlace-a-swagger)
5. [Eventos que publica y consume](#5-eventos-que-publica-y-consume)
6. [Tecnologías y dependencias](#6-tecnologías-y-dependencias)
7. [Cómo ejecutar local y con Docker](#7-cómo-ejecutar-local-y-con-docker)
8. [Variables de entorno](#8-variables-de-entorno)
9. [Pruebas y cobertura](#9-pruebas-y-cobertura)
10. [CI/CD con el badge del pipeline](#10-cicd-con-el-badge-del-pipeline)
11. [Ramas y cómo contribuir](#11-ramas-y-cómo-contribuir)
12. [Enlace a la épica en Jira](#12-enlace-a-la-épica-en-jira)

---

<a id="1-nombre-del-servicio-y-su-bounded-context"></a>

## <img src="https://img.shields.io/badge/01-5600EF?style=for-the-badge" height="20" align="absmiddle" /> 1. NOMBRE DEL SERVICIO Y SU BOUNDED CONTEXT

* **Identificador de Servicio:** `ms-pagos-suscripciones`
* **Paquete Arquitectónico:** `Paquete 4: Pagos y Estadisticas`
* **Puerto de Escucha Oficial:** `8090`
* **Base de Datos Asignada:** `PostgreSQL 16` (`<nombre_db>_db`), aislada bajo el patrón *Database-per-Service*.
* **Bounded Context:** `Pagos de Suscripciones`

### Responsabilidades del Dominio

Este microservicio encapsula las responsabilidades relacionadas con la gestión de pagos asociados a las suscripciones de StyleRadar.

| Qué HACE este Microservicio (Alcance)                             | Qué NO HACE este Microservicio (Límites)                                          |
| :----------------------------------------------------------------- | :---------------------------------------------------------------------------------- |
| • Gestiona el proceso de pago de una suscripción, según los requerimientos definidos.                          | • NO autentica usuarios (delegado a `ms-auth` en `:8081`).                     |
| • Registra y consulta el estado de los pagos.                         | • No administra el catálogo de prendas ni el inventario de las tiendas.     |
| • Representa los resultados de pago, incluyendo los estados alternativos que definan los requerimientos.                       | • NO envía correos ni WebSockets (delegado a `ms-notificaciones` en `:8092`). |
| • Garantiza persistencia ACID e idempotencia en su base de datos. | • NO accede directamente a bases de datos de otros microservicios.                 |

### Arquetipos de Moda y Personajes de StyleRadar

<div align="center">

| Antonia (Sastre) | Mateo (Calle) | Aura (Futuro) | Nora (Rebelde) | Tomás (Esencial) |
| :---: | :---: | :---: | :---: | :---: |
| <img src="https://raw.githubusercontent.com/JUANNICOLASA/Frontend-Style-Radar/main/styleradar-frontend-v3/assets/mascota/antonia.jpg" width="110" style="border-radius:8px;" alt="Antonia" /><br>**Sastre**<br><sub>Tiendas & Descubrimiento</sub> | <img src="https://raw.githubusercontent.com/JUANNICOLASA/Frontend-Style-Radar/main/styleradar-frontend-v3/assets/mascota/mateo.jpg" width="110" style="border-radius:8px;" alt="Mateo" /><br>**Calle**<br><sub>Playlists & Cultura Urbana</sub> | <img src="https://raw.githubusercontent.com/JUANNICOLASA/Frontend-Style-Radar/main/styleradar-frontend-v3/assets/mascota/aura.jpg" width="110" style="border-radius:8px;" alt="Aura" /><br>**Futuro**<br><sub>Probador Virtual AI</sub> | <img src="https://raw.githubusercontent.com/JUANNICOLASA/Frontend-Style-Radar/main/styleradar-frontend-v3/assets/mascota/nora.jpg" width="110" style="border-radius:8px;" alt="Nora" /><br>**Rebelde**<br><sub>Segunda Vida & Vintage</sub> | <img src="https://raw.githubusercontent.com/JUANNICOLASA/Frontend-Style-Radar/main/styleradar-frontend-v3/assets/mascota/tomas.jpg" width="110" style="border-radius:8px;" alt="Tomás" /><br>**Esencial**<br><sub>Fundaciones & Donaciones</sub> |

</div>

---

<a id="2-equipo-y-responsables"></a>

## <img src="https://img.shields.io/badge/02-5600EF?style=for-the-badge" height="20" align="absmiddle" /> 2. EQUIPO Y RESPONSABLES

Desarrollado y mantenido por el **Squad `NQS: No Queremos Scrum`** para la asignatura DOSW 2026.

<div align="center">

| Rol | Nombre Completo | Usuario GitHub |
| :---: | :---: | :---: |
| **Líder y apoyo** | `Cristian Camilo Ortiz Sanchez` | [@CatrachoINXS](https://github.com/CatrachoINXS) |
| **Backend Developer** | `Juan Manuel Garzón Viracacha` | [@JuanMaGV1](https://github.com/JuanMaGV1) |
| **Backend Developer** | `Daniel Jose Villamizar Castellanos` | [@ChanguaLover4ever](https://github.com/ChanguaLover4ever) |
| **Frontend Developer** | `Julian Felipe Morales Zambrano` | [@JulianMorales2003](https://github.com/JulianMorales2003) |

</div>

---

<a id="3-arquitectura-hexagonal-con-su-diagrama"></a>

## <img src="https://img.shields.io/badge/03-5600EF?style=for-the-badge" height="20" align="absmiddle" /> 3. ARQUITECTURA HEXAGONAL CON SU DIAGRAMA

El microservicio implementa **Arquitectura Hexagonal (Puertos y Adaptadores)** combinada con **Domain-Driven Design (DDD)**. El núcleo de negocio permanece 100% puro y aislado de frameworks, bases de datos o brokers de mensajería.

### Diagrama Interactivo de Capas (Mermaid)

```mermaid
flowchart TD
    subgraph INFRA_IN["Infraestructura de Entrada (Driving Adapters)"]
        REST["REST Controller<br>(Spring Web / OpenAPI 3)"]
        AMQP_IN["RabbitMQ Listener<br>(Spring AMQP Consumer)"]
    end

    subgraph APP["Capa de Aplicación (Application Layer)"]
        IN_PORT["Input Ports<br>(Interfaces de Casos de Uso)"]
        USE_CASES["Use Cases Implementation<br>(Servicios de Aplicación)"]
        OUT_PORT["Output Ports<br>(Interfaces de Persistencia y Mensajería)"]
    end

    subgraph DOMAIN["Núcleo de Dominio (Domain Core - Java Puro)"]
        ENTITIES["Entidades de Dominio<br>(Entities & Aggregates)"]
        VO["Value Objects<br>(Inmutables & Reglas de Validación)"]
        EVENTS["Domain Events<br>(Eventos de Negocio)"]
        EXCEPTIONS["Domain Exceptions<br>(Excepciones de Negocio)"]
    end

    subgraph INFRA_OUT["Infraestructura de Salida (Driven Adapters)"]
        JPA_REPO["JPA Adapter<br>(Spring Data / PostgreSQL 16)"]
        RABBIT_PUB["RabbitMQ Publisher<br>(Spring AMQP Producer)"]
        REDIS_ADAPTER["Redis Adapter<br>(Caché & Distributed Locks)"]
    end

    REST --> IN_PORT
    AMQP_IN --> IN_PORT
    IN_PORT --> USE_CASES
    USE_CASES --> DOMAIN
    USE_CASES --> OUT_PORT
    OUT_PORT --> JPA_REPO
    OUT_PORT --> RABBIT_PUB
    OUT_PORT --> REDIS_ADAPTER

    classDef domain fill:#EDE7FF,stroke:#5600EF,stroke-width:2px,color:#0C0C12;
    classDef app fill:#F8FAFC,stroke:#C4B5FD,stroke-width:2px,color:#0C0C12;
    classDef infra fill:#12063A,stroke:#5600EF,stroke-width:1.5px,color:#FFFFFF;

    class DOMAIN,ENTITIES,VO,EVENTS,EXCEPTIONS domain;
    class APP,IN_PORT,USE_CASES,OUT_PORT app;
    class INFRA_IN,INFRA_OUT,REST,AMQP_IN,JPA_REPO,RABBIT_PUB,REDIS_ADAPTER infra;
```

<div align="center">
  <table width="100%" style="margin: 18px 0; border: 2px dashed #C4B5FD; border-radius: 8px; background-color: #FBFBFE;">
    <tr>
      <td align="center" style="padding: 20px;">
        <p>
          <img src="https://img.shields.io/badge/ESPACIO_DE_DIAGRAMA-DRAW.IO_/_PLANTUML-12063A?style=for-the-badge" alt="Diagrama Opcional" /><br />
          <strong><font color="#5600EF" size="3">DIAGRAMA DE ARQUITECTURA HEXAGONAL EXPORTADO (OPCIONAL)</font></strong>
        </p>
        <p>
          <sub>Si tu squad cuenta con un diagrama gráfico exportado de Draw.io, Miro o PlantUML, pégalo en este contenedor como alternativa visual.</sub>
        </p>
        <p>
<!-- <img src="docs/evidencias/diagrama-hexagonal.png" width="90%" alt="Diagrama Hexagonal" /><br /> -->
          <code>[ Espacio opcional para diagrama exportado · docs/evidencias/diagrama-hexagonal.png ]</code>
        </p>
      </td>
    </tr>
  </table>
</div>

### Estructura Oficial de Paquetes en `src/main/java/`

```text
src/main/java/com/styleradar/pagos_suscripciones/
├── domain/                                  <-- NÚCLEO DE DOMINIO (Cero dependencias externas)
│   ├── model/                               <-- Entidades y Agregados raíz
│   ├── valueobject/                         <-- Value Objects inmutables (PrecioCop, Talla, UUIDv7)
│   ├── event/                               <-- Definición de Eventos de Dominio
│   └── exception/                           <-- Excepciones de reglas de negocio
├── application/                             <-- CAPA DE APLICACIÓN (Casos de Uso)
│   ├── port/
│   │   ├── in/                              <-- Input Ports (Interfaces para controladores)
│   │   └── out/                             <-- Output Ports (Interfaces para repositorios y broker)
│   ├── usecase/                             <-- Implementación de casos de uso
│   └── dto/                                 <-- DTOs de entrada y salida (Java Records)
└── infrastructure/                          <-- CAPA DE INFRAESTRUCTURA (Adaptadores)
    ├── adapter/
    │   ├── in/
    │   │   ├── rest/                        <-- Spring REST Controllers y OpenAPI Spec
    │   │   └── amqp/                        <-- Consumidores de eventos RabbitMQ
    │   └── out/
    │       ├── persistence/                 <-- Spring Data JPA Repositories y Entidades SQL
    │       ├── messaging/                   <-- Productores RabbitMQ (AmqpTemplate)
    │       └── cache/                       <-- Clientes Redis (RedisTemplate / Redlock)
    └── config/                              <-- Beans de configuración Spring (Seguridad, AMQP, DB)
```

---

<a id="4-endpoints-y-enlace-a-swagger"></a>

## <img src="https://img.shields.io/badge/04-5600EF?style=for-the-badge" height="20" align="absmiddle" /> 4. ENDPOINTS Y ENLACE A SWAGGER

La documentación interactiva OpenAPI 3.0 y los contratos de ejecución se encuentran desplegados y clasificados por entorno:

### Matriz Oficial de Enlaces por Entorno (PROD, QA, LOCAL)

| Entorno | Estado | Propósito | URL Base del Servicio | Swagger OpenAPI UI | Health Check |
| :--- | :---: | :--- | :--- | :--- | :--- |
| **Producción (PROD)** | Active | Despliegue productivo de StyleRadar | `https://styleradar-backend-prod.azurewebsites.net` | [Swagger UI PROD](https://styleradar-backend-prod.azurewebsites.net/swagger-ui/index.html) | [`/actuator/health`](https://styleradar-backend-prod.azurewebsites.net/actuator/health) |
| **Calidad / Pruebas (QA)** | Active | Pruebas integradas de Sprint y QA | `https://styleradar-backend-qa.azurewebsites.net` | [Swagger UI QA](https://styleradar-backend-qa.azurewebsites.net/swagger-ui/index.html) | [`/actuator/health`](https://styleradar-backend-qa.azurewebsites.net/actuator/health) |
| **Desarrollo Local (DEV)** | Local | Entorno de desarrollo local en máquina | `http://localhost:8090` | [Swagger UI Local](http://localhost:8090/swagger-ui.html) | [`/actuator/health`](http://localhost:8090/actuator/health) |

* **Especificación OpenAPI (Contrato JSON Schema):** `http://localhost:8090/v3/api-docs` (o `/v3/api-docs` en los dominios de PROD y QA).


<div align="center">
  <table width="100%" style="margin: 18px 0; border: 2px dashed #5600EF; border-radius: 8px; background-color: #FBFBFE;">
    <tr>
      <td align="center" style="padding: 22px;">
        <p>
          <img src="https://img.shields.io/badge/ESPACIO_DE_CAPTURA-EVIDENCIA_01-5600EF?style=for-the-badge" alt="Evidencia 01" /><br />
          <strong><font color="#5600EF" size="3">DOCUMENTACIÓN SWAGGER OPENAPI UI ACTIVA</font></strong>
        </p>
        <p>
          <sub>Pega aquí la captura de pantalla de la interfaz interactiva corriendo en <code>http://localhost:808X/swagger-ui.html</code> mostrando los endpoints del controlador.</sub>
        </p>
        <p>
<!-- Al generar tu captura, descomenta la siguiente línea y guarda la imagen en la ruta indicada: -->
<!-- <img src="docs/evidencias/swagger-ui.png" width="90%" alt="Evidencia Swagger UI" /><br /> -->
          <code>[ Pegar captura de Swagger UI aquí · docs/evidencias/swagger-ui.png ]</code>
        </p>
      </td>
    </tr>
  </table>
</div>

### Tabla de Endpoints Expuestos

|  Método  | Endpoint                      | Descripción                               | Rol Requerido         |       Códigos HTTP       |
| :--------: | :---------------------------- | :----------------------------------------- | :-------------------- | :-----------------------: |
|  `GET`  | `/api/v1/pagos/health` | Health Check y estado del microservicio    | Público              |          `200`          |

---

<a id="5-eventos-que-publica-y-consume"></a>

## <img src="https://img.shields.io/badge/05-5600EF?style=for-the-badge" height="20" align="absmiddle" /> 5. EVENTOS QUE PUBLICA Y CONSUME

La comunicación asíncrona entre microservicios se orquesta a través de **RabbitMQ 3.12** utilizando Topic Exchanges durables.

* Estado: Pendiente de definir el mecanismo de comunicación entre microservicios.

Se debe determinar si el servicio publicará eventos relacionados con el resultado de un pago y si consumirá eventos relacionados con suscripciones.

---

<a id="6-tecnologías-y-dependencias"></a>

## <img src="https://img.shields.io/badge/06-5600EF?style=for-the-badge" height="20" align="absmiddle" /> 6. TECNOLOGÍAS Y DEPENDENCIAS

El stack del microservicio cumple con el estándar unificado de arquitectura de StyleRadar:

| Capa / Componente                | Tecnología       | Versión   | Justificación Técnica                                                                        |
| :------------------------------- | :---------------- | :--------- | :--------------------------------------------------------------------------------------------- |
| **Lenguaje Core**          | Java LTS          | `21`     | Virtual Threads (Project Loom) para concurrencia masiva I/O sin saturar hilos del OS.          |
| **Framework Empresarial**  | Spring Boot       | `3.2.9`  | Gestión declarativa de configuración, ecosistema nativo Spring Data y Spring AMQP.           |
| **Base de Datos**          | PostgreSQL        | `16`     | Integridad transaccional ACID, llaves UUIDv7 y soporte de extensiones espaciales/GIN.          |
| **Migraciones de BD**      | Flyway            | `10.x`   | Control de versiones reproducible de scripts SQL (`V1__init.sql`) en arranque de contenedor. |
| **Broker de Mensajería**  | RabbitMQ          | `3.12+`  | Desacoplamiento temporal de microservicios mediante Topic Exchanges durables.                  |
| **Caché en Memoria**      | Redis             | `7.x`    | Caché L2 para consultas de alto tráfico y candados distribuidos (Redlock).                   |
| **Mapeo de DTOs**          | MapStruct         | `1.5.x`  | Generación de mappers en tiempo de compilación con cero sobrecarga de reflection.            |
| **Testing Core**           | JUnit 5 + Mockito | `5.10.x` | Pruebas unitarias sobre el núcleo de dominio sin inicializar el contenedor Spring.            |
| **Testing Integración**   | Testcontainers    | `1.19.x` | Instanciación automática de PostgreSQL y RabbitMQ en Docker durante `mvn verify`.          |
| **Métricas de Cobertura** | JaCoCo Plugin     | `0.8.11` | Verificación estricta de cobertura mínima (≥ 85%) como regla de fallo en el build.     |

---

<a id="7-cómo-ejecutar-local-y-con-docker"></a>

## <img src="https://img.shields.io/badge/07-5600EF?style=for-the-badge" height="20" align="absmiddle" /> 7. CÓMO EJECUTAR LOCAL Y CON DOCKER

### Prerrequisitos de Entorno

* **Java Development Kit (JDK):** Versión 21 LTS instalada (`java -version`).
* **Apache Maven:** Versión 3.9+ instalada (`mvn -version`).
* **Docker Engine & Docker Compose:** Versión 24+ en ejecución (`docker --version`).

### Ejecución Local con Maven (Perfil Dev)

```bash
# 1. Clonar el repositorio
git clone https://github.com/StyleRadar/NQS-Payment-Service.git
cd NQS-Payment-Service# 2. Levantar servicios de infraestructura en Docker (PostgreSQL, Redis, RabbitMQ)
docker compose up -d postgres rabbitmq redis

# 3. Ejecutar el microservicio localmente
mvn clean spring-boot:run -Dspring-boot.run.profiles=dev
```

### Comprobación de Salud (Health Check)

```bash
curl -s http://localhost:8090/actuator/health | jq .
# Esperado: {"status":"UP","components":{"db":{"status":"UP"},"rabbit":{"status":"UP"}}}
```

<div align="center">
  <table width="100%" style="margin: 18px 0; border: 2px dashed #5600EF; border-radius: 8px; background-color: #FBFBFE;">
    <tr>
      <td align="center" style="padding: 22px;">
        <p>
          <img src="https://img.shields.io/badge/ESPACIO_DE_CAPTURA-EVIDENCIA_02-5600EF?style=for-the-badge" alt="Evidencia 02" /><br />
          <strong><font color="#5600EF" size="3">CONTENEDOR OCI DOCKER EN EJECUCIÓN (docker compose ps)</font></strong>
        </p>
        <p>
          <sub>Pega aquí la captura de terminal ejecutando <code>docker compose ps</code> mostrando el contenedor del microservicio y sus servicios de persistencia y broker en estado healthy.</sub>
        </p>
        <p>
<!-- Al generar tu captura, descomenta la siguiente línea y guarda la imagen en la ruta indicada: -->
<!-- <img src="docs/evidencias/docker-compose-ps.png" width="90%" alt="Docker Compose PS" /><br /> -->
          <code>[ Pegar captura de Docker compose ps aquí · docs/evidencias/docker-compose-ps.png ]</code>
        </p>
      </td>
    </tr>
  </table>
</div>


---

<a id="8-variables-de-entorno"></a>

## <img src="https://img.shields.io/badge/08-5600EF?style=for-the-badge" height="20" align="absmiddle" /> 8. VARIABLES DE ENTORNO

Todas las configuraciones sensibles y dependientes de entorno se externalizan mediante variables de sistema conformes al estándar **Twelve-Factor App**.

| Variable                   | Descripción                                 | Valor por Defecto (`dev`) | Requerido en Prod |
| :------------------------- | :------------------------------------------- | :-------------------------- | :---------------: |
| `SERVER_PORT`            | Puerto de escucha HTTP del microservicio     | `8090`                    |        Sí        |
| `SPRING_PROFILES_ACTIVE` | Perfil de configuración Spring Boot activo  | `dev`                     |        Sí        |
| `DB_HOST`                | Hostname o IP del servidor PostgreSQL        | `localhost`               |        Sí        |
| `DB_PORT`                | Puerto de conexión a PostgreSQL             | `5432`                    |        Sí        |
| `DB_NAME`                | Nombre de la base de datos aislada           | `<nombre_db>_db`          |        Sí        |
| `DB_USER`                | Usuario de conexión a la base de datos      | `styleradar_user`         |        Sí        |
| `DB_PASSWORD`            | Contraseña del usuario de base de datos     | `secret_dev_pass`         |        Sí        |
| `RABBITMQ_HOST`          | Hostname del broker de mensajería           | `localhost`               |        Sí        |
| `RABBITMQ_PORT`          | Puerto AMQP de RabbitMQ                      | `5672`                    |        Sí        |
| `RABBITMQ_USERNAME`      | Usuario de conexión a RabbitMQ              | `guest`                   |        Sí        |
| `RABBITMQ_PASSWORD`      | Contraseña de conexión a RabbitMQ          | `guest`                   |        Sí        |
| `REDIS_HOST`             | Hostname del servidor de caché Redis        | `localhost`               |        No        |
| `JWT_PUBLIC_KEY`         | Clave pública RSA/RS256 para validar tokens | `<PATH_OR_PEM_VALUE>`     |        Sí        |

<details>
<summary><b>Click para copiar la Plantilla de Archivo .env.example</b></summary>

<br>

```dotenv
# ==============================================================================
# STYLERADAR · VARIABLES DE ENTORNO LOCALES (.env)
# ==============================================================================
SERVER_PORT=8090
SPRING_PROFILES_ACTIVE=dev

# Persistencia PostgreSQL
DB_HOST=localhost
DB_PORT=5432
DB_NAME=<nombre_db>_db
DB_USER=styleradar_user
DB_PASSWORD=styleradar_secret

# Event Broker RabbitMQ
RABBITMQ_HOST=localhost
RABBITMQ_PORT=5672
RABBITMQ_USERNAME=guest
RABBITMQ_PASSWORD=guest

# Caché Redis
REDIS_HOST=localhost
REDIS_PORT=6379

# Seguridad JWT
JWT_PUBLIC_KEY=classpath:keys/public-key.pem
```

</details>

---

<a id="9-pruebas-y-cobertura"></a>

## <img src="https://img.shields.io/badge/09-5600EF?style=for-the-badge" height="20" align="absmiddle" /> 9. PRUEBAS Y COBERTURA (≥ 85%)

El proyecto exige una cobertura de código mínima del **85%** verificada automáticamente mediante el plugin de **JaCoCo** durante la fase de compilación.

### Comandos de Ejecución de Pruebas

```bash
# Ejecutar todas las pruebas unitarias y de integración
mvn clean verify

# Generar únicamente el reporte HTML de JaCoCo
mvn jacoco:report
```

* **Ruta del Reporte HTML:** `target/site/jacoco/index.html`

<div align="center">
  <table width="100%" style="margin: 18px 0; border: 2px dashed #5600EF; border-radius: 8px; background-color: #FBFBFE;">
    <tr>
      <td align="center" style="padding: 22px;">
        <p>
          <img src="https://img.shields.io/badge/ESPACIO_DE_CAPTURA-EVIDENCIA_03-5600EF?style=for-the-badge" alt="Evidencia 03" /><br />
          <strong><font color="#5600EF" size="3">REPORTE CONSOLIDADO DE COBERTURA JACOCO (≥ 85%)</font></strong>
        </p>
        <p>
          <sub>Pega aquí la captura del reporte HTML (<code>target/site/jacoco/index.html</code>) evidenciando el cumplimiento de la cobertura mínima obligatoria (≥ 85%).</sub>
        </p>
        <p>
<!-- Al generar tu captura, descomenta la siguiente línea y guarda la imagen en la ruta indicada: -->
<!-- <img src="docs/evidencias/cobertura-jacoco.png" width="90%" alt="Reporte JaCoCo" /><br /> -->
          <code>[ Pegar captura de JaCoCo ≥ 85% aquí · docs/evidencias/cobertura-jacoco.png ]</code>
        </p>
      </td>
    </tr>
  </table>
</div>


### Pirámide de Pruebas Implementada

| Nivel de Prueba                   | Alcance                                    | Tecnologías Utilizadas          | Ubicación en el Código                                    | Cobertura Meta |
| :-------------------------------- | :----------------------------------------- | :------------------------------- | :---------------------------------------------------------- | :------------: |
| **Pruebas Unitarias**       | Lógica de Dominio y Casos de Uso aislados | JUnit 5, Mockito, AssertJ        | `src/test/java/.../domain/`, `src/test/java/.../application/` | ≥ 90% |
| **Pruebas de Integración** | Repositorios JPA y Adaptadores REST        | Spring Boot Test, Testcontainers | `src/test/java/.../infrastructure/`                       |  ≥ 85%  |
| **Pruebas de Contrato**     | Verificación de contratos OpenAPI         | MockMvc, JSON Assert             | `src/test/java/.../rest/`                                 |  ≥ 85%  |

> [!IMPORTANT]
> **Criterio de Fallo en CI:** El archivo `pom.xml` contiene la regla `<limit><minimum>0.85</minimum></limit>`. Si la cobertura total consolidada es inferior al 85%, el comando `mvn verify` fallará con error y el pipeline bloqueará el Pull Request.

---

<a id="10-cicd-con-el-badge-del-pipeline"></a>

## <img src="https://img.shields.io/badge/10-5600EF?style=for-the-badge" height="20" align="absmiddle" /> 10. CI/CD CON EL BADGE DEL PIPELINE

El ciclo de vida del software implementa integración continua y entrega continua automatizada mediante **GitHub Actions / Azure Pipelines**:

### Matriz de Pipelines por Entorno (PROD y QA)

| Pipeline | Entorno Destino | Desencadenador (Trigger) | Quality Gate Exigido | Badge de Estado |
| :--- | :--- | :--- | :--- | :---: |
| **CI - Staging / QA** | Calidad (`QA`) | Pull Request o Push a rama `develop` | Build OK + Tests Unitarios/Integración + JaCoCo ≥ 85% + SonarQube OK | [![QA Pipeline](https://img.shields.io/badge/Pipeline_QA-Passing-5600EF?style=for-the-badge&logo=azure-pipelines&logoColor=white)](https://github.com/CatrachoINXS/StyleRadar-Backend/actions) |
| **CD - Producción (PROD)** | Producción (`PROD`) | Merge a rama `main` tras validación QA | Aprobación manual + Imagen OCI desplegada en Azure App Service | [![PROD Pipeline](https://img.shields.io/badge/Pipeline_PROD-Passing-16A34A?style=for-the-badge&logo=azure-pipelines&logoColor=white)](https://github.com/CatrachoINXS/StyleRadar-Backend/actions) |
| **Quality Gate SonarQube** | Transversal | Ejecutado en cada build de CI | 0 Vulnerabilidades, 0 Bugs, Deuda Técnica &lt; 5% | [![Quality Gate](https://img.shields.io/badge/SonarQube-Quality_Gate_Passed-16A34A?style=for-the-badge&logo=sonarqube&logoColor=white)](<URL_SONARQUBE>) |

### Diagrama del Flujo Automatizado (Pipeline Stages)

```mermaid
flowchart LR
    A["Push / PR<br>(GitHub)"] --> B["Build & Compile<br>(Maven Java 21)"]
    B --> C["Tests & JaCoCo<br>(Coverage ≥ 85%)"]
    C --> D["Análisis SonarQube<br>(0 Vulnerabilities)"]
    D --> E["Build Docker OCI<br>(Multi-stage Build)"]
    E --> F["Deploy Cloud<br>(Azure App Service)"]

    classDef step fill:#12063A,stroke:#5600EF,stroke-width:2px,color:#FFFFFF;
    classDef pass fill:#DCFCE7,stroke:#16A34A,stroke-width:2px,color:#0C0C12;

    class A,B,E,F step;
    class C,D pass;
```

### Reglas de Calidad en SonarQube (*Quality Gate Passed*)

* **Bugs:** 0
* **Vulnerabilidades:** 0
* **Hotspots de Seguridad:** 0 revisados al 100%
* **Code Smells Críticos:** 0
* **Deuda Técnica:** < 5%

<div align="center">
  <table width="100%" style="margin: 18px 0; border: 2px dashed #5600EF; border-radius: 8px; background-color: #FBFBFE;">
    <tr>
      <td align="center" style="padding: 22px;">
        <p>
          <img src="https://img.shields.io/badge/ESPACIO_DE_CAPTURA-EVIDENCIA_04-5600EF?style=for-the-badge" alt="Evidencia 04" /><br />
          <strong><font color="#5600EF" size="3">EJECUCIÓN DEL PIPELINE CI/CD EN VERDE (BUILD & QUALITY GATE PASSED)</font></strong>
        </p>
        <p>
          <sub>Pega aquí la captura del pipeline de integración continua (Azure DevOps / GitHub Actions) finalizado con éxito en todas sus fases.</sub>
        </p>
        <p>
<!-- Al generar tu captura, descomenta la siguiente línea y guarda la imagen en la ruta indicada: -->
<!-- <img src="docs/evidencias/pipeline-ci-cd.png" width="90%" alt="Pipeline CI/CD" /><br /> -->
          <code>[ Pegar captura del Pipeline CI/CD en verde aquí · docs/evidencias/pipeline-ci-cd.png ]</code>
        </p>
      </td>
    </tr>
  </table>
</div>

---

<a id="11-ramas-y-cómo-contribuir"></a>

## <img src="https://img.shields.io/badge/11-5600EF?style=for-the-badge" height="20" align="absmiddle" /> 11. RAMAS Y CÓMO CONTRIBUIR

El equipo sigue el modelo de trabajo **GitFlow adaptado para microservicios**. La rama `main` se encuentra protegida y solo recibe cambios mediante Pull Requests aprobados.

### Estructura de Ramas Oficial

```text
main                    <-- Código en producción (Desplegado en Azure PROD)
└── develop             <-- Rama de integración continua (Staging / QA)
    ├── feature/SR-<ID>-<descripcion-corta>   <-- Nuevas funcionalidades
    ├── bugfix/SR-<ID>-<descripcion-corta>    <-- Corrección de defectos
    └── hotfix/<descripcion-corta>            <-- Parche urgente a main
```

### Convención de Commits (*Conventional Commits*)

Todo mensaje de commit debe seguir el estándar:

```text
<tipo>(<alcance>): <descripción imperativa en español> [#SR-RF-XX]

Ejemplos válidos:
feat(catalogo): agregar filtro de prendas por ocasión y estilo [#SR-RF-13]
fix(inventario): corregir condición de carrera en reserva de talla [#SR-RF-48]
test(dominio): agregar pruebas unitarias para entidad Prenda [#SR-RF-50]
docs(readme): actualizar variables de entorno y topología de eventos
```

### Checklist Obligatorio para Pull Requests

- [ ] La rama parte de la versión más reciente de `develop`.
- [ ] El código compila limpiamente con `mvn clean compile`.
- [ ] Todas las pruebas unitarias e integración pasan (`mvn verify`).
- [ ] La cobertura JaCoCo es mayor o igual al 85%.
- [ ] No se introducen credenciales, contraseñas ni tokens en el código fuente.
- [ ] El PR tiene asignado al menos 1 revisor del squad antes del merge.

---

<a id="12-enlace-a-la-épica-en-jira"></a>

## <img src="https://img.shields.io/badge/12-5600EF?style=for-the-badge" height="20" align="absmiddle" /> 12. ENLACE A LA ÉPICA EN JIRA

El seguimiento de requerimientos, evidencias y artefactos de entrega se centraliza en los siguientes enlaces oficiales:

### Enlaces de Gestión, Repositorios y Evidencias

* **Épica Oficial del Microservicio (Jira):** [https://mail-team-hdkr3g6f.atlassian.net/browse/NQS-5](https://mail-team-hdkr3g6f.atlassian.net/browse/NQS-5)
* **Tablero Scrum del Squad (Jira):** [https://mail-team-hdkr3g6f.atlassian.net/jira/software/projects/NQS/boards/1?filter=&groupBy=none](https://mail-team-hdkr3g6f.atlassian.net/jira/software/projects/NQS/boards/1?filter=&groupBy=none)

<div align="center">
  <table width="100%" style="margin: 18px 0; border: 2px dashed #5600EF; border-radius: 8px; background-color: #FBFBFE;">
    <tr>
      <td align="center" style="padding: 22px;">
        <p>
          <img src="https://img.shields.io/badge/ESPACIO_DE_CAPTURA-EVIDENCIA_05-5600EF?style=for-the-badge" alt="Evidencia 05" /><br />
          <strong><font color="#5600EF" size="3">TABLERO SCRUM Y SEGUIMIENTO DE LA ÉPICA EN JIRA</font></strong>
        </p>
        <p>
          <sub>Pega aquí la captura del tablero de Jira mostrando la Épica, User Stories y tareas asignadas al microservicio para el sprint.</sub>
        </p>
        <p>
<!-- Al generar tu captura, descomenta la siguiente línea y guarda la imagen en la ruta indicada: -->
 <img src="docs/evidencias/jira-tablero-epica.png" width="90%" alt="Tablero Jira" /><br />
          <code></code>
        </p>
      </td>
    </tr>
  </table>
</div>

### Requerimientos Funcionales Asignados

| Código RF | Nombre del Requerimiento | Historia en Jira |
|---|---|---|
| `SR-RF-67` | Suscribir almacén aliado | `NQS-33` |
| `SR-RF-68` | Cancelar suscripción de almacén | `NQS-34` |
| `SR-RF-75` | Consultar pagos de suscripciones | `NQS-35` |
| `SR-RF-93` | Pagar suscripción mediante pasarela de pagos | `NQS-52` |
| `SR-RF-94` | Confirmar el estado del pago | `NQS-53` |
| `SR-RF-95` | Renovar suscripción con cobro automático | `NQS-54` |
| `SR-RF-96` | Reintentar pago rechazado | `NQS-55` |
| `SR-RF-97` | Consultar historial de pagos propios | `NQS-56` |
| `SR-RF-98` | Reembolsar pago de suscripción | `NQS-57` |
| `SR-RF-99` | Suspender catálogo por falta de pago | `NQS-58` |
| `SR-RF-100` | Suscribirse a un plan de usuario | `NQS-59` |
| `SR-RF-101` | Cambiar o cancelar plan de usuario | `NQS-60` |
| `SR-RF-102` | Aplicar beneficios según el plan del usuario | `NQS-61` |
| `SR-RF-103` | Volver al plan gratuito por falta de pago | `NQS-62` |
