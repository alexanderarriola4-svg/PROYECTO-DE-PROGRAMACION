package com.oficina.turnos.modelo;

import java.time.LocalDateTime;

/**
 * Turno solicitado por un cliente para un servicio.
 * El numero lo genera la base de datos (AUTOINCREMENT) y nunca se reutiliza.
 */
public class Turno {

    private final int numero;
    private final int codigoCliente;
    private final int codigoServicio;
    private final LocalDateTime fechaGeneracion;
    private EstadoTurno estado;
    private LocalDateTime fechaAtencion;
    private LocalDateTime fechaCancelacion;

    public Turno(int numero, int codigoCliente, int codigoServicio, LocalDateTime fechaGeneracion,
                 EstadoTurno estado, LocalDateTime fechaAtencion, LocalDateTime fechaCancelacion) {
        this.numero = numero;
        this.codigoCliente = codigoCliente;
        this.codigoServicio = codigoServicio;
        this.fechaGeneracion = fechaGeneracion;
        this.estado = estado;
        this.fechaAtencion = fechaAtencion;
        this.fechaCancelacion = fechaCancelacion;
    }

    /** Crea un turno recien generado, en estado PENDIENTE. */
    public static Turno nuevoPendiente(int numero, int codigoCliente, int codigoServicio, LocalDateTime fechaGeneracion) {
        return new Turno(numero, codigoCliente, codigoServicio, fechaGeneracion, EstadoTurno.PENDIENTE, null, null);
    }

    public boolean estaPendiente() {
        return estado == EstadoTurno.PENDIENTE;
    }

    /** Cambia el estado a ATENDIDO. Solo se permite desde PENDIENTE. */
    public void marcarAtendido(LocalDateTime fecha) {
        validarPendiente();
        this.estado = EstadoTurno.ATENDIDO;
        this.fechaAtencion = fecha;
    }

    /** Cambia el estado a CANCELADO. Solo se permite desde PENDIENTE. */
    public void marcarCancelado(LocalDateTime fecha) {
        validarPendiente();
        this.estado = EstadoTurno.CANCELADO;
        this.fechaCancelacion = fecha;
    }

    private void validarPendiente() {
        if (!estaPendiente()) {
            throw new IllegalStateException("El turno " + numero + " ya no esta pendiente (" + estado + ").");
        }
    }

    public int getNumero() {
        return numero;
    }

    public int getCodigoCliente() {
        return codigoCliente;
    }

    public int getCodigoServicio() {
        return codigoServicio;
    }

    public LocalDateTime getFechaGeneracion() {
        return fechaGeneracion;
    }

    public EstadoTurno getEstado() {
        return estado;
    }

    public LocalDateTime getFechaAtencion() {
        return fechaAtencion;
    }

    public LocalDateTime getFechaCancelacion() {
        return fechaCancelacion;
    }

    /** Fecha de atencion o de cancelacion, segun corresponda; null si sigue pendiente. */
    public LocalDateTime getFechaCierre() {
        return fechaAtencion != null ? fechaAtencion : fechaCancelacion;
    }

    @Override
    public String toString() {
        return "Turno " + numero + " (cliente " + codigoCliente + ", servicio " + codigoServicio + ", " + estado + ")";
    }
}
