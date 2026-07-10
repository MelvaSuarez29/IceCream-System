-- 1. Tabla de usuarios
create table public.usuarios (
  id uuid not null default gen_random_uuid (),
  nombre character varying(100) not null,
  email character varying(100) not null,
  password_hash character varying(255) not null,
  rol character varying(20) not null,
  activo boolean null default true,
  created_at timestamp with time zone null default now(),
  constraint usuarios_pkey primary key (id),
  constraint usuarios_email_key unique (email),
  constraint usuarios_rol_check check (
    (
      (rol)::text = any (
        (
          array[
            'admin'::character varying,
            'vendedor'::character varying,
            'cliente'::character varying
          ]
        )::text[]
      )
    )
  )
) TABLESPACE pg_default;

-- 2. Tabla de sabores
create table public.sabores (
  id uuid not null default gen_random_uuid (),
  nombre character varying(100) not null,
  descripcion text null,
  precio numeric(10, 2) not null,
  stock integer null default 0,
  activo boolean null default true,
  constraint sabores_pkey primary key (id)
) TABLESPACE pg_default;

-- 3. Tabla de facturas
create table public.facturas (
  id uuid not null default gen_random_uuid (),
  fecha timestamp with time zone null default now(),
  cliente_id uuid null,
  vendedor_id uuid null,
  total numeric(10, 2) not null default 0.00,
  constraint facturas_pkey primary key (id),
  constraint facturas_cliente_id_fkey foreign KEY (cliente_id) references usuarios (id) on delete set null,
  constraint facturas_vendedor_id_fkey foreign KEY (vendedor_id) references usuarios (id) on delete set null
) TABLESPACE pg_default;

-- 4. Tabla de detalle de factura
create table public.detalle_factura (
  id uuid not null default gen_random_uuid (),
  factura_id uuid null,
  sabor_id uuid null,
  cantidad integer not null,
  subtotal numeric(10, 2) not null,
  constraint detalle_factura_pkey primary key (id),
  constraint detalle_factura_factura_id_fkey foreign KEY (factura_id) references facturas (id) on delete CASCADE,
  constraint detalle_factura_sabor_id_fkey foreign KEY (sabor_id) references sabores (id) on delete RESTRICT,
  constraint detalle_factura_cantidad_check check ((cantidad > 0))
) TABLESPACE pg_default;

-- Comentarios opcionales para documentación
comment on table public.usuarios is 'Almacena los usuarios del sistema con sus roles (admin, vendedor, cliente)';
comment on table public.sabores is 'Catálogo de sabores disponibles con precio y stock';
comment on table public.facturas is 'Cabecera de facturas con información de cliente, vendedor y total';
comment on table public.detalle_factura is 'Detalle de cada línea de factura con cantidad y subtotal por sabor';