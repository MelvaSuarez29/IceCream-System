# 🍦 IceCream-System

Sistema de gestión para heladería desarrollado en **JavaFX** con conexión a **Supabase** (PostgreSQL) como backend. Permite administrar sabores, stock, vendedores, clientes y notas de venta mediante una aplicación de escritorio con autenticación segura y control de acceso por roles.

---

# 📋 Descripción del proyecto

IceCream-System es una aplicación de escritorio diseñada para automatizar los procesos administrativos y comerciales de una heladería. La aplicación utiliza **JavaFX** para la interfaz gráfica y **Supabase** como plataforma Backend-as-a-Service (BaaS), aprovechando PostgreSQL y su API REST para el almacenamiento y consulta de la información.

El sistema implementa una arquitectura organizada por capas (Model, DAO, Controller y Utils), facilitando el mantenimiento, escalabilidad y reutilización del código.

Las principales funcionalidades incluyen:

- Administración de sabores de helado.
- Gestión del inventario y control de stock.
- Administración de vendedores.
- Registro de clientes.
- Creación y administración de notas de venta.
- Consulta del historial de compras.
- Descarga de comprobantes en formato TXT.
- Autenticación segura mediante BCrypt.
- Control de acceso según el rol del usuario.

---

# ✨ Características principales

- 🔐 Autenticación segura con contraseñas cifradas mediante **BCrypt**.
- 👥 Tres roles de usuario con permisos diferenciados.
- 🍦 Gestión completa de sabores (CRUD).
- 📦 Control de inventario y stock.
- 🧾 Registro, edición y anulación de notas de venta.
- 📄 Descarga de comprobantes en formato `.txt`.
- ☁️ Base de datos en la nube utilizando **Supabase**.
- 🌐 Comunicación mediante API REST.
- 🎨 Interfaz gráfica desarrollada con **JavaFX** y **FXML**.
- 🔄 Manejo de sesión y navegación entre ventanas.

---

# 🛠 Tecnologías utilizadas

| Tecnología | Versión | Propósito |
|------------|----------|-----------|
| Java | 17 o superior | Lenguaje de programación |
| JavaFX | 21.0.6 | Desarrollo de la interfaz gráfica |
| Maven | 3.8+ | Gestión de dependencias |
| Supabase | PostgreSQL | Backend como servicio |
| Gson | 2.11.0 | Serialización y deserialización JSON |
| BCrypt | 0.10.2 | Cifrado de contraseñas |
| HTTP Client | Java 17 | Comunicación mediante API REST |

---

# 📁 Estructura del proyecto

```text
IceCream-System/
│
├── .idea/                     # Configuración del IDE
├── .mvn/                      # Maven Wrapper
│
├── src/
│   └── main/
│
│       ├── java/
│       │
│       │   └── com.heladeria/
│       │       ├── config/
│       │       ├── controllers/
│       │       ├── dao/
│       │       ├── models/
│       │       ├── utils/
│       │       ├── Launcher.java
│       │       ├── MainApp.java
│       │       └── module-info.java
│       │
│       └── resources/
│           └── com.heladeria/
│               ├── img/
│               └── views/
│
├── target/
├── .env
├── .env.example
├── .gitignore
├── mvnw
├── mvnw.cmd
├── pom.xml
└── README.md
```

---

# 🔧 Requisitos previos

Antes de ejecutar el proyecto es necesario disponer de:

- Java JDK 17 o superior.
- Maven instalado o utilizar Maven Wrapper.
- Una cuenta en Supabase.
- Un proyecto creado en Supabase.
- Base de datos PostgreSQL configurada.

---

# ⚙️ Configuración

## 1. Clonar el repositorio

```bash
git clone https://github.com/tuusuario/IceCream-System.git

cd IceCream-System
```

## 2. Configurar el archivo `.env`

Copiar el archivo:

```text
.env.example
```

como:

```text
.env
```

y completar la información:

```env
SUPABASE_URL="https://tusubdominio.supabase.co"

SUPABASE_KEY="tu_anon_key"

SUPABASE_SERVICE_ROLE="tu_service_role_key"
```

Donde:

- **SUPABASE_URL:** URL del proyecto en Supabase.
- **SUPABASE_KEY:** Clave pública (Anon Key).
- **SUPABASE_SERVICE_ROLE:** Clave privada de servicio.

