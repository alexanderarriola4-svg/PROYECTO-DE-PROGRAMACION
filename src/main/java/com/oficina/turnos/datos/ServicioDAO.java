package com.oficina.turnos.datos;

import com.oficina.turnos.modelo.Servicio;

import java.util.List;
import java.util.Optional;

/** Contrato de acceso a datos de servicios. */
public interface ServicioDAO {

    void insertar(Servicio servicio);

    /** Actualiza el nombre. El codigo no se modifica. */
    void actualizar(Servicio servicio);

    void cambiarEstado(int codigo, boolean activo);

    Optional<Servicio> buscarPorCodigo(int codigo);

    List<Servicio> listarTodos();
}
