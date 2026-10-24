package com.oficina.turnos.estructuras;

import com.oficina.turnos.modelo.Turno;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ColaTurnosTest {

    private ColaTurnos cola;

    @BeforeEach
    void preparar() {
        cola = new ColaTurnos();
        for (int numero = 1; numero <= 5; numero++) {
            cola.encolar(Turno.nuevoPendiente(numero, 1000 + numero, 1, LocalDateTime.now()));
        }
    }

    @Test
    void respetaFifo() {
        assertEquals(1, cola.verPrimero().getNumero());
        assertEquals(1, cola.desencolar().getNumero());
        assertEquals(2, cola.desencolar().getNumero());
        assertEquals("3 -> 4 -> 5", cola.mostrar());
    }

    @Test
    void cancelarEnMedioConservaOrden() {
        assertEquals(3, cola.cancelar(3).getNumero());
        assertEquals("1 -> 2 -> 4 -> 5", cola.mostrar());
        assertEquals(4, cola.tamanio());
    }

    @Test
    void cancelarPrimeroYUltimoActualizaExtremos() {
        cola.cancelar(1);
        cola.cancelar(5);
        assertEquals("2 -> 3 -> 4", cola.mostrar());
        cola.encolar(Turno.nuevoPendiente(6, 2000, 1, LocalDateTime.now()));
        assertEquals("2 -> 3 -> 4 -> 6", cola.mostrar());
    }

    @Test
    void cancelarInexistenteDevuelveNull() {
        assertNull(cola.cancelar(99));
        assertEquals(5, cola.tamanio());
    }

    @Test
    void vaciarCompletamente() {
        while (!cola.estaVacia()) {
            cola.desencolar();
        }
        assertNull(cola.desencolar());
        assertNull(cola.verPrimero());
        assertEquals("(vacia)", cola.mostrar());
        cola.encolar(Turno.nuevoPendiente(7, 1, 1, LocalDateTime.now()));
        assertEquals(7, cola.verPrimero().getNumero());
    }
}
