package com.leonbeats.leonbeats;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Playlist {
    private String nombre;
    private String creadorAlias;
    private String caratulaRuta;
    private List<String> canciones;

    public Playlist(String nombre, String creadorAlias, String caratulaRuta, List<String> canciones) {
        this.nombre = (nombre == null || nombre.isEmpty()) ? "Nueva Playlist" : nombre;
        this.creadorAlias = (creadorAlias == null || creadorAlias.isEmpty()) ? "Desconocido" : creadorAlias;
        this.caratulaRuta = caratulaRuta;
        this.canciones = (canciones != null) ? canciones : new ArrayList<>();
    }

    public boolean guardarPlaylist() {
        File carpeta = new File("playlists");
        if (!carpeta.exists()) {
            carpeta.mkdirs();
        }

        File archivo = new File(carpeta, nombre + ".playlist");
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(archivo))) {
            bw.write("nombre:" + nombre);
            bw.newLine();
            bw.write("creador:" + creadorAlias);
            bw.newLine();
            bw.write("caratula:" + (caratulaRuta != null ? caratulaRuta : ""));
            bw.newLine();
            bw.write("canciones:");
            for (int i = 0; i < canciones.size(); i++) {
                bw.write(canciones.get(i));
                if (i < canciones.size() - 1) bw.write(";");
            }
            bw.newLine();
            return true;
        } catch (IOException e) {
            System.out.println("Error al guardar la playlist: " + e.getMessage());
            return false;
        }
    }

    public static Playlist cargarDesdeArchivo(File archivo) {
        String nombre = "Nueva Playlist";
        String creador = "Desconocido";
        String caratula = "";
        List<String> canciones = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                if (linea.startsWith("nombre:")) {
                    nombre = linea.substring("nombre:".length()).trim();
                } else if (linea.startsWith("creador:")) {
                    creador = linea.substring("creador:".length()).trim();
                } else if (linea.startsWith("caratula:")) {
                    caratula = linea.substring("caratula:".length()).trim();
                } else if (linea.startsWith("canciones:")) {
                    String[] partes = linea.substring("canciones:".length()).split(";");
                    for (String path : partes) {
                        if (!path.isEmpty()) canciones.add(path.trim());
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Error al leer playlist: " + e.getMessage());
        }

        return new Playlist(nombre, creador, caratula, canciones);
    }

    public String getNombre() {
        return nombre;
    }

    public String getCreadorAlias() {
        return creadorAlias;
    }

    public String getCaratulaRuta() {
        return caratulaRuta;
    }

    public List<String> getCanciones() {
        return canciones;
    }

    public void agregarCancion(String ruta) {
        if (ruta != null && !ruta.isEmpty() && !canciones.contains(ruta)) {
            canciones.add(ruta);
        }
    }

    public void setCaratulaRuta(String ruta) {
        this.caratulaRuta = ruta;
    }
    
    public String getAlias() {
        return nombre; 
    }
   
}