package com.oficina.turnos.datos;

import com.oficina.turnos.modelo.EstadoTurno;
import com.oficina.turnos.modelo.ResumenServicio;
import com.oficina.turnos.modelo.Turno;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/** Contrato de acceso a datos de turnos y de los reportes que dependen de ellos. */
public interface TurnoDAO {

    /** Inserta un turno PENDIENTE y devuelve el turno con el numero generado por la base. */
    Turno insertar(int codigoCliente, int codigoServicio, LocalDateTime fechaGeneracion);

    /** Pasa un turno de PENDIENTE a ATENDIDO. Devuelve false si el turno no estaba pendiente. */
    boolean marcarAtendido(int numero, LocalDateTime fechaAtencion);

    /** Pasa un turno de PENDIENTE a CANCELADO. Devuelve false si el turno no estaba pendiente. */
    boolean marcarCancelado(int numero, LocalDateTime fechaCancelacion);

    Optional<Turno> buscarPorNumero(int numero);

    /** Turnos con el estado indicado, ordenados por numero. */
    List<Turno> listarPorEstado(EstadoTurno estado);

    /** Historial completo de un cliente, ordenado por numero. */
    List<Turno> listarPorCliente(int codigoCliente);

    int contarPendientesPorCliente(int codigoCliente);

    int contarPendientesPorServicio(int codigoServicio);

    /** Cantidades por estado para cada servicio, incluidos los servicios sin turnos. */
    List<ResumenServicio> resumenPorServicio();
}
