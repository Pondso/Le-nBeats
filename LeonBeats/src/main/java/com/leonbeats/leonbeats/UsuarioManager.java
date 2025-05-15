<<<<<<< HEAD
package com.leonbeats.leonbeats;

import java.io.*;

public class UsuarioManager {
    private static final String ARCHIVO_USUARIOS = "usuarios.txt"; // Archivo principal

public static boolean verificarUsuario(String codigo, String contraseña) {
    try (BufferedReader br = new BufferedReader(new FileReader(ARCHIVO_USUARIOS))) {
        String linea;
        while ((linea = br.readLine()) != null) {
            String[] partes = linea.split(",", -1);
            if (partes.length >= 2 && partes[0].equals(codigo) && partes[1].equals(contraseña)) {
                return true;
            }
        }
    } catch (IOException e) {
        System.out.println("Error al leer usuarios: " + e.getMessage());
    }
    return false;
}
    public static boolean registrarUsuario(Usuario nuevo) {
        // Verifica si el usuario ya existe por su código
        if (existeUsuario(nuevo.getCodigo())) return false;

        // Asegura que los campos no sean null antes de escribirlos en el archivo
        if (nuevo.getNombre() == null) nuevo.setNombre("");
        if (nuevo.getFotoRuta() == null) nuevo.setFotoRuta("");
    
        // Asegura que el campo "admin" esté correctamente calculado si no se estableció
        if (nuevo.getNombre().trim().equalsIgnoreCase("admin")) {
            nuevo.setAdmin(true);
        }

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(ARCHIVO_USUARIOS, true))) {
            bw.write(nuevo.toString());
            bw.newLine();
            return true;
        } catch (IOException e) {
            System.out.println("Error al guardar usuario: " + e.getMessage());
            return false;
        }
    }

    public static boolean existeUsuario(String codigo) {
        try (BufferedReader br = new BufferedReader(new FileReader(ARCHIVO_USUARIOS))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                String[] partes = linea.split(",", 3);
                if (partes[0].equals(codigo)) return true;
            }
        } catch (IOException e) {
            return false;
        }
        return false;
    }

    public static void actualizarUsuario(Usuario actualizado) {
        File archivoOriginal = new File(ARCHIVO_USUARIOS);
        File archivoTemporal = new File("usuarios_temp.txt");

        try (BufferedReader br = new BufferedReader(new FileReader(archivoOriginal));
             BufferedWriter bw = new BufferedWriter(new FileWriter(archivoTemporal))) {

            String linea;
            while ((linea = br.readLine()) != null) {
                String[] partes = linea.split(",", 4);
                if (partes.length >= 2 && partes[0].equals(actualizado.getCodigo())) {
                    bw.write(actualizado.toString()); // Línea actualizada
                } else {
                    bw.write(linea); // Línea original
                }
                bw.newLine();
            }

        } catch (IOException e) {
            System.out.println("Error al actualizar usuario: " + e.getMessage());
            return;
        }

        // Reemplazo seguro del archivo
        if (!archivoOriginal.delete()) {
            System.out.println("No se pudo eliminar el archivo original.");
            return;
        }

        if (!archivoTemporal.renameTo(archivoOriginal)) {
            System.out.println("No se pudo renombrar el archivo temporal.");
        }
    }

    public static Usuario obtenerUsuario(String codigo, String contraseña) {
    try (BufferedReader br = new BufferedReader(new FileReader(ARCHIVO_USUARIOS))) {
        String linea;
        while ((linea = br.readLine()) != null) {
            String[] partes = linea.split(",", -1); // Permite campos vacíos
            if (partes.length >= 3 && partes[0].equals(codigo) && partes[1].equals(contraseña)) {
                String nombre = partes[2];
                String fotoRuta = partes.length >= 4 ? partes[3] : "";
                boolean admin = partes.length >= 5 && Boolean.parseBoolean(partes[4].trim());
                return new Usuario(codigo, contraseña, nombre, fotoRuta, admin);
            }
        }
    } catch (IOException e) {
        System.out.println("Error al obtener usuario: " + e.getMessage());
    }
    return null;
}

