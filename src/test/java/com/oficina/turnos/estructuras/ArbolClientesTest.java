package com.oficina.turnos.estructuras;

import com.oficina.turnos.modelo.Cliente;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ArbolClientesTest {

    private ArbolClientes arbolConClientes(int... codigos) {
        ArbolClientes arbol = new ArbolClientes();
        for (int codigo : codigos) {
            arbol.insertar(new Cliente(codigo, "Cliente " + codigo, "55500000", true));
        }
        return arbol;
    }

    @Test
    void inordenDevuelveCodigosOrdenados() {
        ArbolClientes arbol = arbolConClientes(1003, 1001, 1005, 1002, 1004);
        List<Integer> codigos = arbol.inorden().stream().map(Cliente::getCodigo).toList();
        assertEquals(List.of(1001, 1002, 1003, 1004, 1005), codigos);
        assertEquals(3, arbol.altura());
    }

    @Test
    void buscaExistenteYNoExistente() {
        ArbolClientes arbol = arbolConClientes(1001, 1002, 1003);
        assertEquals("Cliente 1003", arbol.buscar(1003).getNombre());
        assertNull(arbol.buscar(9999));
    }

    @Test
    void rechazaCodigoDuplicado() {
        ArbolClientes arbol = arbolConClientes(1001);
        assertThrows(IllegalArgumentException.class,
                () -> arbol.insertar(new Cliente(1001, "Otro", "55500000", true)));
        assertEquals(1, arbol.tamanio());
    }

    @Test
    void actualizarReemplazaDatosDelNodo() {
        ArbolClientes arbol = arbolConClientes(1002, 1001, 1003);
        arbol.actualizar(new Cliente(1003, "Nuevo nombre", "12345678", false));
        Cliente cliente = arbol.buscar(1003);
        assertEquals("Nuevo nombre", cliente.getNombre());
        assertFalse(cliente.isActivo());
    }
}
