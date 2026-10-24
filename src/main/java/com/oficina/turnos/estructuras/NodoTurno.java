package com.oficina.turnos.estructuras;

import com.oficina.turnos.modelo.Turno;

/** Nodo de la lista enlazada simple que forma la cola de turnos. */
public class NodoTurno {

    private final Turno turno;
    private NodoTurno siguiente;

    public NodoTurno(Turno turno) {
        this.turno = turno;
        this.siguiente = null;
    }

    public Turno getTurno() {
        return turno;
    }

    public NodoTurno getSiguiente() {
        return siguiente;
    }

    public void setSiguiente(NodoTurno siguiente) {
        this.siguiente = siguiente;
    }
}
