-- =====================================================================
--  Sistema de Atencion de Turnos - Programacion II
--  Crea la base de datos desde cero y carga los datos iniciales comunes.
--
--  Uso (desde la carpeta raiz del proyecto):
--      sqlite3 data/turnos.db < sql/crear_y_cargar.sql
--  o abriendo este archivo en DB Browser for SQLite > Ejecutar SQL.
--
--  ATENCION: elimina las tablas existentes. Uselo solo para crear una
--  base NUEVA. La aplicacion Java nunca ejecuta este script ni borra datos.
-- =====================================================================

PRAGMA foreign_keys = ON;

BEGIN TRANSACTION;

DROP TABLE IF EXISTS turnos;
DROP TABLE IF EXISTS clientes;
DROP TABLE IF EXISTS servicios;

-- ---------------------------------------------------------------------
-- Clientes: el codigo es la clave primaria (y la clave del BST).
-- ---------------------------------------------------------------------
CREATE TABLE clientes (
    codigo   INTEGER PRIMARY KEY CHECK (codigo > 0),
    nombre   TEXT    NOT NULL CHECK (length(trim(nombre)) > 0),
    telefono TEXT    NOT NULL CHECK (length(telefono) = 8 AND telefono NOT GLOB '*[^0-9]*'),
    activo   INTEGER NOT NULL DEFAULT 1 CHECK (activo IN (0, 1))
);

-- ---------------------------------------------------------------------
-- Servicios
-- ---------------------------------------------------------------------
CREATE TABLE servicios (
    codigo INTEGER PRIMARY KEY CHECK (codigo > 0),
    nombre TEXT    NOT NULL CHECK (length(trim(nombre)) > 0),
    activo INTEGER NOT NULL DEFAULT 1 CHECK (activo IN (0, 1))
);

-- ---------------------------------------------------------------------
-- Turnos: AUTOINCREMENT garantiza que un numero nunca se reutilice.
-- Claves foraneas hacia clientes y servicios.
-- ---------------------------------------------------------------------
CREATE TABLE turnos (
    numero            INTEGER PRIMARY KEY AUTOINCREMENT,
    codigo_cliente    INTEGER NOT NULL REFERENCES clientes (codigo) ON UPDATE RESTRICT ON DELETE RESTRICT,
    codigo_servicio   INTEGER NOT NULL REFERENCES servicios (codigo) ON UPDATE RESTRICT ON DELETE RESTRICT,
    fecha_generacion  TEXT    NOT NULL,
    estado            TEXT    NOT NULL DEFAULT 'PENDIENTE' CHECK (estado IN ('PENDIENTE', 'ATENDIDO', 'CANCELADO')),
    fecha_atencion    TEXT,
    fecha_cancelacion TEXT,
    CHECK (
        (estado = 'PENDIENTE' AND fecha_atencion IS NULL     AND fecha_cancelacion IS NULL) OR
        (estado = 'ATENDIDO'  AND fecha_atencion IS NOT NULL AND fecha_cancelacion IS NULL) OR
        (estado = 'CANCELADO' AND fecha_atencion IS NULL     AND fecha_cancelacion IS NOT NULL)
    )
);

-- Un cliente solo puede tener un turno PENDIENTE a la vez (refuerza la regla de negocio).
CREATE UNIQUE INDEX ux_turnos_un_pendiente_por_cliente ON turnos (codigo_cliente) WHERE estado = 'PENDIENTE';
CREATE INDEX ix_turnos_estado_numero ON turnos (estado, numero);
CREATE INDEX ix_turnos_servicio ON turnos (codigo_servicio);

-- ---------------------------------------------------------------------
-- Datos iniciales comunes (sin turnos)
-- ---------------------------------------------------------------------
INSERT INTO clientes (codigo, nombre, telefono, activo) VALUES
    (1001, 'Ana Lopez',      '55501001', 1),
    (1002, 'Carlos Perez',   '55501002', 1),
    (1003, 'Maria Garcia',   '55501003', 1),
    (1004, 'Jose Hernandez', '55501004', 1),
    (1005, 'Luisa Morales',  '55501005', 1);

INSERT INTO servicios (codigo, nombre, activo) VALUES
    (1, 'Informacion',            1),
    (2, 'Entrega de documentos',  1),
    (3, 'Actualizacion de datos', 1);

COMMIT;
