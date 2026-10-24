package com.oficina.turnos.logica;

import com.oficina.turnos.datos.TurnoDAO;
import com.oficina.turnos.modelo.Cliente;
import com.oficina.turnos.modelo.ResumenServicio;
import com.oficina.turnos.modelo.Servicio;
import com.oficina.turnos.modelo.Turno;
import com.oficina.turnos.modelo.TurnoDetalle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Reportes y preparacion de datos para mostrar. Usa colecciones de Java
 * (ArrayList y HashMap) como funcion adicional; no reemplazan la cola ni el BST.
 */
public class GestorReportes {

    private final TurnoDAO turnoDAO;
    private final GestorClientes gestorClientes;
    private final GestorServicios gestorServicios;

    public GestorReportes(TurnoDAO turnoDAO, GestorClientes gestorClientes, GestorServicios gestorServicios) {
        this.turnoDAO = turnoDAO;
        this.gestorClientes = gestorClientes;
        this.gestorServicios = gestorServicios;
    }

    /** Una fila por servicio (incluye servicios sin turnos, en cero). */
    public List<ResumenServicio> resumenPorServicio() {
        return turnoDAO.resumenPorServicio();
    }

    /** Fila de totales del resumen. */
    public ResumenServicio totalGeneral(List<ResumenServicio> filas) {
        int pendientes = 0;
        int atendidos = 0;
        int cancelados = 0;
        for (ResumenServicio fila : filas) {
            pendientes += fila.pendientes();
            atendidos += fila.atendidos();
            cancelados += fila.cancelados();
        }
        return new ResumenServicio(0, "Total", pendientes, atendidos, cancelados);
    }

    /** Agrega a cada turno el nombre del cliente (desde el BST) y del servicio (HashMap). */
    public List<TurnoDetalle> detallar(List<Turno> turnos) {
        Map<Integer, String> nombresServicios = new HashMap<>();
        for (Servicio servicio : gestorServicios.listar()) {
            nombresServicios.put(servicio.getCodigo(), servicio.getNombre());
        }
        List<TurnoDetalle> detalles = new ArrayList<>(turnos.size());
        for (Turno turno : turnos) {
            Cliente cliente = gestorClientes.buscarSiExiste(turno.getCodigoCliente());
            String nombreCliente = cliente == null ? "(desconocido)" : cliente.getNombre();
            String nombreServicio = nombresServicios.getOrDefault(turno.getCodigoServicio(), "(desconocido)");
            detalles.add(new TurnoDetalle(turno, nombreCliente, nombreServicio));
        }
        return detalles;
    }

    public TurnoDetalle detallar(Turno turno) {
        return detallar(List.of(turno)).get(0);
    }
}
