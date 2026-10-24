package com.oficina.turnos.datos;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Abre conexiones JDBC a la base SQLite. La ruta es relativa a la carpeta
 * desde donde se ejecuta el programa (la raiz del proyecto): data/turnos.db.
 * Cada conexion habilita las claves foraneas, que SQLite trae desactivadas.
 */
public class ConexionSQLite {

    public static final Path RUTA_PREDETERMINADA = Path.of("data", "turnos.db");

    private final Path rutaBaseDatos;

    public ConexionSQLite() {
        this(RUTA_PREDETERMINADA);
    }

    public ConexionSQLite(Path rutaBaseDatos) {
        this.rutaBaseDatos = rutaBaseDatos;
    }

    /** Abre una conexion nueva. Quien la pide debe cerrarla (try-with-resources). */
    public Connection abrir() throws SQLException {
        crearCarpetaSiNoExiste();
        Connection conexion = DriverManager.getConnection("jdbc:sqlite:" + rutaBaseDatos.toString().replace('\\', '/'));
        try (Statement sentencia = conexion.createStatement()) {
            sentencia.execute("PRAGMA foreign_keys = ON");
        } catch (SQLException e) {
            conexion.close();
            throw e;
        }
        return conexion;
    }

    private void crearCarpetaSiNoExiste() throws SQLException {
        Path carpeta = rutaBaseDatos.toAbsolutePath().getParent();
        if (carpeta == null) {
            return;
        }
        try {
            Files.createDirectories(carpeta);
        } catch (IOException e) {
            throw new SQLException("No se pudo crear la carpeta " + carpeta + ": " + e.getMessage(), e);
        }
    }

    public Path getRutaBaseDatos() {
        return rutaBaseDatos;
    }
}
