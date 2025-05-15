package com.leonbeats.leonbeats;

public class Usuario {
    private String codigo;
    private String contraseña;
    private String nombre;
    private String fotoRuta;
    private boolean admin;

    public Usuario(String codigo, String contraseña, String nombre, String fotoRuta, boolean admin) {
        this.codigo = codigo;
        this.contraseña = contraseña;
        this.nombre = nombre;
        this.fotoRuta = fotoRuta;
        this.admin = admin;
    }

    // Constructor sin fotoRuta (puede deducir si es admin por el nombre)
    public Usuario(String codigo, String contraseña, String nombre) {
        this.codigo = codigo;
        this.contraseña = contraseña;
        this.nombre = nombre;
        this.fotoRuta = "";
        this.admin = nombre != null && nombre.trim().equalsIgnoreCase("admin");
    }

    public String getCodigo() {
        return codigo;
    }

    public String getContraseña() {
        return contraseña;
    }

    public String getNombre() {
        return nombre;
    }

    public String getFotoRuta() {
        return fotoRuta;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
        // Actualiza admin si el nuevo nombre es "admin"
        this.admin = nombre != null && nombre.trim().equalsIgnoreCase("admin");
    }

    public void setContraseña(String nuevaContraseña) {
        this.contraseña = nuevaContraseña;
    }

    public void setFotoRuta(String fotoRuta) {
        this.fotoRuta = fotoRuta;
    }

    public boolean isAdmin() {
        return admin;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }
    
    public String getAlias() {
        return nombre; // Si "alias" debe ser distinto, cámbialo al atributo correcto
    }

    @Override
    public String toString() {
        return codigo + "," + contraseña + "," + nombre + "," + fotoRuta + "," + admin;
    }
}