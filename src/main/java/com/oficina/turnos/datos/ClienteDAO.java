package com.oficina.turnos.datos;

import com.oficina.turnos.modelo.Cliente;

import java.util.List;
import java.util.Optional;

/**
 * Contrato de acceso a datos de clientes. La logica de negocio depende de
 * esta interfaz y no de SQLite directamente (inversion de dependencias).
 */
public interface ClienteDAO {

    void insertar(Cliente cliente);

    /** Actualiza nombre y telefono. El codigo no se modifica. */
    void actualizar(Cliente cliente);

    void cambiarEstado(int codigo, boolean activo);

    Optional<Cliente> buscarPorCodigo(int codigo);

    List<Cliente> listarTodos();
}
