package com.oficina.turnos.logica;

/** Validaciones de formato comunes. Lanzan ReglaNegocioException con un mensaje claro. */
public final class Validador {

    private static final int LONGITUD_MAXIMA_TEXTO = 100;

    private Validador() {
    }

    public static void validarCodigo(int codigo, String entidad) {
        if (codigo <= 0) {
            throw new ReglaNegocioException("El codigo de " + entidad + " debe ser un numero entero positivo.");
        }
    }

    /** Devuelve el texto sin espacios sobrantes; falla si esta vacio o es demasiado largo. */
    public static String validarTextoObligatorio(String texto, String campo) {
        if (texto == null || texto.isBlank()) {
            throw new ReglaNegocioException("El campo \"" + campo + "\" es obligatorio y no puede quedar vacio.");
        }
        String limpio = texto.trim().replaceAll("\\s+", " ");
        if (limpio.length() > LONGITUD_MAXIMA_TEXTO) {
            throw new ReglaNegocioException("El campo \"" + campo + "\" admite como maximo " + LONGITUD_MAXIMA_TEXTO + " caracteres.");
        }
        return limpio;
    }

    public static String validarTelefono(String telefono) {
        String limpio = telefono == null ? "" : telefono.trim();
        if (!limpio.matches("\\d{8}")) {
            throw new ReglaNegocioException("El telefono debe tener exactamente 8 digitos numericos (por ejemplo 55501001).");
        }
        return limpio;
    }
}
