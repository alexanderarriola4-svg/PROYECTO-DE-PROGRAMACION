package com.oficina.turnos.estructuras;

import com.oficina.turnos.modelo.Cliente;

import java.util.ArrayList;
import java.util.List;

/**
 * Arbol binario de busqueda (BST) propio de clientes, con el codigo como clave.
 * Regla: los codigos menores van a la izquierda y los mayores a la derecha.
 * Insercion, busqueda y recorrido inorden son recursivos.
 * Los clientes inactivos permanecen en el arbol.
 */
public class ArbolClientes {

    private NodoCliente raiz;
    private int tamanio;

    /** Inserta un cliente. Lanza IllegalArgumentException si el codigo ya existe. */
    public void insertar(Cliente cliente) {
        if (cliente == null) {
            throw new IllegalArgumentException("No se puede insertar un cliente nulo.");
        }
        raiz = insertarRecursivo(raiz, cliente);
        tamanio++;
    }

    private NodoCliente insertarRecursivo(NodoCliente nodo, Cliente cliente) {
        if (nodo == null) {
            return new NodoCliente(cliente);           // caso base: lugar libre
        }
        int codigo = cliente.getCodigo();
        if (codigo < nodo.getClave()) {
            nodo.setIzquierdo(insertarRecursivo(nodo.getIzquierdo(), cliente));
        } else if (codigo > nodo.getClave()) {
            nodo.setDerecho(insertarRecursivo(nodo.getDerecho(), cliente));
        } else {
            throw new IllegalArgumentException("Ya existe un cliente con el codigo " + codigo + " en el arbol.");
        }
        return nodo;
    }

    /** Busca un cliente por codigo de forma recursiva. Devuelve null si no existe. */
    public Cliente buscar(int codigo) {
        NodoCliente nodo = buscarNodo(raiz, codigo);
        return nodo == null ? null : nodo.getCliente();
    }

    private NodoCliente buscarNodo(NodoCliente nodo, int codigo) {
        if (nodo == null || nodo.getClave() == codigo) {
            return nodo;                               // casos base: no existe o encontrado
        }
        if (codigo < nodo.getClave()) {
            return buscarNodo(nodo.getIzquierdo(), codigo);
        }
        return buscarNodo(nodo.getDerecho(), codigo);
    }

    public boolean contiene(int codigo) {
        return buscar(codigo) != null;
    }

    /**
     * Reemplaza los datos guardados en el nodo del cliente (misma clave).
     * Se usa para mantener el arbol sincronizado despues de guardar en la base de datos.
     */
    public void actualizar(Cliente clienteActualizado) {
        NodoCliente nodo = buscarNodo(raiz, clienteActualizado.getCodigo());
        if (nodo == null) {
            throw new IllegalArgumentException("No existe el cliente " + clienteActualizado.getCodigo() + " en el arbol.");
        }
        nodo.setCliente(clienteActualizado);
    }

    /** Recorrido inorden recursivo (izquierdo, raiz, derecho): clientes ordenados por codigo. */
    public List<Cliente> inorden() {
        List<Cliente> resultado = new ArrayList<>(tamanio);
        inordenRecursivo(raiz, resultado);
        return resultado;
    }

    private void inordenRecursivo(NodoCliente nodo, List<Cliente> resultado) {
        if (nodo == null) {
            return;
        }
        inordenRecursivo(nodo.getIzquierdo(), resultado);
        resultado.add(nodo.getCliente());
        inordenRecursivo(nodo.getDerecho(), resultado);
    }

    /** Altura del arbol (recursiva). Un arbol vacio tiene altura 0. */
    public int altura() {
        return alturaRecursiva(raiz);
    }

    private int alturaRecursiva(NodoCliente nodo) {
        if (nodo == null) {
            return 0;
        }
        return 1 + Math.max(alturaRecursiva(nodo.getIzquierdo()), alturaRecursiva(nodo.getDerecho()));
    }

    public boolean estaVacio() {
        return raiz == null;
    }

    public int tamanio() {
        return tamanio;
    }

    public void vaciar() {
        raiz = null;
        tamanio = 0;
    }
}
