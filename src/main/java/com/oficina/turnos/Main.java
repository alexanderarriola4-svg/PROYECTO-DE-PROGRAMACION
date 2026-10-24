package com.oficina.turnos;

import com.oficina.turnos.datos.AccesoDatosException;
import com.oficina.turnos.datos.ClienteDAO;
import com.oficina.turnos.datos.ClienteDAOSQLite;
import com.oficina.turnos.datos.ConexionSQLite;
import com.oficina.turnos.datos.InicializadorBD;
import com.oficina.turnos.datos.ServicioDAO;
import com.oficina.turnos.datos.ServicioDAOSQLite;
import com.oficina.turnos.datos.TurnoDAO;
import com.oficina.turnos.datos.TurnoDAOSQLite;
import com.oficina.turnos.estructuras.ArbolClientes;
import com.oficina.turnos.estructuras.ColaTurnos;
import com.oficina.turnos.logica.GestorClientes;
import com.oficina.turnos.logica.GestorReportes;
import com.oficina.turnos.logica.GestorServicios;
import com.oficina.turnos.logica.GestorTurnos;
import com.oficina.turnos.vista.EntradaConsola;
import com.oficina.turnos.vista.FinDeEntradaException;
import com.oficina.turnos.vista.MenuPrincipal;
import com.oficina.turnos.vista.VistaClientes;
import com.oficina.turnos.vista.VistaServicios;
import com.oficina.turnos.vista.VistaTurnos;

/**
 * Punto de entrada. Solo arma las piezas (base de datos, DAO, estructuras,
 * gestores y vistas), reconstruye el BST y la cola, y arranca el menu.
 */
public class Main {

    public static void main(String[] args) {
        try {
            ConexionSQLite conexion = new ConexionSQLite();
            boolean datosCargados = new InicializadorBD(conexion).inicializar();

            ClienteDAO clienteDAO = new ClienteDAOSQLite(conexion);
            ServicioDAO servicioDAO = new ServicioDAOSQLite(conexion);
            TurnoDAO turnoDAO = new TurnoDAOSQLite(conexion);

            ArbolClientes arbolClientes = new ArbolClientes();
            ColaTurnos colaTurnos = new ColaTurnos();

            GestorClientes gestorClientes = new GestorClientes(clienteDAO, turnoDAO, arbolClientes);
            GestorServicios gestorServicios = new GestorServicios(servicioDAO, turnoDAO);
            GestorTurnos gestorTurnos = new GestorTurnos(turnoDAO, colaTurnos, gestorClientes, gestorServicios);
            GestorReportes gestorReportes = new GestorReportes(turnoDAO, gestorClientes, gestorServicios);

            int clientes = gestorClientes.cargarArbolDesdeBD();
            int pendientes = gestorTurnos.reconstruirCola();

            System.out.println("Base de datos: " + conexion.getRutaBaseDatos().toAbsolutePath()
                    + (datosCargados ? " (creada con los datos iniciales)" : ""));
            System.out.println("Clientes cargados en el BST: " + clientes);
            System.out.println("Turnos pendientes reconstruidos en la cola: " + pendientes
                    + " [" + gestorTurnos.colaComoTexto() + "]");

            EntradaConsola entrada = new EntradaConsola(System.in);
            MenuPrincipal menu = new MenuPrincipal(entrada,
                    new VistaClientes(entrada, gestorClientes),
                    new VistaServicios(entrada, gestorServicios),
                    new VistaTurnos(entrada, gestorTurnos, gestorClientes, gestorServicios, gestorReportes));
            menu.iniciar();
        } catch (AccesoDatosException e) {
            System.err.println("[ERROR] No se pudo iniciar la aplicacion: " + e.getMessage());
            System.err.println("Verifique que se ejecuta desde la carpeta raiz del proyecto (donde esta pom.xml).");
            System.exit(1);
        } catch (FinDeEntradaException e) {
            System.out.println("\nEntrada finalizada. Cerrando la aplicacion.");
        }
    }
}