public static void limpiarYCorregirArchivoUsuarios() {
    File archivo = new File("usuarios.txt");
    File archivoTemporal = new File("usuarios_temp.txt");

    try (BufferedReader br = new BufferedReader(new FileReader(archivo));
         BufferedWriter bw = new BufferedWriter(new FileWriter(archivoTemporal))) {

        String linea;
        while ((linea = br.readLine()) != null) {
            linea = linea.trim();
            if (linea.isEmpty()) continue;

            String[] partes = linea.split(",", -1);
            String codigo = partes.length > 0 ? partes[0] : "";
            String contraseña = partes.length > 1 ? partes[1] : "";
            String nombre = partes.length > 2 ? partes[2] : "";
            String ruta = partes.length > 3 ? partes[3] : "";
            String adminStr = "false";

            if (nombre.equalsIgnoreCase("admin")) {
                adminStr = "true";
            }

            // Si la línea ya tiene admin como parte 5, usa eso
            if (partes.length > 4) {
                adminStr = partes[4];
            }

            bw.write(codigo + "," + contraseña + "," + nombre + "," + ruta + "," + adminStr);
            bw.newLine();
        }

    } catch (IOException e) {
        System.out.println("Error al procesar el archivo: " + e.getMessage());
        return;
    }

    // Intentar borrar y renombrar
    if (!archivo.delete()) {
        System.out.println("⚠ No se pudo eliminar el archivo original. Asegúrate de que no esté en uso.");
        return;
    }

    try {
        java.nio.file.Files.move(archivoTemporal.toPath(), archivo.toPath());
        System.out.println("✅ Archivo corregido y reemplazado correctamente.");
    } catch (IOException e) {
        System.out.println("❌ No se pudo renombrar el archivo temporal: " + e.getMessage());
    }
}
}
=======
package com.leonbeats.leonbeats;

import java.io.*;