---
```sql
-- Tabla: usuarios

CREATE TABLE usuarios (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nombre TEXT NOT NULL,
    email TEXT UNIQUE NOT NULL,
    password_hash TEXT NOT NULL,
    rol TEXT NOT NULL CHECK (rol IN ('admin','vendedor','cliente')),
    activo BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMPTZ DEFAULT NOW()
);


-- Tabla: sabores


CREATE TABLE sabores (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nombre TEXT NOT NULL,
    descripcion TEXT,
    precio DECIMAL(10,2) NOT NULL,
    stock INTEGER DEFAULT 0,
    activo BOOLEAN DEFAULT TRUE
);

-- Tabla: facturas

CREATE TABLE facturas (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    fecha TIMESTAMPTZ DEFAULT NOW(),
    cliente_id UUID NOT NULL REFERENCES usuarios(id),
    vendedor_id UUID NOT NULL REFERENCES usuarios(id),
    total DECIMAL(10,2) DEFAULT 0,
    activa BOOLEAN DEFAULT TRUE
);

-- Tabla: detalle_factura


CREATE TABLE detalle_factura (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    factura_id UUID NOT NULL REFERENCES facturas(id) ON DELETE CASCADE,
    sabor_id UUID NOT NULL REFERENCES sabores(id),
    cantidad INTEGER NOT NULL CHECK (cantidad > 0),
    subtotal DECIMAL(10,2) NOT NULL
);
```

---

# 🗄️ Modelo de Base de Datos

## Diagrama Entidad-Relación (Representación textual)

```text
┌──────────────┐          ┌──────────────────┐
│   usuarios   │          │    facturas      │
├──────────────┤          ├──────────────────┤
│ id (PK)      │◄─────────│ cliente_id (FK)  │
│ nombre       │          │ vendedor_id(FK)  │
│ email        │          │ fecha            │
│ password     │          │ total            │
│ rol          │          │ activa           │
│ activo       │          └─────────┬────────┘
└──────────────┘                    │
                                    │
                                    │
                          ┌─────────▼────────┐
                          │ detalle_factura  │
                          ├──────────────────┤
                          │ id (PK)          │
                          │ factura_id (FK)  │
                          │ sabor_id (FK)    │
                          │ cantidad         │
                          │ subtotal         │
                          └─────────┬────────┘
                                    │
                                    │
                          ┌─────────▼────────┐
                          │     sabores      │
                          ├──────────────────┤
                          │ id (PK)          │
                          │ nombre           │
                          │ descripcion      │
                          │ precio           │
                          │ stock            │
                          │ activo           │
                          └──────────────────┘
```

---

# 📑 Descripción de tablas

## Tabla `usuarios`

Contiene todos los usuarios registrados en el sistema.

| Campo | Tipo | Descripción |
|-------|------|-------------|
| id | UUID | Identificador único |
| nombre | TEXT | Nombre del usuario |
| email | TEXT | Correo electrónico |
| password_hash | TEXT | Contraseña cifrada con BCrypt |
| rol | TEXT | admin, vendedor o cliente |
| activo | BOOLEAN | Estado del usuario |
| created_at | TIMESTAMPTZ | Fecha de creación |

---

## Tabla `sabores`

Almacena los sabores disponibles para la venta.

| Campo | Tipo | Descripción |
|-------|------|-------------|
| id | UUID | Identificador |
| nombre | TEXT | Nombre del sabor |
| descripcion | TEXT | Descripción |
| precio | DECIMAL | Precio |
| stock | INTEGER | Inventario disponible |
| activo | BOOLEAN | Disponible para la venta |

---

## Tabla `facturas`

Representa las notas de venta realizadas por los vendedores.

| Campo | Tipo | Descripción |
|-------|------|-------------|
| id | UUID | Identificador |
| fecha | TIMESTAMPTZ | Fecha de emisión |
| cliente_id | UUID | Cliente asociado |
| vendedor_id | UUID | Vendedor responsable |
| total | DECIMAL | Total de la venta |
| activa | BOOLEAN | Indica si la nota está anulada o vigente |

---

## Tabla `detalle_factura`

Almacena cada producto vendido dentro de una nota de venta.

| Campo | Tipo | Descripción |
|-------|------|-------------|
| id | UUID | Identificador |
| factura_id | UUID | Nota de venta |
| sabor_id | UUID | Sabor vendido |
| cantidad | INTEGER | Cantidad |
| subtotal | DECIMAL | Cantidad × Precio |

---

# 🔗 Relaciones entre tablas

- Un **cliente** puede tener múltiples notas de venta.
- Un **vendedor** puede generar múltiples notas de venta.
- Una **nota de venta** posee varios detalles.
- Un **sabor** puede aparecer en diferentes notas de venta.

---

# 🚀 Instalación y ejecución

## Usando Maven Wrapper (recomendado)

```bash
./mvnw javafx:run
```

En Windows:

```bash
mvnw.cmd javafx:run
```

## Usando Maven instalado

```bash
mvn clean compile javafx:run
```

## Generar un archivo JAR

```bash
mvn clean package
```

Ejecutar:

```bash
java -jar target/heladeria_app-1.0-SNAPSHOT.jar
```


## 3. Crear las tablas

Ejecutar el siguiente script SQL dentro del editor SQL de Supabase.
