package com.oficina.turnos.vista;

import com.oficina.turnos.logica.GestorClientes;
import com.oficina.turnos.modelo.Cliente;

/** Submenu de gestion de clientes. */
public class VistaClientes extends MenuBase {

    private final GestorClientes gestorClientes;

    public VistaClientes(EntradaConsola entrada, GestorClientes gestorClientes) {
        super(entrada);
        this.gestorClientes = gestorClientes;
    }

    public void mostrarMenu() {
        int opcion;
        do {
            Pantalla.titulo("GESTION DE CLIENTES");
            System.out.println(" 1. Registrar cliente");
            System.out.println(" 2. Consultar todos los clientes (inorden del BST)");
            System.out.println(" 3. Buscar cliente por codigo (BST)");
            System.out.println(" 4. Actualizar nombre y telefono");
            System.out.println(" 5. Activar / desactivar cliente");
            System.out.println(" 0. Volver al menu principal");
            opcion = entrada.leerEntero("Seleccione una opcion: ");
            switch (opcion) {
                case 1 -> ejecutarSeguro(this::registrar);
                case 2 -> ejecutarSeguro(this::listar);
                case 3 -> ejecutarSeguro(this::buscar);
                case 4 -> ejecutarSeguro(this::actualizar);
                case 5 -> ejecutarSeguro(this::cambiarEstado);
                case 0 -> { }
                default -> opcionInvalida(opcion);
            }
        } while (opcion != 0);
    }

    private void registrar() {
        Pantalla.titulo("Registrar cliente");
        int codigo = entrada.leerEntero("Codigo (entero positivo): ");
        String nombre = entrada.leerTexto("Nombre completo: ");
        String telefono = entrada.leerTexto("Telefono (8 digitos): ");
        Cliente cliente = gestorClientes.registrar(codigo, nombre, telefono);
        Pantalla.exito("Cliente registrado: " + cliente);
    }

    private void listar() {
        Pantalla.titulo("Clientes ordenados por codigo (recorrido inorden del BST)");
        Pantalla.tablaClientes(gestorClientes.listar());
    }

    private void buscar() {
        Pantalla.titulo("Buscar cliente en el BST");
        int codigo = entrada.leerEntero("Codigo del cliente: ");
        Cliente cliente = gestorClientes.buscar(codigo);
        Pantalla.exito("Cliente encontrado mediante busqueda recursiva en el BST:");
        Pantalla.fichaCliente(cliente);
    }

    private void actualizar() {
        Pantalla.titulo("Actualizar cliente");
        int codigo = entrada.leerEntero("Codigo del cliente a actualizar: ");
        Cliente actual = gestorClientes.buscar(codigo);
        Pantalla.fichaCliente(actual);
        Pantalla.info("El codigo no se puede modificar. Presione Enter para conservar el valor actual.");
        String nombre = entrada.leerTexto("Nuevo nombre completo [" + actual.getNombre() + "]: ");
        String telefono = entrada.leerTexto("Nuevo telefono [" + actual.getTelefono() + "]: ");
        Cliente actualizado = gestorClientes.actualizar(codigo,
                nombre.isEmpty() ? actual.getNombre() : nombre,
                telefono.isEmpty() ? actual.getTelefono() : telefono);
        Pantalla.exito("Cliente actualizado en la base de datos y en el BST:");
        Pantalla.fichaCliente(actualizado);
    }

    private void cambiarEstado() {
        Pantalla.titulo("Activar / desactivar cliente");
        int codigo = entrada.leerEntero("Codigo del cliente: ");
        Cliente cliente = gestorClientes.buscar(codigo);
        Pantalla.fichaCliente(cliente);
        if (cliente.isActivo()) {
            if (entrada.confirmar("Desea DESACTIVAR a este cliente?")) {
                gestorClientes.desactivar(codigo);
                Pantalla.exito("Cliente " + cliente + " desactivado. Conserva su historial.");
            }
        } else if (entrada.confirmar("Desea ACTIVAR a este cliente?")) {
            gestorClientes.activar(codigo);
            Pantalla.exito("Cliente " + cliente + " activado.");
        }
    }
}