public class UsuarioManager {
    private static final String ARCHIVO_USUARIOS = "usuarios.txt"; // Archivo principal

public static boolean verificarUsuario(String codigo, String contraseña) {
    try (BufferedReader br = new BufferedReader(new FileReader(ARCHIVO_USUARIOS))) {
        String linea;
        while ((linea = br.readLine()) != null) {
            String[] partes = linea.split(",", -1);
            if (partes.length >= 2 && partes[0].equals(codigo) && partes[1].equals(contraseña)) {
                return true;
            }
        }
    } catch (IOException e) {
        System.out.println("Error al leer usuarios: " + e.getMessage());
    }
    return false;
}
    public static boolean registrarUsuario(Usuario nuevo) {
        // Verifica si el usuario ya existe por su código
        if (existeUsuario(nuevo.getCodigo())) return false;

        // Asegura que los campos no sean null antes de escribirlos en el archivo
        if (nuevo.getNombre() == null) nuevo.setNombre("");
        if (nuevo.getFotoRuta() == null) nuevo.setFotoRuta("");
    
        // Asegura que el campo "admin" esté correctamente calculado si no se estableció
        if (nuevo.getNombre().trim().equalsIgnoreCase("admin")) {
            nuevo.setAdmin(true);
        }

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(ARCHIVO_USUARIOS, true))) {
            bw.write(nuevo.toString());
            bw.newLine();
            return true;
        } catch (IOException e) {
            System.out.println("Error al guardar usuario: " + e.getMessage());
            return false;
        }
    }

    public static boolean existeUsuario(String codigo) {
        try (BufferedReader br = new BufferedReader(new FileReader(ARCHIVO_USUARIOS))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                String[] partes = linea.split(",", 3);
                if (partes[0].equals(codigo)) return true;
            }
        } catch (IOException e) {
            return false;
        }
        return false;
    }

    public static void actualizarUsuario(Usuario actualizado) {
        File archivoOriginal = new File(ARCHIVO_USUARIOS);
        File archivoTemporal = new File("usuarios_temp.txt");

        try (BufferedReader br = new BufferedReader(new FileReader(archivoOriginal));
             BufferedWriter bw = new BufferedWriter(new FileWriter(archivoTemporal))) {

            String linea;
            while ((linea = br.readLine()) != null) {
                String[] partes = linea.split(",", 4);
                if (partes.length >= 2 && partes[0].equals(actualizado.getCodigo())) {
                    bw.write(actualizado.toString()); // Línea actualizada
                } else {
                    bw.write(linea); // Línea original
                }
                bw.newLine();
            }

        } catch (IOException e) {
            System.out.println("Error al actualizar usuario: " + e.getMessage());
            return;
        }

        // Reemplazo seguro del archivo
        if (!archivoOriginal.delete()) {
            System.out.println("No se pudo eliminar el archivo original.");
            return;
        }

        if (!archivoTemporal.renameTo(archivoOriginal)) {
            System.out.println("No se pudo renombrar el archivo temporal.");
        }
    }

    public static Usuario obtenerUsuario(String codigo, String contraseña) {
    try (BufferedReader br = new BufferedReader(new FileReader(ARCHIVO_USUARIOS))) {
        String linea;
        while ((linea = br.readLine()) != null) {
            String[] partes = linea.split(",", -1); // Permite campos vacíos
            if (partes.length >= 3 && partes[0].equals(codigo) && partes[1].equals(contraseña)) {
                String nombre = partes[2];
                String fotoRuta = partes.length >= 4 ? partes[3] : "";
                boolean admin = partes.length >= 5 && Boolean.parseBoolean(partes[4].trim());
                return new Usuario(codigo, contraseña, nombre, fotoRuta, admin);
            }
        }
    } catch (IOException e) {
        System.out.println("Error al obtener usuario: " + e.getMessage());
    }
    return null;
}

public static void limpiarYCorregirArchivoUsuarios() {
    File archivo = new File("usuarios.txt");
    File archivoTemporal = new File("usuarios_temp.txt");

    try (BufferedReader br = new BufferedReader(new FileReader(archivo));
         BufferedWriter bw = new BufferedWriter(new FileWriter(archivoTemporal))) {

        String linea;
        while ((linea = br.readLine()) != null) {
            linea = linea.trim();
            if (linea.isEmpty()) continue;

            String[] partes = linea.split(",", -1);
            String codigo = partes.length > 0 ? partes[0] : "";
            String contraseña = partes.length > 1 ? partes[1] : "";
            String nombre = partes.length > 2 ? partes[2] : "";
            String ruta = partes.length > 3 ? partes[3] : "";
            String adminStr = "false";

            if (nombre.equalsIgnoreCase("admin")) {
                adminStr = "true";
            }

            // Si la línea ya tiene admin como parte 5, usa eso
            if (partes.length > 4) {
                adminStr = partes[4];
            }

            bw.write(codigo + "," + contraseña + "," + nombre + "," + ruta + "," + adminStr);
            bw.newLine();
        }

    } catch (IOException e) {
        System.out.println("Error al procesar el archivo: " + e.getMessage());
        return;
    }

    // Intentar borrar y renombrar
    if (!archivo.delete()) {
        System.out.println("⚠ No se pudo eliminar el archivo original. Asegúrate de que no esté en uso.");
        return;
    }

    try {
        java.nio.file.Files.move(archivoTemporal.toPath(), archivo.toPath());
        System.out.println("✅ Archivo corregido y reemplazado correctamente.");
    } catch (IOException e) {
        System.out.println("❌ No se pudo renombrar el archivo temporal: " + e.getMessage());
    }
}
}
>>>>>>> 1654dcc8dba7b5d09d3e6dabbaabff0c335b0855
