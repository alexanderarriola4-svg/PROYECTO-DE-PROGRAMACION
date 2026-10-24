package com.oficina.turnos.estructuras;

import com.oficina.turnos.modelo.Turno;

import java.util.ArrayList;
import java.util.List;

/**
 * Cola FIFO de turnos pendientes implementada con una lista enlazada simple propia.
 *
 * <pre>
 *  frente                              fin
 *    |                                  |
 *  [T1] -> [T2] -> [T4] -> [T5] -> null
 * </pre>
 *
 * Se encola por el fin y se atiende por el frente. Encolar, desencolar y
 * consultar el primero son O(1); cancelar o buscar por numero son O(n).
 * No usa LinkedList, ArrayDeque ni otra coleccion para resolver la cola.
 */
public class ColaTurnos {

    private NodoTurno frente;
    private NodoTurno fin;
    private int tamanio;

    public ColaTurnos() {
        vaciar();
    }

    /** Agrega el turno al final de la cola. */
    public void encolar(Turno turno) {
        if (turno == null) {
            throw new IllegalArgumentException("No se puede encolar un turno nulo.");
        }
        NodoTurno nuevo = new NodoTurno(turno);
        if (estaVacia()) {
            frente = nuevo;
        } else {
            fin.setSiguiente(nuevo);
        }
        fin = nuevo;
        tamanio++;
    }

    /** Retira y devuelve el primer turno (FIFO). Devuelve null si la cola esta vacia. */
    public Turno desencolar() {
        if (estaVacia()) {
            return null;
        }
        Turno primero = frente.getTurno();
        frente = frente.getSiguiente();
        if (frente == null) {
            fin = null;
        }
        tamanio--;
        return primero;
    }

    /** Devuelve el primer turno sin retirarlo, o null si la cola esta vacia. */
    public Turno verPrimero() {
        return estaVacia() ? null : frente.getTurno();
    }

    /**
     * Retira de la cola el turno con el numero indicado, sin alterar el orden
     * de los demas: el nodo anterior pasa a apuntar al siguiente del eliminado.
     *
     * @return el turno retirado, o null si no esta en la cola
     */
    public Turno cancelar(int numero) {
        NodoTurno anterior = null;
        NodoTurno actual = frente;
        while (actual != null && actual.getTurno().getNumero() != numero) {
            anterior = actual;
            actual = actual.getSiguiente();
        }
        if (actual == null) {
            return null;
        }
        if (anterior == null) {
            frente = actual.getSiguiente();      // era el primero
        } else {
            anterior.setSiguiente(actual.getSiguiente());
        }
        if (actual == fin) {
            fin = anterior;                      // era el ultimo
        }
        tamanio--;
        return actual.getTurno();
    }

    /** Busca un turno por numero dentro de la cola, sin retirarlo. */
    public Turno buscar(int numero) {
        for (NodoTurno actual = frente; actual != null; actual = actual.getSiguiente()) {
            if (actual.getTurno().getNumero() == numero) {
                return actual.getTurno();
            }
        }
        return null;
    }

    /** Indica si el cliente ya tiene un turno en la cola. */
    public boolean contieneCliente(int codigoCliente) {
        for (NodoTurno actual = frente; actual != null; actual = actual.getSiguiente()) {
            if (actual.getTurno().getCodigoCliente() == codigoCliente) {
                return true;
            }
        }
        return false;
    }

    /** Recorre la cola desde el frente y devuelve los turnos en orden de atencion. */
    public List<Turno> recorrer() {
        List<Turno> turnos = new ArrayList<>(tamanio);
        for (NodoTurno actual = frente; actual != null; actual = actual.getSiguiente()) {
            turnos.add(actual.getTurno());
        }
        return turnos;
    }

    /** Representacion corta de la cola, por ejemplo: "1 -> 2 -> 4 -> 5". */
    public String mostrar() {
        if (estaVacia()) {
            return "(vacia)";
        }
        StringBuilder texto = new StringBuilder();
        for (NodoTurno actual = frente; actual != null; actual = actual.getSiguiente()) {
            texto.append(actual.getTurno().getNumero());
            if (actual.getSiguiente() != null) {
                texto.append(" -> ");
            }
        }
        return texto.toString();
    }

    public boolean estaVacia() {
        return frente == null;
    }

    public int tamanio() {
        return tamanio;
    }

    public final void vaciar() {
        frente = null;
        fin = null;
        tamanio = 0;
    }
}
