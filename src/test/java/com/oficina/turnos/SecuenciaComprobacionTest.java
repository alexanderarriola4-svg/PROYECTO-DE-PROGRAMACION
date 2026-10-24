package com.oficina.turnos;

import com.oficina.turnos.datos.ClienteDAOSQLite;
import com.oficina.turnos.datos.ConexionSQLite;
import com.oficina.turnos.datos.InicializadorBD;
import com.oficina.turnos.datos.ServicioDAOSQLite;
import com.oficina.turnos.datos.TurnoDAOSQLite;
import com.oficina.turnos.estructuras.ArbolClientes;
import com.oficina.turnos.estructuras.ColaTurnos;
import com.oficina.turnos.logica.GestorClientes;
import com.oficina.turnos.logica.GestorReportes;
import com.oficina.turnos.logica.GestorServicios;
import com.oficina.turnos.logica.GestorTurnos;
import com.oficina.turnos.logica.ReglaNegocioException;
import com.oficina.turnos.modelo.EstadoTurno;
import com.oficina.turnos.modelo.ResumenServicio;
import com.oficina.turnos.modelo.Turno;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Reproduce la secuencia de comprobacion del enunciado (pasos 1 a 10)
 * sobre una base temporal, incluido el cierre y la reapertura del programa.
 */
class SecuenciaComprobacionTest {

    /** Representa una "ejecucion" del programa: todo se reconstruye desde la base. */
    private static final class Aplicacion {
        final GestorClientes clientes;
        final GestorServicios servicios;
        final GestorTurnos turnos;
        final GestorReportes reportes;

        Aplicacion(Path rutaBD) {
            ConexionSQLite conexion = new ConexionSQLite(rutaBD);
            new InicializadorBD(conexion).inicializar();
            TurnoDAOSQLite turnoDAO = new TurnoDAOSQLite(conexion);
            clientes = new GestorClientes(new ClienteDAOSQLite(conexion), turnoDAO, new ArbolClientes());
            servicios = new GestorServicios(new ServicioDAOSQLite(conexion), turnoDAO);
            turnos = new GestorTurnos(turnoDAO, new ColaTurnos(), clientes, servicios);
            reportes = new GestorReportes(turnoDAO, clientes, servicios);
            clientes.cargarArbolDesdeBD();
            turnos.reconstruirCola();
        }
    }

    @Test
    void secuenciaCompleta(@TempDir Path carpeta) {
        Path rutaBD = carpeta.resolve("data").resolve("turnos.db");
        Aplicacion app = new Aplicacion(rutaBD);

        // 1. Buscar al cliente 1003 mediante el BST
        assertEquals("Maria Garcia", app.clientes.buscar(1003).getNombre());

        // 2. Generar cinco turnos
        int[][] solicitudes = {{1001, 1}, {1002, 2}, {1003, 1}, {1004, 3}, {1005, 2}};
        for (int i = 0; i < solicitudes.length; i++) {
            Turno turno = app.turnos.generar(solicitudes[i][0], solicitudes[i][1]);
            assertEquals(i + 1, turno.getNumero());
        }

        // 3. Rechazar segundo turno de 1001
        ReglaNegocioException rechazo = assertThrows(ReglaNegocioException.class, () -> app.turnos.generar(1001, 2));
        assertTrue(rechazo.getMessage().contains("turno pendiente"));

        // 4. Cancelar el turno 3
        app.turnos.cancelar(3);
        assertEquals("1 -> 2 -> 4 -> 5", app.turnos.colaComoTexto());

        // 5. Atender dos turnos
        assertEquals(1, app.turnos.atenderSiguiente().orElseThrow().getNumero());
        assertEquals(2, app.turnos.atenderSiguiente().orElseThrow().getNumero());

        // 6. Cerrar y volver a abrir
        Aplicacion reabierta = new Aplicacion(rutaBD);
        assertEquals("4 -> 5", reabierta.turnos.colaComoTexto());

        // 7. Atender los restantes
        assertEquals(4, reabierta.turnos.atenderSiguiente().orElseThrow().getNumero());
        assertEquals(5, reabierta.turnos.atenderSiguiente().orElseThrow().getNumero());

        // 8. No hay pendientes
        assertTrue(reabierta.turnos.atenderSiguiente().isEmpty());

        // 9. Historial de 1003
        List<Turno> historial = reabierta.turnos.historial(1003);
        assertEquals(1, historial.size());
        assertEquals(3, historial.get(0).getNumero());
        assertEquals(EstadoTurno.CANCELADO, historial.get(0).getEstado());

        // 10. Resumen por servicio
        List<ResumenServicio> resumen = reabierta.reportes.resumenPorServicio();
        assertEquals(List.of(
                new ResumenServicio(1, "Informacion", 0, 1, 1),
                new ResumenServicio(2, "Entrega de documentos", 0, 2, 0),
                new ResumenServicio(3, "Actualizacion de datos", 0, 1, 0)), resumen);
        ResumenServicio total = reabierta.reportes.totalGeneral(resumen);
        assertEquals(0, total.pendientes());
        assertEquals(4, total.atendidos());
        assertEquals(1, total.cancelados());
        assertEquals(5, total.total());
    }

    @Test
    void reglasDeEstadosYValidaciones(@TempDir Path carpeta) {
        Aplicacion app = new Aplicacion(carpeta.resolve("turnos.db"));

        // Validaciones de clientes
        assertThrows(ReglaNegocioException.class, () -> app.clientes.registrar(1001, "Repetido", "12345678"));
        assertThrows(ReglaNegocioException.class, () -> app.clientes.registrar(2000, "   ", "12345678"));
        assertThrows(ReglaNegocioException.class, () -> app.clientes.registrar(2000, "Nuevo", "1234"));
        assertThrows(ReglaNegocioException.class, () -> app.clientes.registrar(-5, "Nuevo", "12345678"));
        assertThrows(ReglaNegocioException.class, () -> app.clientes.buscar(9999));

        // No desactivar cliente ni servicio con turno pendiente
        app.turnos.generar(1001, 1);
        assertThrows(ReglaNegocioException.class, () -> app.clientes.desactivar(1001));
        assertThrows(ReglaNegocioException.class, () -> app.servicios.desactivar(1));

        // Cliente inactivo no recibe turnos y conserva historial
        app.clientes.desactivar(1002);
        assertThrows(ReglaNegocioException.class, () -> app.turnos.generar(1002, 1));

        // Servicio inactivo no admite turnos
        app.servicios.desactivar(3);
        assertThrows(ReglaNegocioException.class, () -> app.turnos.generar(1003, 3));

        // No cancelar un turno ya atendido ni inexistente
        app.turnos.atenderSiguiente();
        assertThrows(ReglaNegocioException.class, () -> app.turnos.cancelar(1));
        assertThrows(ReglaNegocioException.class, () -> app.turnos.cancelar(99));

        // Actualizacion sincronizada BD - BST
        app.clientes.actualizar(1004, "Jose Hernandez Ruiz", "55509999");
        Aplicacion reabierta = new Aplicacion(carpeta.resolve("turnos.db"));
        assertEquals("Jose Hernandez Ruiz", reabierta.clientes.buscar(1004).getNombre());
        assertFalse(reabierta.clientes.buscar(1002).isActivo());

        // Numeros sin reutilizacion: el siguiente es 2
        assertEquals(2, reabierta.turnos.generar(1003, 1).getNumero());
    }
}
