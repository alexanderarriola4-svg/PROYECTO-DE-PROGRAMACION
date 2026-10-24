package com.oficina.turnos.vista;

import com.oficina.turnos.logica.GestorServicios;
import com.oficina.turnos.modelo.Servicio;

/** Submenu de gestion de servicios. */
public class VistaServicios extends MenuBase {

    private final GestorServicios gestorServicios;

    public VistaServicios(EntradaConsola entrada, GestorServicios gestorServicios) {
        super(entrada);
        this.gestorServicios = gestorServicios;
    }

    public void mostrarMenu() {
        int opcion;
        do {
            Pantalla.titulo("GESTION DE SERVICIOS");
            System.out.println(" 1. Registrar servicio");
            System.out.println(" 2. Listar servicios");
            System.out.println(" 3. Actualizar nombre de un servicio");
            System.out.println(" 4. Activar / desactivar servicio");
            System.out.println(" 0. Volver al menu principal");
            opcion = entrada.leerEntero("Seleccione una opcion: ");
            switch (opcion) {
                case 1 -> ejecutarSeguro(this::registrar);
                case 2 -> ejecutarSeguro(this::listar);
                case 3 -> ejecutarSeguro(this::actualizar);
                case 4 -> ejecutarSeguro(this::cambiarEstado);
                case 0 -> { }
                default -> opcionInvalida(opcion);
            }
        } while (opcion != 0);
    }

    private void registrar() {
        Pantalla.titulo("Registrar servicio");
        int codigo = entrada.leerEntero("Codigo (entero positivo): ");
        String nombre = entrada.leerTexto("Nombre del servicio: ");
        Servicio servicio = gestorServicios.registrar(codigo, nombre);
        Pantalla.exito("Servicio registrado: " + servicio);
    }

    private void listar() {
        Pantalla.titulo("Servicios registrados");
        Pantalla.tablaServicios(gestorServicios.listar());
    }

    private void actualizar() {
        Pantalla.titulo("Actualizar servicio");
        int codigo = entrada.leerEntero("Codigo del servicio: ");
        Servicio actual = gestorServicios.buscar(codigo);
        Pantalla.info("Servicio actual: " + actual + " [" + actual.getEstadoTexto() + "]. El codigo no se puede modificar.");
        String nombre = entrada.leerTexto("Nuevo nombre: ");
        Servicio actualizado = gestorServicios.actualizar(codigo, nombre);
        Pantalla.exito("Servicio actualizado: " + actualizado);
    }

    private void cambiarEstado() {
        Pantalla.titulo("Activar / desactivar servicio");
        int codigo = entrada.leerEntero("Codigo del servicio: ");
        Servicio servicio = gestorServicios.buscar(codigo);
        Pantalla.info("Servicio: " + servicio + " [" + servicio.getEstadoTexto() + "]");
        if (servicio.isActivo()) {
            if (entrada.confirmar("Desea DESACTIVAR este servicio?")) {
                gestorServicios.desactivar(codigo);
                Pantalla.exito("Servicio " + servicio + " desactivado.");
            }
        } else if (entrada.confirmar("Desea ACTIVAR este servicio?")) {
            gestorServicios.activar(codigo);
            Pantalla.exito("Servicio " + servicio + " activado.");
        }
    }
}
