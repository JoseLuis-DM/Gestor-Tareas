# Gestor de Tareas

Este proyecto es un **Gestor de Tareas** desarrollado con **Spring Boot 3**, **Spring Security** y **JWT**, que permite a los usuarios autenticarse y gestionar sus tareas de manera segura.  
Además, incluye **monitorización completa** mediante **Prometheus**, **Grafana**, **Loki** y **Promtail**, lo que facilita la observación de métricas y logs en tiempo real.

---

## Tecnologías Utilizadas

###  Backend
-  **Java 17**
-  **Spring Boot 3**
-  **Spring Security con JWT**
-  **Spring Data JPA**
-  **MySQL**
-  **Maven**
-  **JUnit y Mockito** (pruebas unitarias e integrales)
-  **JaCoCo** (cobertura de pruebas)

###  Contenerización y Observabilidad
-  **Docker & Docker Compose**
-  **Prometheus** (métricas)
-  **Grafana** (dashboards)
-  **Loki + Promtail** (recolección y visualización de logs)

---

##  Funcionalidades
 Registro de usuarios  
 Login con autenticación JWT  
 CRUD de tareas (crear, leer, actualizar, eliminar)  
 Roles de usuario (`USER`, `ADMIN`)  
 Seguridad en endpoints (solo usuarios autenticados pueden gestionar tareas)  
 Documentación de API con **Swagger**
 Manejo centralizado de excepciones  
 Monitoreo de métricas y logs con Grafana

---

## Pruebas y Calidad de Código

<details>
<summary>Ver detalles de pruebas</summary>

- Se realizaron **pruebas unitarias e integrales** con **JUnit** y **Mockito**.
- La cobertura de pruebas se mide con **JaCoCo**, asegurando que el código esté testeado.
- Los reportes de JaCoCo pueden generarse con:

```bash
mvn clean verify
```
- Una vez generados, el reporte lo puedes encontrar en:
```bash
target/site/jacoco/index.html
```
Puedes abrirlo en tu navegador para ver visualmente qué clases y que métodos están cubiertos por pruebas.
</details>

---

## ️ Integración Continua (CI) con GitHub Actions

Este proyecto cuenta con **Integración Continua (CI)** configurada mediante **GitHub Actions**, lo que permite ejecutar 
automaticamente pruebas y validaciones cada vez que se hace un **push o pull request** a la rama `main`. 

###  Pipeline de CI

El flujo de CI realiza los siguientes pasos:
- Configura el entorno de ejecución con **JDK 17**
- Compila el proyecto con **Maven** (`mvn clean verify`)
- Ejecuta todas las pruebas unitarias e integrales usando **Spring Boot** y una base de datos **H2 en memoria**
- Genera los reportes de **JaCoCo** y **Surefire**
  - Sube los reportes y logs generados como artefactos en GitHub

 **Archivo del pipeline:**
.github/workflows/ci.yml

###  Estado del build

![Build Status](https://github.com/JoseLuis-DM/Gestor-Tareas/actions/workflows/ci.yml/badge.svg)

> Cada vez que se hace un cambio en la rama `main`, este badge se actualizará automáticamente mostrando si el último build fue exitoso (`✅`) o fallido (`❌`).

---

##  Estructura del Proyecto

```
Gestor-Tareas/
├─ src/                      <- Código fuente de Spring Boot
│   ├─ main/
│   └─ test/
├─ monitoring/
│   ├─ prometheus.yml
│   ├─ loki-config.yml
│   ├─ prontail-config.yml
│   └─ spring-boot-dashboard.json
├─ pom.xml                   <- Proyecto Maven
├─ README.md
├─ Dockerfile                <- Dockerfile del backend
├─ docker-compose.yml        <- Docker Compose para levantar app + MySQL
└─ .gitignore
```

---

##  Levantar el Proyecto con Docker

<details>
<summary>Instrucciones para levantar con Docker</summary>

1. Clonar el repositorio:

```bash
git clone https://github.com/JoseLuis-DM/Gestor-Tareas.git
cd Gestor-Tareas
```

2. Verificar que los archivos **docker-compose.yml**, **Dockerfile** y **variables.env** estén en la misma carpeta de la raíz del proyecto (**Gestor-Tareas**).

3. Crear un archivo **variables.env** en la raíz del proyecto con tus credenciales:

```env
SPRING_DATASOURCE_URL=jdbc:mysql://gestor-db:3306/gestorbd
SPRING_DATASOURCE_USERNAME=root
SPRING_DATASOURCE_PASSWORD=tu_contraseña
JWT_SECRET=secret_key
```

>  **No subir `variables.env` al repositorio**, es privado.

4. Levantar los contenedores:

```bashx
docker compose up --build
```

- La aplicación estará disponible en **http://localhost:8080**
- La base de datos MySQL estará en **http://localhost:3307**
- La documentación de la API con Swagger estará disponible en **http://localhost:8080/swagger-ui/index.html**
- Prometheus en **http://localhost:9090**
- Grafana en **http://localhost:3000**
- Loki en **http://localhost:3100**

6. Detener los contenedores
```bash
docker compose down
```

</details>

---

##  Monitoreo con Grafana, Prometheus y Loki

El proyecto incluye un entorno completo de **monitorización y logging** en tiempo real con **Grafana**, **Prometheus** y **Loki**.

###  Servicios incluidos en `docker-compose.yml`

- **Prometheus** → Recolecta métricas desde `/actuator/prometheus`
- **Loki** → Almacena los logs de la aplicación
- **Promtail** → Envía los logs de Docker a Loki
- **Grafana** → Visualiza métricas y logs en un dashboard preconfigurado

###  Dashboard incluido

El dashboard **“Spring Boot - Gestor de Tareas”** ya está configurado y disponible dentro de Grafana.  
Este muestra automáticamente:

-  **Requests por segundo**
-  **Tiempo promedio de respuesta (ms)**
-  **Uso de memoria JVM (MB)**
-  **Uso de CPU (%)**
-  **Threads activos JVM**
-  **Errores HTTP (4xx y 5xx)**
-  **Logs de la aplicación (Loki)**

 **Acceso a Grafana**

- URL: [http://localhost:3000](http://localhost:3000)
- Usuario: `admin`
- Contraseña: `admin`

El dashboard se actualiza cada **10 segundos** y utiliza **Prometheus** para las métricas y **Loki** para los logs.

---

##  Notas

- Asegúrate de que **Docker** y **Docker Compose** estén instalados en tu máquina.
- Las variables sensibles se deben mantener en **variables.env** y **nunca se deben subir** al repositorio.
- Para desarrollo local, puedes crear un **variables.env.example** como plantilla para otros colaboradores.
- Las métricas y logs se reinician cada vez que se eliminan los contenedores.