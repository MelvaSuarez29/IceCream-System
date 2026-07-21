# 🍦 IceCream-System

Sistema de gestión para heladería desarrollado en **JavaFX** con conexión a **Supabase** como backend. Permite administrar sabores, stock, vendedores, clientes y notas de venta mediante una aplicación de escritorio con autenticación y control de acceso por roles.

---

# 📋 Descripción del proyecto

IceCream-System es una aplicación de escritorio orientada a la administración de una heladería. Implementa una arquitectura basada en JavaFX para la interfaz gráfica y Supabase (PostgreSQL + API REST) como backend de almacenamiento.

El sistema permite:

- Gestionar sabores y stock.
- Administrar vendedores.
- Registrar clientes.
- Crear, editar y anular notas de venta.
- Consultar historial de compras.
- Descargar comprobantes en formato TXT.
- Autenticación segura mediante BCrypt.

---

# ✨ Características principales

- 🔐 Autenticación segura con contraseñas cifradas mediante BCrypt.
- 👥 Tres roles de usuario:
  - Administrador
  - Vendedor
  - Cliente
- ☁️ Base de datos en la nube utilizando Supabase.
- 🌐 Comunicación mediante API REST.
- 🎨 Interfaz gráfica desarrollada con JavaFX y FXML.
- 📦 Arquitectura organizada por capas.
- 🔄 Manejo de sesiones de usuario.
- 📄 Exportación de notas de venta en formato TXT.
- 📊 Control automático del stock.

---

# 👥 Roles del sistema

## Administrador

Puede realizar las siguientes acciones:

- Gestionar sabores (CRUD completo).
- Activar y desactivar sabores.
- Actualizar el stock.
- Gestionar vendedores.
- Habilitar y deshabilitar vendedores.
- Eliminar vendedores.

---

## Vendedor

Puede:

- Visualizar sabores disponibles.
- Consultar stock.
- Registrar nuevos clientes.
- Crear notas de venta.
- Editar notas activas.
- Anular notas de venta.
- Consultar historial de ventas.

---

## Cliente

Puede:

- Consultar sus notas de venta.
- Ver el detalle de cada compra.
- Descargar las notas en formato `.txt`.

---

# 🛠 Tecnologías utilizadas

| Tecnología | Versión |
|------------|----------|
| Java | 17 o superior |
| JavaFX | 21.0.6 |
| Maven | Última estable |
| Supabase | PostgreSQL + REST API |
| Gson | 2.11.0 |
| BCrypt | 0.10.2 |
| Java HTTP Client | Incluido en Java |

---

# 📁 Estructura del proyecto

```text
IceCream-System/
│
├── .idea/
├── .mvn/
│
├── src/
│   ├── main/
│   │
│   ├── java/
│   │   └── com.heladeria/
│   │       ├── config/
│   │       ├── controllers/
│   │       ├── dao/
│   │       ├── models/
│   │       ├── utils/
│   │       ├── Launcher.java
│   │       ├── MainApp.java
│   │       └── module-info.java
│   │
│   └── resources/
│       └── com.heladeria/
│           ├── img/
│           └── views/
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

---

## 2. Configurar las variables de entorno

Copiar:

```text
.env.example
```

como

```text
.env
```

y completar:

```env
SUPABASE_URL="https://tusubdominio.supabase.co"
SUPABASE_KEY="tu_anon_key"
SUPABASE_SERVICE_ROLE="tu_service_role_key"
```

Donde:

- **SUPABASE_URL:** URL del proyecto.
- **SUPABASE_KEY:** Clave pública (Anon Key).
- **SUPABASE_SERVICE_ROLE:** Clave de servicio.

---

## 3. Crear las tablas

Ejecutar el siguiente script SQL en Supabase.

### Tabla usuarios

```sql
CREATE TABLE usuarios (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nombre TEXT NOT NULL,
    email TEXT UNIQUE NOT NULL,
    password_hash TEXT NOT NULL,
    rol TEXT NOT NULL CHECK (rol IN ('admin','vendedor','cliente')),
    activo BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMPTZ DEFAULT NOW()
);
```

---

### Tabla sabores

```sql
CREATE TABLE sabores (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nombre TEXT NOT NULL,
    descripcion TEXT,
    precio DECIMAL(10,2) NOT NULL,
    stock INTEGER DEFAULT 0,
    activo BOOLEAN DEFAULT TRUE
);
```

---

### Tabla facturas

```sql
CREATE TABLE facturas (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    fecha TIMESTAMPTZ DEFAULT NOW(),
    cliente_id UUID NOT NULL REFERENCES usuarios(id),
    vendedor_id UUID NOT NULL REFERENCES usuarios(id),
    total DECIMAL(10,2) DEFAULT 0,
    activa BOOLEAN DEFAULT TRUE
);
```

---

### Tabla detalle_factura

```sql
CREATE TABLE detalle_factura (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    factura_id UUID NOT NULL REFERENCES facturas(id) ON DELETE CASCADE,
    sabor_id UUID NOT NULL REFERENCES sabores(id),
    cantidad INTEGER NOT NULL CHECK (cantidad > 0),
    subtotal DECIMAL(10,2) NOT NULL
);
```

---

# 🚀 Instalación y ejecución

## Utilizando Maven Wrapper

Linux y macOS

```bash
./mvnw javafx:run
```

Windows

```bash
mvnw.cmd javafx:run
```

---

## Utilizando Maven instalado

```bash
mvn clean compile javafx:run
```

---

## Generar el JAR

```bash
mvn clean package
```

Ejecutar

```bash
java -jar target/heladeria_app-1.0-SNAPSHOT.jar
```

---

# 🔐 Credenciales iniciales

En el primer inicio del sistema se crea automáticamente un usuario administrador.

| Campo | Valor |
|--------|-------|
| Email | admin@heladeria.com |
| Contraseña | admin123 |

> **Importante:** Se recomienda cambiar la contraseña después del primer inicio.

---

# 📦 Dependencias principales

| Dependencia | Versión | Función |
|-------------|----------|----------|
| JavaFX Controls | 21.0.6 | Componentes gráficos |
| JavaFX FXML | 21.0.6 | Carga de interfaces |
| Gson | 2.11.0 | Conversión JSON |
| BCrypt | 0.10.2 | Cifrado de contraseñas |

---

# 🖼 Capturas de pantalla

Puedes agregar aquí imágenes como:

- Pantalla de Login.
- Panel Administrador.
- Gestión de Sabores.
- Gestión de Vendedores.
- Panel del Vendedor.
- Creación de Nota de Venta.
- Historial del Cliente.

---

# 🤝 Contribuciones

Si deseas contribuir al proyecto:

```bash
Fork

↓

Crear rama

git checkout -b feature/nueva-funcionalidad

↓

Realizar cambios

↓

Commit

git commit -m "Nueva funcionalidad"

↓

Push

git push origin feature/nueva-funcionalidad

↓

Crear Pull Request
```

---

# 📄 Licencia

Este proyecto se distribuye bajo la licencia **MIT**.

---

# 📧 Contacto

Si tienes preguntas, sugerencias o deseas reportar errores, puedes abrir un **Issue** en el repositorio del proyecto.

---

# 🍦 IceCream-System

Aplicación desarrollada con JavaFX y Supabase para la administración integral de una heladería, implementando autenticación segura, control por roles, gestión de inventario y ventas mediante una arquitectura moderna basada en servicios REST.
