package com.oficina.turnos.modelo;

/** Fila del reporte "Resumen por servicio". */
public record ResumenServicio(int codigoServicio, String nombreServicio,
                              int pendientes, int atendidos, int cancelados) {

    public int total() {
        return pendientes + atendidos + cancelados;
    }
}
