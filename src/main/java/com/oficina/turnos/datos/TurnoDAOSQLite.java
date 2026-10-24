package com.oficina.turnos.datos;

import com.oficina.turnos.modelo.EstadoTurno;
import com.oficina.turnos.modelo.ResumenServicio;
import com.oficina.turnos.modelo.Turno;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Implementacion de TurnoDAO con JDBC sobre SQLite. Las fechas se guardan como texto ISO. */
public class TurnoDAOSQLite implements TurnoDAO {

    private static final DateTimeFormatter FORMATO_BD = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String COLUMNAS =
            "numero, codigo_cliente, codigo_servicio, fecha_generacion, estado, fecha_atencion, fecha_cancelacion";

    private final ConexionSQLite conexionSQLite;

    public TurnoDAOSQLite(ConexionSQLite conexionSQLite) {
        this.conexionSQLite = conexionSQLite;
    }

    @Override
    public Turno insertar(int codigoCliente, int codigoServicio, LocalDateTime fechaGeneracion) {
        String sql = "INSERT INTO turnos (codigo_cliente, codigo_servicio, fecha_generacion, estado) VALUES (?, ?, ?, 'PENDIENTE')";
        try (Connection conexion = conexionSQLite.abrir();
             PreparedStatement sentencia = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            sentencia.setInt(1, codigoCliente);
            sentencia.setInt(2, codigoServicio);
            sentencia.setString(3, FORMATO_BD.format(fechaGeneracion));
            sentencia.executeUpdate();
            try (ResultSet claves = sentencia.getGeneratedKeys()) {
                if (!claves.next()) {
                    throw new AccesoDatosException("La base de datos no devolvio el numero del turno.");
                }
                return Turno.nuevoPendiente(claves.getInt(1), codigoCliente, codigoServicio, fechaGeneracion);
            }
        } catch (SQLException e) {
            throw new AccesoDatosException("No se pudo guardar el turno: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean marcarAtendido(int numero, LocalDateTime fechaAtencion) {
        String sql = "UPDATE turnos SET estado = 'ATENDIDO', fecha_atencion = ? WHERE numero = ? AND estado = 'PENDIENTE'";
        return cerrarTurno(sql, numero, fechaAtencion, "atender");
    }

    @Override
    public boolean marcarCancelado(int numero, LocalDateTime fechaCancelacion) {
        String sql = "UPDATE turnos SET estado = 'CANCELADO', fecha_cancelacion = ? WHERE numero = ? AND estado = 'PENDIENTE'";
        return cerrarTurno(sql, numero, fechaCancelacion, "cancelar");
    }

    private boolean cerrarTurno(String sql, int numero, LocalDateTime fecha, String accion) {
        try (Connection conexion = conexionSQLite.abrir();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setString(1, FORMATO_BD.format(fecha));
            sentencia.setInt(2, numero);
            return sentencia.executeUpdate() == 1;
        } catch (SQLException e) {
            throw new AccesoDatosException("No se pudo " + accion + " el turno " + numero + ": " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Turno> buscarPorNumero(int numero) {
        String sql = "SELECT " + COLUMNAS + " FROM turnos WHERE numero = ?";
        List<Turno> turnos = consultar(sql, numero);
        return turnos.isEmpty() ? Optional.empty() : Optional.of(turnos.get(0));
    }

    @Override
    public List<Turno> listarPorEstado(EstadoTurno estado) {
        String sql = "SELECT " + COLUMNAS + " FROM turnos WHERE estado = ? ORDER BY numero";
        return consultar(sql, estado.name());
    }

    @Override
    public List<Turno> listarPorCliente(int codigoCliente) {
        String sql = "SELECT " + COLUMNAS + " FROM turnos WHERE codigo_cliente = ? ORDER BY numero";
        return consultar(sql, codigoCliente);
    }

    @Override
    public int contarPendientesPorCliente(int codigoCliente) {
        return contar("SELECT COUNT(*) FROM turnos WHERE codigo_cliente = ? AND estado = 'PENDIENTE'", codigoCliente);
    }

    @Override
    public int contarPendientesPorServicio(int codigoServicio) {
        return contar("SELECT COUNT(*) FROM turnos WHERE codigo_servicio = ? AND estado = 'PENDIENTE'", codigoServicio);
    }

    @Override
    public List<ResumenServicio> resumenPorServicio() {
        // LEFT JOIN: los servicios sin turnos tambien aparecen, con cantidades en cero.
        String sql = """
                SELECT s.codigo, s.nombre,
                       COALESCE(SUM(CASE WHEN t.estado = 'PENDIENTE' THEN 1 ELSE 0 END), 0) AS pendientes,
                       COALESCE(SUM(CASE WHEN t.estado = 'ATENDIDO'  THEN 1 ELSE 0 END), 0) AS atendidos,
                       COALESCE(SUM(CASE WHEN t.estado = 'CANCELADO' THEN 1 ELSE 0 END), 0) AS cancelados
                FROM servicios s
                LEFT JOIN turnos t ON t.codigo_servicio = s.codigo
                GROUP BY s.codigo, s.nombre
                ORDER BY s.codigo""";
        List<ResumenServicio> resumen = new ArrayList<>();
        try (Connection conexion = conexionSQLite.abrir();
             PreparedStatement sentencia = conexion.prepareStatement(sql);
             ResultSet resultado = sentencia.executeQuery()) {
            while (resultado.next()) {
                resumen.add(new ResumenServicio(
                        resultado.getInt("codigo"),
                        resultado.getString("nombre"),
                        resultado.getInt("pendientes"),
                        resultado.getInt("atendidos"),
                        resultado.getInt("cancelados")));
            }
            return resumen;
        } catch (SQLException e) {
            throw new AccesoDatosException("No se pudo generar el resumen por servicio: " + e.getMessage(), e);
        }
    }

    private List<Turno> consultar(String sql, Object parametro) {
        List<Turno> turnos = new ArrayList<>();
        try (Connection conexion = conexionSQLite.abrir();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setObject(1, parametro);
            try (ResultSet resultado = sentencia.executeQuery()) {
                while (resultado.next()) {
                    turnos.add(mapear(resultado));
                }
            }
            return turnos;
        } catch (SQLException e) {
            throw new AccesoDatosException("No se pudieron consultar los turnos: " + e.getMessage(), e);
        }
    }

    private int contar(String sql, int parametro) {
        try (Connection conexion = conexionSQLite.abrir();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setInt(1, parametro);
            try (ResultSet resultado = sentencia.executeQuery()) {
                return resultado.next() ? resultado.getInt(1) : 0;
            }
        } catch (SQLException e) {
            throw new AccesoDatosException("No se pudieron contar los turnos: " + e.getMessage(), e);
        }
    }

    private Turno mapear(ResultSet resultado) throws SQLException {
        return new Turno(
                resultado.getInt("numero"),
                resultado.getInt("codigo_cliente"),
                resultado.getInt("codigo_servicio"),
                aFecha(resultado.getString("fecha_generacion")),
                EstadoTurno.valueOf(resultado.getString("estado")),
                aFecha(resultado.getString("fecha_atencion")),
                aFecha(resultado.getString("fecha_cancelacion")));
    }

    private static LocalDateTime aFecha(String texto) {
        return texto == null ? null : LocalDateTime.parse(texto, FORMATO_BD);
    }
}
