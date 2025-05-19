package com.leonbeats.leonbeats;

import java.io.InputStream;
import java.util.Scanner;

/**
 *
 * @author Luis Alfonso
 */
public class VolumeControl {

    private static final String EXE_PATH = "C:\\Users\\Luis Alfonso\\OneDrive\\Documentos\\SoundVolumeView.exe";
    private static final String DEVICE_NAME = "Altavoces";

    // Establecer volumen (0 a 100)
    public static void setSystemVolume(int volume) {
        executeCommand("\"" + EXE_PATH + "\" /SetVolume \"" + DEVICE_NAME + "\" " + volume);
    }

    // Mutear el dispositivo
    public static void muteSystemVolume() {
        executeCommand("\"" + EXE_PATH + "\" /Mute \"" + DEVICE_NAME + "\"");
    }

    // Desmutear el dispositivo
    public static void unmuteSystemVolume() {
        executeCommand("\"" + EXE_PATH + "\" /Unmute \"" + DEVICE_NAME + "\"");
    }

    // Obtener y mostrar el volumen actual
    public static void printCurrentVolume() {
        try {
            Process process = Runtime.getRuntime().exec("\"" + EXE_PATH + "\" /GetPercent \"" + DEVICE_NAME + "\"");
            InputStream input = process.getInputStream();
            Scanner scanner = new Scanner(input);
            while (scanner.hasNextLine()) {
                System.out.println("Volumen actual: " + scanner.nextLine() + "%");
            }
            scanner.close();
        } catch (Exception e) {
            System.err.println("Error al obtener el volumen: " + e.getMessage());
        }
    }

    // Método interno para ejecutar comandos sin salida
    private static void executeCommand(String command) {
        try {
            Runtime.getRuntime().exec(command);
        } catch (Exception e) {
            System.err.println("Error ejecutando comando: " + e.getMessage());
        }
    }
}

