package com.oficina.turnos.datos;

import com.oficina.turnos.modelo.Cliente;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Implementacion de ClienteDAO con JDBC sobre SQLite. */
public class ClienteDAOSQLite implements ClienteDAO {

    private final ConexionSQLite conexionSQLite;

    public ClienteDAOSQLite(ConexionSQLite conexionSQLite) {
        this.conexionSQLite = conexionSQLite;
    }

    @Override
    public void insertar(Cliente cliente) {
        String sql = "INSERT INTO clientes (codigo, nombre, telefono, activo) VALUES (?, ?, ?, ?)";
        try (Connection conexion = conexionSQLite.abrir();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setInt(1, cliente.getCodigo());
            sentencia.setString(2, cliente.getNombre());
            sentencia.setString(3, cliente.getTelefono());
            sentencia.setInt(4, cliente.isActivo() ? 1 : 0);
            sentencia.executeUpdate();
        } catch (SQLException e) {
            throw new AccesoDatosException("No se pudo registrar el cliente " + cliente.getCodigo() + ": " + e.getMessage(), e);
        }
    }

    @Override
    public void actualizar(Cliente cliente) {
        String sql = "UPDATE clientes SET nombre = ?, telefono = ? WHERE codigo = ?";
        try (Connection conexion = conexionSQLite.abrir();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setString(1, cliente.getNombre());
            sentencia.setString(2, cliente.getTelefono());
            sentencia.setInt(3, cliente.getCodigo());
            verificarUnaFila(sentencia.executeUpdate(), cliente.getCodigo());
        } catch (SQLException e) {
            throw new AccesoDatosException("No se pudo actualizar el cliente " + cliente.getCodigo() + ": " + e.getMessage(), e);
        }
    }

    @Override
    public void cambiarEstado(int codigo, boolean activo) {
        String sql = "UPDATE clientes SET activo = ? WHERE codigo = ?";
        try (Connection conexion = conexionSQLite.abrir();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setInt(1, activo ? 1 : 0);
            sentencia.setInt(2, codigo);
            verificarUnaFila(sentencia.executeUpdate(), codigo);
        } catch (SQLException e) {
            throw new AccesoDatosException("No se pudo cambiar el estado del cliente " + codigo + ": " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Cliente> buscarPorCodigo(int codigo) {
        String sql = "SELECT codigo, nombre, telefono, activo FROM clientes WHERE codigo = ?";
        try (Connection conexion = conexionSQLite.abrir();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setInt(1, codigo);
            try (ResultSet resultado = sentencia.executeQuery()) {
                return resultado.next() ? Optional.of(mapear(resultado)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new AccesoDatosException("No se pudo consultar el cliente " + codigo + ": " + e.getMessage(), e);
        }
    }

    @Override
    public List<Cliente> listarTodos() {
        String sql = "SELECT codigo, nombre, telefono, activo FROM clientes ORDER BY codigo";
        List<Cliente> clientes = new ArrayList<>();
        try (Connection conexion = conexionSQLite.abrir();
             PreparedStatement sentencia = conexion.prepareStatement(sql);
             ResultSet resultado = sentencia.executeQuery()) {
            while (resultado.next()) {
                clientes.add(mapear(resultado));
            }
            return clientes;
        } catch (SQLException e) {
            throw new AccesoDatosException("No se pudieron consultar los clientes: " + e.getMessage(), e);
        }
    }

    private Cliente mapear(ResultSet resultado) throws SQLException {
        return new Cliente(
                resultado.getInt("codigo"),
                resultado.getString("nombre"),
                resultado.getString("telefono"),
                resultado.getInt("activo") == 1);
    }

    private void verificarUnaFila(int filasAfectadas, int codigo) {
        if (filasAfectadas != 1) {
            throw new AccesoDatosException("El cliente " + codigo + " no existe en la base de datos.");
        }
    }
}
