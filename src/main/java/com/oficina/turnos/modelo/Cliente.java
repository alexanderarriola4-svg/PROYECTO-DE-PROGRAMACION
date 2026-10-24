package com.oficina.turnos.modelo;

import java.util.Objects;

/**
 * Cliente de la oficina. El codigo es la clave del BST y de la tabla
 * "clientes"; por eso es final y no puede modificarse.
 */
public class Cliente {

    private final int codigo;
    private String nombre;
    private String telefono;
    private boolean activo;

    public Cliente(int codigo, String nombre, String telefono, boolean activo) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.telefono = telefono;
        this.activo = activo;
    }

    public int getCodigo() {
        return codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public String getEstadoTexto() {
        return activo ? "Activo" : "Inactivo";
    }

    @Override
    public boolean equals(Object otro) {
        if (this == otro) {
            return true;
        }
        if (!(otro instanceof Cliente cliente)) {
            return false;
        }
        return codigo == cliente.codigo;
    }

    @Override
    public int hashCode() {
        return Objects.hash(codigo);
    }

    @Override
    public String toString() {
        return codigo + " - " + nombre;
    }
}
