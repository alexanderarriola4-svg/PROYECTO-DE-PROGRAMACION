package com.oficina.turnos.vista;

/** Se lanza cuando la entrada estandar se cierra (por ejemplo, Ctrl+D / Ctrl+Z). */
public class FinDeEntradaException extends RuntimeException {

    public FinDeEntradaException() {
        super("La entrada de datos se cerro.");
    }
}
