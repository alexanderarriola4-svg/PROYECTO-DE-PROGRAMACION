package com.oficina.turnos.datos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Garantiza que la base de datos tenga sus tablas. Si la base esta vacia
 * (sin clientes ni servicios) carga los datos iniciales comunes UNA sola vez.
 * Nunca borra ni restablece datos existentes.
 * El mismo esquema esta en sql/crear_y_cargar.sql para crear la base manualmente.
 */
public class InicializadorBD {

    private static final String[] ESQUEMA = {
        """
        CREATE TABLE IF NOT EXISTS clientes (
            codigo   INTEGER PRIMARY KEY CHECK (codigo > 0),
            nombre   TEXT    NOT NULL CHECK (length(trim(nombre)) > 0),
            telefono TEXT    NOT NULL CHECK (length(telefono) = 8 AND telefono NOT GLOB '*[^0-9]*'),
            activo   INTEGER NOT NULL DEFAULT 1 CHECK (activo IN (0, 1))
        )""",
        """
        CREATE TABLE IF NOT EXISTS servicios (
            codigo INTEGER PRIMARY KEY CHECK (codigo > 0),
            nombre TEXT    NOT NULL CHECK (length(trim(nombre)) > 0),
            activo INTEGER NOT NULL DEFAULT 1 CHECK (activo IN (0, 1))
        )""",
        """
        CREATE TABLE IF NOT EXISTS turnos (
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
        )""",
        "CREATE UNIQUE INDEX IF NOT EXISTS ux_turnos_un_pendiente_por_cliente ON turnos (codigo_cliente) WHERE estado = 'PENDIENTE'",
        "CREATE INDEX IF NOT EXISTS ix_turnos_estado_numero ON turnos (estado, numero)",
        "CREATE INDEX IF NOT EXISTS ix_turnos_servicio ON turnos (codigo_servicio)"
    };

    private static final Object[][] CLIENTES_INICIALES = {
        {1001, "Ana Lopez", "55501001"},
        {1002, "Carlos Perez", "55501002"},
        {1003, "Maria Garcia", "55501003"},
        {1004, "Jose Hernandez", "55501004"},
        {1005, "Luisa Morales", "55501005"}
    };

    private static final Object[][] SERVICIOS_INICIALES = {
        {1, "Informacion"},
        {2, "Entrega de documentos"},
        {3, "Actualizacion de datos"}
    };

    private final ConexionSQLite conexionSQLite;

    public InicializadorBD(ConexionSQLite conexionSQLite) {
        this.conexionSQLite = conexionSQLite;
    }

    /** @return true si se cargaron los datos iniciales en esta ejecucion */
    public boolean inicializar() {
        try (Connection conexion = conexionSQLite.abrir()) {
            conexion.setAutoCommit(false);
            try {
                crearTablas(conexion);
                boolean cargarDatos = estaVacia(conexion);
                if (cargarDatos) {
                    cargarDatosIniciales(conexion);
                }
                conexion.commit();
                return cargarDatos;
            } catch (SQLException e) {
                conexion.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new AccesoDatosException("No se pudo inicializar la base de datos: " + e.getMessage(), e);
        }
    }

    private void crearTablas(Connection conexion) throws SQLException {
        try (Statement sentencia = conexion.createStatement()) {
            for (String instruccion : ESQUEMA) {
                sentencia.execute(instruccion);
            }
        }
    }

    private boolean estaVacia(Connection conexion) throws SQLException {
        String sql = "SELECT (SELECT COUNT(*) FROM clientes) + (SELECT COUNT(*) FROM servicios)";
        try (Statement sentencia = conexion.createStatement();
             ResultSet resultado = sentencia.executeQuery(sql)) {
            return resultado.next() && resultado.getInt(1) == 0;
        }
    }

    private void cargarDatosIniciales(Connection conexion) throws SQLException {
        String sqlCliente = "INSERT INTO clientes (codigo, nombre, telefono, activo) VALUES (?, ?, ?, 1)";
        try (PreparedStatement sentencia = conexion.prepareStatement(sqlCliente)) {
            for (Object[] fila : CLIENTES_INICIALES) {
                sentencia.setInt(1, (Integer) fila[0]);
                sentencia.setString(2, (String) fila[1]);
                sentencia.setString(3, (String) fila[2]);
                sentencia.addBatch();
            }
            sentencia.executeBatch();
        }
        String sqlServicio = "INSERT INTO servicios (codigo, nombre, activo) VALUES (?, ?, 1)";
        try (PreparedStatement sentencia = conexion.prepareStatement(sqlServicio)) {
            for (Object[] fila : SERVICIOS_INICIALES) {
                sentencia.setInt(1, (Integer) fila[0]);
                sentencia.setString(2, (String) fila[1]);
                sentencia.addBatch();
            }
            sentencia.executeBatch();
        }
    }
}
