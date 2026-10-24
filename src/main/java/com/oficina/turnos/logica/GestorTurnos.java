package com.oficina.turnos.logica;

import com.oficina.turnos.datos.TurnoDAO;
import com.oficina.turnos.estructuras.ColaTurnos;
import com.oficina.turnos.modelo.Cliente;
import com.oficina.turnos.modelo.EstadoTurno;
import com.oficina.turnos.modelo.Servicio;
import com.oficina.turnos.modelo.Turno;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Generacion, atencion y cancelacion de turnos. Coordina la tabla "turnos"
 * con la cola enlazada propia de turnos pendientes.
 *
 * Igual que con el arbol: primero se guarda en la BD y, solo si eso funciona,
 * se modifica la cola. Asi la cola nunca refleja cambios no guardados.
 */
public class GestorTurnos {

    private final TurnoDAO turnoDAO;
    private final ColaTurnos cola;
    private final GestorClientes gestorClientes;
    private final GestorServicios gestorServicios;

    public GestorTurnos(TurnoDAO turnoDAO, ColaTurnos cola,
                        GestorClientes gestorClientes, GestorServicios gestorServicios) {
        this.turnoDAO = turnoDAO;
        this.cola = cola;
        this.gestorClientes = gestorClientes;
        this.gestorServicios = gestorServicios;
    }

    /** Reconstruye la cola con los turnos PENDIENTES de la BD, ordenados por numero. */
    public int reconstruirCola() {
        cola.vaciar();
        for (Turno turno : turnoDAO.listarPorEstado(EstadoTurno.PENDIENTE)) {
            cola.encolar(turno);
        }
        return cola.tamanio();
    }

    /**
     * Verifica que el cliente exista, este activo y no tenga un turno pendiente.
     * La vista la usa para avisar antes de pedir el servicio; generar() la vuelve a aplicar.
     */
    public Cliente validarClienteParaTurno(int codigoCliente) {
        Cliente cliente = gestorClientes.buscar(codigoCliente);
        if (!cliente.isActivo()) {
            throw new ReglaNegocioException("El cliente " + cliente + " esta inactivo y no puede recibir nuevos turnos.");
        }
        if (cola.contieneCliente(codigoCliente) || turnoDAO.contarPendientesPorCliente(codigoCliente) > 0) {
            throw new ReglaNegocioException("El cliente " + cliente + " ya tiene un turno pendiente. "
                    + "Debe atenderse o cancelarse antes de solicitar otro.");
        }
        return cliente;
    }

    public Turno generar(int codigoCliente, int codigoServicio) {
        validarClienteParaTurno(codigoCliente);
        Servicio servicio = gestorServicios.buscar(codigoServicio);
        if (!servicio.isActivo()) {
            throw new ReglaNegocioException("El servicio " + servicio + " esta inactivo y no admite turnos.");
        }
        Turno turno = turnoDAO.insertar(codigoCliente, codigoServicio, ahora());  // 1) BD
        cola.encolar(turno);                                                     // 2) cola
        return turno;
    }

    /** Atiende el primer turno de la cola (FIFO). Devuelve vacio si no hay pendientes. */
    public Optional<Turno> atenderSiguiente() {
        Turno primero = cola.verPrimero();
        if (primero == null) {
            return Optional.empty();
        }
        LocalDateTime fecha = ahora();
        if (!turnoDAO.marcarAtendido(primero.getNumero(), fecha)) {
            throw new ReglaNegocioException("El turno " + primero.getNumero()
                    + " ya no esta pendiente en la base de datos. Reinicie la aplicacion para sincronizar la cola.");
        }
        cola.desencolar();
        primero.marcarAtendido(fecha);
        return Optional.of(primero);
    }

    public Turno cancelar(int numero) {
        if (numero <= 0) {
            throw new ReglaNegocioException("El numero de turno debe ser un entero positivo.");
        }
        Turno turno = cola.buscar(numero);
        if (turno == null) {
            Turno guardado = turnoDAO.buscarPorNumero(numero)
                    .orElseThrow(() -> new ReglaNegocioException("No existe un turno con el numero " + numero + "."));
            throw new ReglaNegocioException("El turno " + numero + " no se puede cancelar porque ya esta "
                    + guardado.getEstado().name().toLowerCase() + ".");
        }
        LocalDateTime fecha = ahora();
        if (!turnoDAO.marcarCancelado(numero, fecha)) {
            throw new ReglaNegocioException("El turno " + numero
                    + " ya no esta pendiente en la base de datos. Reinicie la aplicacion para sincronizar la cola.");
        }
        cola.cancelar(numero);
        turno.marcarCancelado(fecha);
        return turno;
    }

    /** Turnos pendientes en orden de atencion (recorrido de la cola). */
    public List<Turno> pendientes() {
        return cola.recorrer();
    }

    public String colaComoTexto() {
        return cola.mostrar();
    }

    public Turno siguiente() {
        return cola.verPrimero();
    }

    public List<Turno> atendidos() {
        return turnoDAO.listarPorEstado(EstadoTurno.ATENDIDO);
    }

    public List<Turno> cancelados() {
        return turnoDAO.listarPorEstado(EstadoTurno.CANCELADO);
    }

    public List<Turno> historial(int codigoCliente) {
        gestorClientes.buscar(codigoCliente);
        return turnoDAO.listarPorCliente(codigoCliente);
    }

    private static LocalDateTime ahora() {
        return LocalDateTime.now().withNano(0);
    }
}
