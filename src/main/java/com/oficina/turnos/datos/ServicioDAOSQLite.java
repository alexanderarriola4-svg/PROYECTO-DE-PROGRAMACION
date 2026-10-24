package com.oficina.turnos.datos;

import com.oficina.turnos.modelo.Servicio;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Implementacion de ServicioDAO con JDBC sobre SQLite. */
public class ServicioDAOSQLite implements ServicioDAO {

    private final ConexionSQLite conexionSQLite;

    public ServicioDAOSQLite(ConexionSQLite conexionSQLite) {
        this.conexionSQLite = conexionSQLite;
    }

    @Override
    public void insertar(Servicio servicio) {
        String sql = "INSERT INTO servicios (codigo, nombre, activo) VALUES (?, ?, ?)";
        try (Connection conexion = conexionSQLite.abrir();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setInt(1, servicio.getCodigo());
            sentencia.setString(2, servicio.getNombre());
            sentencia.setInt(3, servicio.isActivo() ? 1 : 0);
            sentencia.executeUpdate();
        } catch (SQLException e) {
            throw new AccesoDatosException("No se pudo registrar el servicio " + servicio.getCodigo() + ": " + e.getMessage(), e);
        }
    }

    @Override
    public void actualizar(Servicio servicio) {
        String sql = "UPDATE servicios SET nombre = ? WHERE codigo = ?";
        try (Connection conexion = conexionSQLite.abrir();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setString(1, servicio.getNombre());
            sentencia.setInt(2, servicio.getCodigo());
            verificarUnaFila(sentencia.executeUpdate(), servicio.getCodigo());
        } catch (SQLException e) {
            throw new AccesoDatosException("No se pudo actualizar el servicio " + servicio.getCodigo() + ": " + e.getMessage(), e);
        }
    }

    @Override
    public void cambiarEstado(int codigo, boolean activo) {
        String sql = "UPDATE servicios SET activo = ? WHERE codigo = ?";
        try (Connection conexion = conexionSQLite.abrir();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setInt(1, activo ? 1 : 0);
            sentencia.setInt(2, codigo);
            verificarUnaFila(sentencia.executeUpdate(), codigo);
        } catch (SQLException e) {
            throw new AccesoDatosException("No se pudo cambiar el estado del servicio " + codigo + ": " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Servicio> buscarPorCodigo(int codigo) {
        String sql = "SELECT codigo, nombre, activo FROM servicios WHERE codigo = ?";
        try (Connection conexion = conexionSQLite.abrir();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setInt(1, codigo);
            try (ResultSet resultado = sentencia.executeQuery()) {
                return resultado.next() ? Optional.of(mapear(resultado)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new AccesoDatosException("No se pudo consultar el servicio " + codigo + ": " + e.getMessage(), e);
        }
    }

    @Override
    public List<Servicio> listarTodos() {
        String sql = "SELECT codigo, nombre, activo FROM servicios ORDER BY codigo";
        List<Servicio> servicios = new ArrayList<>();
        try (Connection conexion = conexionSQLite.abrir();
             PreparedStatement sentencia = conexion.prepareStatement(sql);
             ResultSet resultado = sentencia.executeQuery()) {
            while (resultado.next()) {
                servicios.add(mapear(resultado));
            }
            return servicios;
        } catch (SQLException e) {
            throw new AccesoDatosException("No se pudieron consultar los servicios: " + e.getMessage(), e);
        }
    }

    private Servicio mapear(ResultSet resultado) throws SQLException {
        return new Servicio(
                resultado.getInt("codigo"),
                resultado.getString("nombre"),
                resultado.getInt("activo") == 1);
    }

    private void verificarUnaFila(int filasAfectadas, int codigo) {
        if (filasAfectadas != 1) {
            throw new AccesoDatosException("El servicio " + codigo + " no existe en la base de datos.");
        }
    }
}
