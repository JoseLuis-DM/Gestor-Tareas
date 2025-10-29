# 📝 Gestor de Tareas - Spring Boot 3

Este proyecto es un **Gestor de Tareas** desarrollado con **Spring Boot 3**, **Spring Security** y **JWT**, que permite a los usuarios autenticarse y gestionar sus tareas de manera segura.

---

## 🚀 Tecnologías Utilizadas
- 🟢 **Java 17**
- 🟢 **Spring Boot 3**
- 🟢 **Spring Security con JWT**
- 🟢 **Spring Data JPA**
- 🟢 **MySQL**
- 🟢 **Maven**
- 🧪 **JUnit y Mockito** (pruebas unitarias e integrales)
- 📊 **JaCoCo** (cobertura de pruebas)

---

## 📌 Funcionalidades
✅ Registro de usuarios  
✅ Login con autenticación JWT  
✅ CRUD de tareas (crear, leer, actualizar, eliminar)  
✅ Roles de usuario (`USER`, `ADMIN`)  
✅ Seguridad en endpoints (solo usuarios autenticados pueden gestionar tareas)  
✅ Documentación de API con **Swagger**

---

## 🧪 Pruebas y Calidad de Código

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

## 📂 Estructura del Proyecto

```
Gestor-Tareas/
├─ src/                      <- Código fuente de Spring Boot
│   ├─ main/
│   └─ test/
├─ pom.xml                   <- Proyecto Maven
├─ README.md
├─ Dockerfile                <- Dockerfile del backend
├─ docker-compose.yml        <- Docker Compose para levantar app + MySQL
└─ .gitignore
```

---

## 🐳 Levantar el Proyecto con Docker

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
JWT_SECRET=tu_secreto
```

> ⚠️ **No subir `variables.env` al repositorio**, es privado.

4. Levantar los contenedores:

```bashx
docker compose up --build
```

- La aplicación estará disponible en **http://localhost:8080**
- La base de datos MySQL estará en **http://localhost:3307**
- La documentación de la API con Swagger estará disponible en **http://localhost:8080/swagger-ui/index.html**

</details>

---

## 📝 Notas

- Asegúrate de que **Docker** y **Docker Compose** estén instalados en tu máquina.
- Las variables sensibles se deben mantener en **variables.env** y **nunca se deben subir** al repositorio.
- Para desarrollo local, puedes crear un **variables.env.example** como plantilla para otros colaboradores.