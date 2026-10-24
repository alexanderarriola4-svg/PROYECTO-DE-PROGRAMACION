package com.oficina.turnos.vista;

import java.io.Console;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Scanner;

/** Lee datos del teclado y repite la pregunta cuando el valor numerico no es valido. */
public class EntradaConsola {

    private final Scanner scanner;

    public EntradaConsola(InputStream entrada) {
        this(entrada, codificacionDeConsola());
    }

    public EntradaConsola(InputStream entrada, Charset codificacion) {
        this.scanner = new Scanner(entrada, codificacion);
    }

    /** Usa la codificacion real de la terminal (en Windows suele ser distinta de UTF-8) para leer bien las tildes. */
    private static Charset codificacionDeConsola() {
        Console consola = System.console();
        return consola != null ? consola.charset() : Charset.defaultCharset();
    }

    /** Lee una linea completa, sin espacios al inicio ni al final (puede quedar vacia). */
    public String leerTexto(String mensaje) {
        System.out.print(mensaje);
        if (!scanner.hasNextLine()) {
            throw new FinDeEntradaException();
        }
        return scanner.nextLine().trim();
    }

    /** Lee un numero entero; si el valor esta vacio o no es numerico, explica el error y vuelve a pedirlo. */
    public int leerEntero(String mensaje) {
        while (true) {
            String texto = leerTexto(mensaje);
            if (texto.isEmpty()) {
                Pantalla.error("El campo no puede quedar vacio. Ingrese un numero entero.");
                continue;
            }
            try {
                return Integer.parseInt(texto);
            } catch (NumberFormatException e) {
                Pantalla.error("\"" + texto + "\" no es un numero entero valido. Intente de nuevo.");
            }
        }
    }

    /** Pregunta S/N hasta recibir una respuesta valida. */
    public boolean confirmar(String mensaje) {
        while (true) {
            String respuesta = leerTexto(mensaje + " (S/N): ").toUpperCase();
            if (respuesta.equals("S") || respuesta.equals("SI")) {
                return true;
            }
            if (respuesta.equals("N") || respuesta.equals("NO")) {
                return false;
            }
            Pantalla.error("Responda S (si) o N (no).");
        }
    }
}
