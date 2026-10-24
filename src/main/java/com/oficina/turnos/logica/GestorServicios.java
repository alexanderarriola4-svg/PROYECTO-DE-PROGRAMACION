package com.oficina.turnos.logica;

import com.oficina.turnos.datos.ServicioDAO;
import com.oficina.turnos.datos.TurnoDAO;
import com.oficina.turnos.modelo.Servicio;

import java.util.List;

/** Reglas de negocio de servicios. */
public class GestorServicios {

    private final ServicioDAO servicioDAO;
    private final TurnoDAO turnoDAO;

    public GestorServicios(ServicioDAO servicioDAO, TurnoDAO turnoDAO) {
        this.servicioDAO = servicioDAO;
        this.turnoDAO = turnoDAO;
    }

    public Servicio registrar(int codigo, String nombre) {
        Validador.validarCodigo(codigo, "servicio");
        String nombreLimpio = Validador.validarTextoObligatorio(nombre, "nombre del servicio");
        if (servicioDAO.buscarPorCodigo(codigo).isPresent()) {
            throw new ReglaNegocioException("Ya existe un servicio con el codigo " + codigo + ".");
        }
        Servicio nuevo = new Servicio(codigo, nombreLimpio, true);
        servicioDAO.insertar(nuevo);
        return nuevo;
    }

    public List<Servicio> listar() {
        return servicioDAO.listarTodos();
    }

    public List<Servicio> listarActivos() {
        return servicioDAO.listarTodos().stream().filter(Servicio::isActivo).toList();
    }

    public Servicio buscar(int codigo) {
        Validador.validarCodigo(codigo, "servicio");
        return servicioDAO.buscarPorCodigo(codigo)
                .orElseThrow(() -> new ReglaNegocioException("No existe un servicio registrado con el codigo " + codigo + "."));
    }

    public Servicio actualizar(int codigo, String nombre) {
        Servicio servicio = buscar(codigo);
        String nombreLimpio = Validador.validarTextoObligatorio(nombre, "nombre del servicio");
        Servicio actualizado = new Servicio(codigo, nombreLimpio, servicio.isActivo());
        servicioDAO.actualizar(actualizado);
        return actualizado;
    }

    public Servicio activar(int codigo) {
        Servicio servicio = buscar(codigo);
        if (servicio.isActivo()) {
            throw new ReglaNegocioException("El servicio " + servicio + " ya esta activo.");
        }
        servicioDAO.cambiarEstado(codigo, true);
        servicio.setActivo(true);
        return servicio;
    }

    public Servicio desactivar(int codigo) {
        Servicio servicio = buscar(codigo);
        if (!servicio.isActivo()) {
            throw new ReglaNegocioException("El servicio " + servicio + " ya esta inactivo.");
        }
        int pendientes = turnoDAO.contarPendientesPorServicio(codigo);
        if (pendientes > 0) {
            throw new ReglaNegocioException("No se puede desactivar el servicio " + servicio
                    + " porque tiene " + pendientes + " turno(s) pendiente(s).");
        }
        servicioDAO.cambiarEstado(codigo, false);
        servicio.setActivo(false);
        return servicio;
    }
}
