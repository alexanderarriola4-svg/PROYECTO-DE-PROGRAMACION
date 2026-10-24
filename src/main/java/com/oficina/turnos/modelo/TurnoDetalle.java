package com.oficina.turnos.modelo;

/** Turno acompanado de los nombres del cliente y del servicio, para mostrarlo en pantalla. */
public record TurnoDetalle(Turno turno, String nombreCliente, String nombreServicio) {
}
