package com.oficina.turnos.vista;

import com.oficina.turnos.modelo.Cliente;
import com.oficina.turnos.modelo.Servicio;
import com.oficina.turnos.modelo.TurnoDetalle;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/** Utilidades de presentacion: titulos, mensajes y tablas. */
public final class Pantalla {

    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    private Pantalla() {
    }

    public static void titulo(String texto) {
        String linea = "-".repeat(Math.max(40, texto.length() + 4));
        System.out.println();
        System.out.println(linea);
        System.out.println("  " + texto);
        System.out.println(linea);
    }

    public static void exito(String mensaje) {
        System.out.println("[OK] " + mensaje);
    }

    public static void error(String mensaje) {
        System.out.println("[ERROR] " + mensaje);
    }

    public static void info(String mensaje) {
        System.out.println("[i] " + mensaje);
    }

    public static String fecha(LocalDateTime fecha) {
        return fecha == null ? "-" : FORMATO.format(fecha);
    }

    public static void tablaClientes(List<Cliente> clientes) {
        if (clientes.isEmpty()) {
            info("No hay clientes registrados.");
            return;
        }
        String formato = "%-8s %-30s %-10s %-8s%n";
        System.out.printf(formato, "Codigo", "Nombre completo", "Telefono", "Estado");
        for (Cliente c : clientes) {
            System.out.printf(formato, c.getCodigo(), recortar(c.getNombre(), 30), c.getTelefono(), c.getEstadoTexto());
        }
        System.out.println("Total: " + clientes.size() + " cliente(s).");
    }

    public static void fichaCliente(Cliente c) {
        System.out.println("  Codigo   : " + c.getCodigo());
        System.out.println("  Nombre   : " + c.getNombre());
        System.out.println("  Telefono : " + c.getTelefono());
        System.out.println("  Estado   : " + c.getEstadoTexto());
    }

    public static void tablaServicios(List<Servicio> servicios) {
        if (servicios.isEmpty()) {
            info("No hay servicios registrados.");
            return;
        }
        String formato = "%-8s %-30s %-8s%n";
        System.out.printf(formato, "Codigo", "Nombre", "Estado");
        for (Servicio s : servicios) {
            System.out.printf(formato, s.getCodigo(), recortar(s.getNombre(), 30), s.getEstadoTexto());
        }
    }

    public static void tablaPendientes(List<TurnoDetalle> turnos) {
        String formato = "%-5s %-7s %-26s %-24s %-19s%n";
        System.out.printf(formato, "Pos.", "Turno", "Cliente", "Servicio", "Generado");
        int posicion = 1;
        for (TurnoDetalle d : turnos) {
            System.out.printf(formato, posicion++, d.turno().getNumero(),
                    recortar(d.turno().getCodigoCliente() + " " + d.nombreCliente(), 26),
                    recortar(d.nombreServicio(), 24), fecha(d.turno().getFechaGeneracion()));
        }
    }

    /** Tabla para turnos atendidos o cancelados; la ultima columna es la fecha de cierre. */
    public static void tablaCerrados(List<TurnoDetalle> turnos, String tituloFechaCierre) {
        String formato = "%-7s %-26s %-24s %-19s  %-19s%n";
        System.out.printf(formato, "Turno", "Cliente", "Servicio", "Generado", tituloFechaCierre);
        for (TurnoDetalle d : turnos) {
            System.out.printf(formato, d.turno().getNumero(),
                    recortar(d.turno().getCodigoCliente() + " " + d.nombreCliente(), 26),
                    recortar(d.nombreServicio(), 24),
                    fecha(d.turno().getFechaGeneracion()), fecha(d.turno().getFechaCierre()));
        }
    }

    public static void tablaHistorial(List<TurnoDetalle> turnos) {
        String formato = "%-7s %-24s %-10s %-19s  %-19s%n";
        System.out.printf(formato, "Turno", "Servicio", "Estado", "Generado", "Atencion/Cancelacion");
        for (TurnoDetalle d : turnos) {
            System.out.printf(formato, d.turno().getNumero(), recortar(d.nombreServicio(), 24),
                    d.turno().getEstado(), fecha(d.turno().getFechaGeneracion()), fecha(d.turno().getFechaCierre()));
        }
    }

    private static String recortar(String texto, int maximo) {
        return texto.length() <= maximo ? texto : texto.substring(0, maximo - 3) + "...";
    }
}
