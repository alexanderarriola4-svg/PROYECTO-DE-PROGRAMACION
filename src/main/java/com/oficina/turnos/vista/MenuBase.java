package com.oficina.turnos.vista;

import com.oficina.turnos.datos.AccesoDatosException;
import com.oficina.turnos.logica.ReglaNegocioException;

/**
 * Comportamiento comun de los menus: ejecutar una accion y, si falla,
 * mostrar el mensaje sin detener la aplicacion.
 */
public abstract class MenuBase {

    protected final EntradaConsola entrada;

    protected MenuBase(EntradaConsola entrada) {
        this.entrada = entrada;
    }

    protected void ejecutarSeguro(Runnable accion) {
        try {
            accion.run();
        } catch (ReglaNegocioException e) {
            Pantalla.error(e.getMessage());
        } catch (AccesoDatosException e) {
            Pantalla.error("Problema con la base de datos. No se guardo ningun cambio. Detalle: " + e.getMessage());
        } catch (IllegalArgumentException | IllegalStateException e) {
            Pantalla.error(e.getMessage());
        }
    }

    protected void opcionInvalida(int opcion) {
        Pantalla.error("La opcion " + opcion + " no existe. Elija una de las opciones del menu.");
    }
}
