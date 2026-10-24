package com.oficina.turnos.estructuras;

import com.oficina.turnos.modelo.Cliente;

/** Nodo del arbol binario de busqueda de clientes. */
public class NodoCliente {

    private Cliente cliente;
    private NodoCliente izquierdo;
    private NodoCliente derecho;

    public NodoCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public NodoCliente getIzquierdo() {
        return izquierdo;
    }

    public void setIzquierdo(NodoCliente izquierdo) {
        this.izquierdo = izquierdo;
    }

    public NodoCliente getDerecho() {
        return derecho;
    }

    public void setDerecho(NodoCliente derecho) {
        this.derecho = derecho;
    }

    public int getClave() {
        return cliente.getCodigo();
    }
}
