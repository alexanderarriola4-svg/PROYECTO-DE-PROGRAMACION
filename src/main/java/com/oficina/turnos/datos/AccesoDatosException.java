package com.oficina.turnos.datos;

/** Error al leer o guardar en la base de datos. Envuelve la SQLException original. */
public class AccesoDatosException extends RuntimeException {

    public AccesoDatosException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }

    public AccesoDatosException(String mensaje) {
        super(mensaje);
    }
}
