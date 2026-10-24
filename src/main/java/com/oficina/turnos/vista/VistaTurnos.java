package com.oficina.turnos.vista;

import com.oficina.turnos.logica.GestorClientes;
import com.oficina.turnos.logica.GestorReportes;
import com.oficina.turnos.logica.GestorServicios;
import com.oficina.turnos.logica.GestorTurnos;
import com.oficina.turnos.modelo.Cliente;
import com.oficina.turnos.modelo.ResumenServicio;
import com.oficina.turnos.modelo.Turno;
import com.oficina.turnos.modelo.TurnoDetalle;

import java.util.List;
import java.util.Optional;

/** Pantallas de turnos, consultas y reportes. */
public class VistaTurnos extends MenuBase {

    private final GestorTurnos gestorTurnos;
    private final GestorClientes gestorClientes;
    private final GestorServicios gestorServicios;
    private final GestorReportes gestorReportes;

    public VistaTurnos(EntradaConsola entrada, GestorTurnos gestorTurnos, GestorClientes gestorClientes,
                       GestorServicios gestorServicios, GestorReportes gestorReportes) {
        super(entrada);
        this.gestorTurnos = gestorTurnos;
        this.gestorClientes = gestorClientes;
        this.gestorServicios = gestorServicios;
        this.gestorReportes = gestorReportes;
    }

    public void generarTurno() {
        Pantalla.titulo("GENERAR TURNO");
        int codigoCliente = entrada.leerEntero("Codigo del cliente: ");
        Cliente cliente = gestorTurnos.validarClienteParaTurno(codigoCliente);
        Pantalla.info("Cliente: " + cliente + " [" + cliente.getEstadoTexto() + "]");
        System.out.println("Servicios activos:");
        Pantalla.tablaServicios(gestorServicios.listarActivos());
        int codigoServicio = entrada.leerEntero("Codigo del servicio: ");
        Turno turno = gestorTurnos.generar(codigoCliente, codigoServicio);
        TurnoDetalle detalle = gestorReportes.detallar(turno);
        Pantalla.exito("Turno " + turno.getNumero() + " generado para " + detalle.nombreCliente()
                + " - servicio: " + detalle.nombreServicio() + " (" + Pantalla.fecha(turno.getFechaGeneracion()) + ")");
        Pantalla.info("Cola pendiente: " + gestorTurnos.colaComoTexto());
    }

    public void atenderSiguiente() {
        Pantalla.titulo("ATENDER SIGUIENTE TURNO");
        Optional<Turno> atendido = gestorTurnos.atenderSiguiente();
        if (atendido.isEmpty()) {
            Pantalla.info("No existen turnos pendientes por atender.");
            return;
        }
        TurnoDetalle detalle = gestorReportes.detallar(atendido.get());
        Turno turno = detalle.turno();
        Pantalla.exito("Atendiendo turno " + turno.getNumero());
        System.out.println("  Cliente  : " + turno.getCodigoCliente() + " - " + detalle.nombreCliente());
        System.out.println("  Servicio : " + turno.getCodigoServicio() + " - " + detalle.nombreServicio());
        System.out.println("  Estado   : " + turno.getEstado() + " (" + Pantalla.fecha(turno.getFechaAtencion()) + ")");
        Pantalla.info("Cola pendiente: " + gestorTurnos.colaComoTexto());
    }

    public void cancelarTurno() {
        Pantalla.titulo("CANCELAR TURNO");
        Pantalla.info("Cola pendiente: " + gestorTurnos.colaComoTexto());
        int numero = entrada.leerEntero("Numero de turno a cancelar: ");
        Turno turno = gestorTurnos.cancelar(numero);
        Pantalla.exito("Turno " + turno.getNumero() + " CANCELADO (" + Pantalla.fecha(turno.getFechaCancelacion()) + ").");
        Pantalla.info("Cola pendiente: " + gestorTurnos.colaComoTexto());
    }

    public void consultarPendientes() {
        Pantalla.titulo("TURNOS PENDIENTES (en orden de atencion)");
        List<Turno> pendientes = gestorTurnos.pendientes();
        if (pendientes.isEmpty()) {
            Pantalla.info("No existen turnos pendientes.");
            return;
        }
        Pantalla.tablaPendientes(gestorReportes.detallar(pendientes));
        Pantalla.info("Cola: " + gestorTurnos.colaComoTexto());
    }

    public void consultarAtendidos() {
        Pantalla.titulo("TURNOS ATENDIDOS");
        mostrarCerrados(gestorTurnos.atendidos(), "Atendido", "No hay turnos atendidos.");
    }

    public void consultarCancelados() {
        Pantalla.titulo("TURNOS CANCELADOS");
        mostrarCerrados(gestorTurnos.cancelados(), "Cancelado", "No hay turnos cancelados.");
    }

    private void mostrarCerrados(List<Turno> turnos, String tituloFecha, String mensajeVacio) {
        if (turnos.isEmpty()) {
            Pantalla.info(mensajeVacio);
            return;
        }
        Pantalla.tablaCerrados(gestorReportes.detallar(turnos), tituloFecha);
        System.out.println("Total: " + turnos.size());
    }

    public void historialCliente() {
        Pantalla.titulo("HISTORIAL DE UN CLIENTE");
        int codigo = entrada.leerEntero("Codigo del cliente: ");
        Cliente cliente = gestorClientes.buscar(codigo);
        List<Turno> historial = gestorTurnos.historial(codigo);
        Pantalla.info("Cliente: " + cliente + " [" + cliente.getEstadoTexto() + "]");
        if (historial.isEmpty()) {
            Pantalla.info("El cliente no tiene turnos registrados.");
            return;
        }
        Pantalla.tablaHistorial(gestorReportes.detallar(historial));
    }

    public void resumenPorServicio() {
        Pantalla.titulo("RESUMEN POR SERVICIO");
        List<ResumenServicio> filas = gestorReportes.resumenPorServicio();
        String formato = "%-26s %10s %10s %10s %7s%n";
        System.out.printf(formato, "Servicio", "Pendientes", "Atendidos", "Cancelados", "Total");
        for (ResumenServicio fila : filas) {
            System.out.printf(formato, fila.nombreServicio(), fila.pendientes(), fila.atendidos(), fila.cancelados(), fila.total());
        }
        ResumenServicio total = gestorReportes.totalGeneral(filas);
        System.out.println("-".repeat(67));
        System.out.printf(formato, total.nombreServicio(), total.pendientes(), total.atendidos(), total.cancelados(), total.total());
    }
}
