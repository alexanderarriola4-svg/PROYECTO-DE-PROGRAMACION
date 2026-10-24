package com.oficina.turnos.vista;

/** Menu principal: solo muestra opciones y delega en las vistas especializadas. */
public class MenuPrincipal extends MenuBase {

    /** Nombre personalizable de la oficina. */
    public static final String NOMBRE_OFICINA = "Oficina de Servicios Integrados";

    /** Datos del autor que se muestran en el encabezado del menu. */
    public static final String AUTOR = "Christopher Guillermo Alexander Arriola Villagran";
    public static final String CARNE = "1990-25-21997";

    private final VistaClientes vistaClientes;
    private final VistaServicios vistaServicios;
    private final VistaTurnos vistaTurnos;

    public MenuPrincipal(EntradaConsola entrada, VistaClientes vistaClientes,
                         VistaServicios vistaServicios, VistaTurnos vistaTurnos) {
        super(entrada);
        this.vistaClientes = vistaClientes;
        this.vistaServicios = vistaServicios;
        this.vistaTurnos = vistaTurnos;
    }

    public void iniciar() {
        int opcion;
        do {
            mostrarOpciones();
            opcion = entrada.leerEntero("Seleccione una opcion: ");
            switch (opcion) {
                case 1 -> vistaClientes.mostrarMenu();
                case 2 -> vistaServicios.mostrarMenu();
                case 3 -> ejecutarSeguro(vistaTurnos::generarTurno);
                case 4 -> ejecutarSeguro(vistaTurnos::atenderSiguiente);
                case 5 -> ejecutarSeguro(vistaTurnos::cancelarTurno);
                case 6 -> ejecutarSeguro(vistaTurnos::consultarPendientes);
                case 7 -> ejecutarSeguro(vistaTurnos::consultarAtendidos);
                case 8 -> ejecutarSeguro(vistaTurnos::consultarCancelados);
                case 9 -> ejecutarSeguro(vistaTurnos::historialCliente);
                case 10 -> ejecutarSeguro(vistaTurnos::resumenPorServicio);
                case 0 -> System.out.println("\nGracias por usar el sistema de turnos de " + NOMBRE_OFICINA + ". Hasta pronto!");
                default -> opcionInvalida(opcion);
            }
        } while (opcion != 0);
    }

    private void mostrarOpciones() {
        System.out.println();
        System.out.println("==================================================");
        System.out.println("  " + NOMBRE_OFICINA.toUpperCase());
        System.out.println("  Sistema de atencion por turnos");
        System.out.println("  Desarrollado por: " + AUTOR + " (" + CARNE + ")");
        System.out.println("==================================================");
        System.out.println("  1. Gestion de clientes");
        System.out.println("  2. Gestion de servicios");
        System.out.println("  3. Generar turno");
        System.out.println("  4. Atender siguiente turno");
        System.out.println("  5. Cancelar turno");
        System.out.println("  6. Consultar turnos pendientes");
        System.out.println("  7. Consultar turnos atendidos");
        System.out.println("  8. Consultar turnos cancelados");
        System.out.println("  9. Historial de un cliente");
        System.out.println(" 10. Resumen por servicio");
        System.out.println("  0. Salir");
    }
}
