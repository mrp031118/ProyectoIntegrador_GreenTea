# Green Tea – Sistema Web de Inventario, Ventas y Producción

Sistema web desarrollado con **Spring Boot**, **Java 17**, **Thymeleaf** y **MySQL**, implementado para optimizar la gestión de inventario, ventas, producción diaria y análisis estadístico en la cafetería **Green Tea**, siguiendo el método **Kardex PEPS**.

Este proyecto forma parte del Trabajo Integrador UTP (2025).

---

## Características principales

- **Gestión completa de inventario**
  - Insumos, productos elaborados y categorías
  - Control de lotes y vencimientos
  - Kardex PEPS automático
- **Módulo de ventas**
  - Actualización del inventario en tiempo real
  - Registro de método de pago 
- **Producción diaria**
  - Registro de productos elaborados y consumo de insumos
- **Mermas y desperdicios**
  - Descuentos automáticos del inventario
- **Gestión de proveedores y clientes**
- **Alertas**
  - Stock crítico
  - Insumos próximos a vencer
- **Dashboards**
  - Administrador
  - Empleado
- **Seguridad**
  - Roles: Administrador / Empleado
  - Contraseñas encriptadas
- **Reportes**
  - Visualización gráfica de ventas, productos más vendidos y stock crítico

---

## Tecnologías utilizadas

### Backend
- Java 17  
- Spring Boot 3.x  
- Spring Data JPA  
- Spring Security  
- Hibernate  
- Lombok  
- MySQL Connector  
- DevTools  

### Frontend
- Thymeleaf  
- HTML5  
- CSS3  
- JavaScript  

### Base de datos
- MySQL 8.0  

---

## Requisitos previos

| Herramienta | Versión recomendada |
|-------------|---------------------|
| Java | 17+ |
| Maven | 3.6+ |
| MySQL | 8.0 |
| MySQL Workbench | Opcional |
| Git | Última versión |
| VS Code / IntelliJ | Recomendado |

---

# Instalación y Configuración

## 1️. Clonar el repositorio

```bash
git clone https://github.com/tu-usuario/green-tea.git
cd green-tea
git clone https://github.com/tu-usuario/green-tea.git
cd green-tea
```

## 2. Crear la base de datos en MySQL
```bash
CREATE DATABASE bd_coffee_nueva;
USE bd_coffee_nueva;
```

## 3. Configurar el archivo application.properties
```bash
spring.application.name=demo
server.port=8081

# Base de datos
spring.datasource.url=jdbc:mysql://localhost:3306/bd_coffee_nueva?useSSL=false&serverTimezone=UTC
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.datasource.username=root
spring.datasource.password=

logging.level.com.example.demo=DEBUG

# Pool HikariCP
spring.datasource.hikari.maximum-pool-size=10
spring.datasource.hikari.minimum-idle=3
spring.datasource.hikari.idle-timeout=60000
spring.datasource.hikari.max-lifetime=1800000
spring.datasource.hikari.keepalive-time=60000
spring.datasource.hikari.connection-timeout=20000

# JPA
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
```

## 4. Compilar y ejecutar 
```bash
mvn clean install
mvn spring-boot:run
```
El proyecto inicia en: [http://localhost:8081/login](http://localhost:8081/login)

## Estructura del Proyecto

```txt
src/
 └── main/
     ├── java/com/example/demo/
     │   ├── controller/     # Controladores de la aplicación
     │   ├── dto/            # Objetos de transferencia de datos
     │   ├── entity/         # Entidades JPA
     │   ├── repository/     # Repositorios JPA
     │   ├── security/       # Configuraciones de seguridad
     │   └── service/        # Servicios de negocio
     └── resources/          # Recursos (templates, archivos estáticos, etc.)
         ├── templates/
         │   ├── admin/
         │   └── empleado/
         ├── static/
         └── application.properties
```
## Acceso al sistema

### Rol Administrador

Acceso a:

- Insumos
- Productos
- Recetas
- Proveedores
- Clientes
- Lotes
- Usuarios
- Reportes
- Movimientos
- Dashboard Administrador

---

### Rol Empleado

Acceso a:

- Registrar ventas
- Producción diaria
- Registro de mermas
- Verificar disponibilidad
- Dashboard Empleado

---

## Módulos principales

### Gestión de Inventario
- Insumos  
- Productos
- Recetas 
- Categorías  
- Kardex PEPS  
- Lotes y vencimientos  

### Producción Diaria
- Registro de productos elaborados  
- Consumo automático de insumos  

### Ventas
- Registro de ventas  
- Actualización del stock en tiempo real  
- Historial consultable  

### Mermas
- Registro de mermas  
- Ajuste automático del inventario  

### Proveedores y Clientes
- CRUD completo  

### Reportes
- Ventas del día  
- Stock crítico  
- Productos más vendidos  
- Mermas  

---






