package com.leonbeats.leonbeats;

public class PlaylistVisual {
    private String nombre;
    private String caratulaPath;

    public PlaylistVisual(String nombre, String caratulaPath) {
        this.nombre = nombre;
        this.caratulaPath = caratulaPath;
    }

    public String getNombre() { return nombre; }
    public String getCaratulaPath() { return caratulaPath; }

    @Override
    public String toString() {
        return nombre;
    }
}