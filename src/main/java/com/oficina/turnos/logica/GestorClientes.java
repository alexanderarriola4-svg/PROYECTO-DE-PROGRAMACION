package com.oficina.turnos.logica;

import com.oficina.turnos.datos.ClienteDAO;
import com.oficina.turnos.datos.TurnoDAO;
import com.oficina.turnos.estructuras.ArbolClientes;
import com.oficina.turnos.modelo.Cliente;

import java.util.List;

/**
 * Reglas de negocio de clientes. Coordina la base de datos (fuente persistente)
 * con el BST (estructura en memoria).
 *
 * Orden de cada operacion: validar -> guardar en BD -> actualizar el arbol.
 * Si la BD falla se lanza una excepcion antes de tocar el arbol, de modo que
 * el arbol nunca muestra cambios que no se guardaron.
 */
public class GestorClientes {

    private final ClienteDAO clienteDAO;
    private final TurnoDAO turnoDAO;
    private final ArbolClientes arbol;

    public GestorClientes(ClienteDAO clienteDAO, TurnoDAO turnoDAO, ArbolClientes arbol) {
        this.clienteDAO = clienteDAO;
        this.turnoDAO = turnoDAO;
        this.arbol = arbol;
    }

    /** Construye el BST con los clientes guardados en SQLite. */
    public int cargarArbolDesdeBD() {
        arbol.vaciar();
        for (Cliente cliente : clienteDAO.listarTodos()) {
            arbol.insertar(cliente);
        }
        return arbol.tamanio();
    }

    public Cliente registrar(int codigo, String nombre, String telefono) {
        Validador.validarCodigo(codigo, "cliente");
        String nombreLimpio = Validador.validarTextoObligatorio(nombre, "nombre completo");
        String telefonoLimpio = Validador.validarTelefono(telefono);
        if (arbol.contiene(codigo)) {
            throw new ReglaNegocioException("Ya existe un cliente con el codigo " + codigo + ".");
        }
        Cliente nuevo = new Cliente(codigo, nombreLimpio, telefonoLimpio, true);
        clienteDAO.insertar(nuevo);
        arbol.insertar(nuevo);
        return nuevo;
    }

    /** Clientes ordenados por codigo (recorrido inorden del BST). */
    public List<Cliente> listar() {
        return arbol.inorden();
    }

    /** Busca en el BST. Lanza excepcion si el codigo no esta registrado. */
    public Cliente buscar(int codigo) {
        Validador.validarCodigo(codigo, "cliente");
        Cliente cliente = arbol.buscar(codigo);
        if (cliente == null) {
            throw new ReglaNegocioException("No existe un cliente registrado con el codigo " + codigo + ".");
        }
        return cliente;
    }

    /** Busca en el BST sin lanzar excepcion; devuelve null si no existe. */
    public Cliente buscarSiExiste(int codigo) {
        return arbol.buscar(codigo);
    }

    public Cliente actualizar(int codigo, String nombre, String telefono) {
        Cliente actual = buscar(codigo);
        String nombreLimpio = Validador.validarTextoObligatorio(nombre, "nombre completo");
        String telefonoLimpio = Validador.validarTelefono(telefono);
        Cliente actualizado = new Cliente(codigo, nombreLimpio, telefonoLimpio, actual.isActivo());
        clienteDAO.actualizar(actualizado);   // 1) base de datos
        arbol.actualizar(actualizado);        // 2) arbol, solo si la BD tuvo exito
        return actualizado;
    }

    public Cliente activar(int codigo) {
        Cliente cliente = buscar(codigo);
        if (cliente.isActivo()) {
            throw new ReglaNegocioException("El cliente " + cliente + " ya esta activo.");
        }
        clienteDAO.cambiarEstado(codigo, true);
        cliente.setActivo(true);
        return cliente;
    }

    public Cliente desactivar(int codigo) {
        Cliente cliente = buscar(codigo);
        if (!cliente.isActivo()) {
            throw new ReglaNegocioException("El cliente " + cliente + " ya esta inactivo.");
        }
        if (turnoDAO.contarPendientesPorCliente(codigo) > 0) {
            throw new ReglaNegocioException("No se puede desactivar al cliente " + cliente
                    + " porque tiene un turno pendiente. Atiendalo o cancelelo primero.");
        }
        clienteDAO.cambiarEstado(codigo, false);
        cliente.setActivo(false);
        return cliente;
    }

    public int alturaArbol() {
        return arbol.altura();
    }
}
