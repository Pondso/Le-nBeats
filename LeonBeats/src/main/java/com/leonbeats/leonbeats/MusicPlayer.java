package com.leonbeats.leonbeats;

import javazoom.jl.player.advanced.AdvancedPlayer;
import javazoom.jl.decoder.Bitstream;
import javazoom.jl.decoder.Header;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Timer;

public class MusicPlayer {
    private AdvancedPlayer player;
    private boolean isPlaying;
    private List<Song> playlist;
    private int currentTrackIndex;
    private String musicFolderPath = "C:\\Users\\Luis Alfonso\\Music\\LeonBeats";
    private int pausedFrame = 0;
    private int totalFrames = 0;
    private final int framesPerSecond = 38;
    private Timer progresoTimer;
    private int tiempoTranscurrido = 0;

    public enum PlayerState {
        PLAYING, PAUSED, STOPPED
    }

    private PlayerState state = PlayerState.STOPPED;

    public MusicPlayer() {
        playlist = new ArrayList<>();
        currentTrackIndex = 0;
        loadMusicFiles();
    }

    private void loadMusicFiles() {
        File folder = new File(musicFolderPath);
        File[] files = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".mp3"));

        if (files != null) {
            for (File file : files) {
                System.out.println("Cargando: " + file.getName());
                playlist.add(new Song(file));
            }
        } else {
            System.out.println("No se encontraron archivos en: " + musicFolderPath);
        }
    }

    public void play() {
        if (playlist.isEmpty()) {
            System.out.println("No hay canciones en la playlist.");
            return;
        }

        if (state == PlayerState.PAUSED && pausedFrame > 0) {
            resume();
            return;
        }

        stop(); // detener cualquier reproducción previa
        state = PlayerState.PLAYING;

        try {
            Song currentSong = playlist.get(currentTrackIndex);
            InputStream input = new BufferedInputStream(new FileInputStream(currentSong.getFile()));
            player = new AdvancedPlayer(input);

            totalFrames = (int) ((currentSong.getDuration() / 1000.0) * framesPerSecond);
            pausedFrame = 0;
            tiempoTranscurrido = 0;

            new Thread(() -> {
                try {
                    isPlaying = true;
                    player.play();
                    isPlaying = false;
                    state = PlayerState.STOPPED;
                    if (progresoTimer != null) {
                        progresoTimer.stop();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();

            progresoTimer = new Timer(1000, e -> tiempoTranscurrido += 1000);
            progresoTimer.start();

            displayCurrentTrackInfo();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void resume() {
    try {
        Song currentSong = playlist.get(currentTrackIndex);

        System.out.println("▶ Reanudando desde frame: " + pausedFrame); // ✅ Aquí

        player = crearPlayerDesdeFrame(currentSong.getFile(), pausedFrame);

        state = PlayerState.PLAYING;

        new Thread(() -> {
            try {
                isPlaying = true;
                player.play();
                isPlaying = false;
                state = PlayerState.STOPPED;
                if (progresoTimer != null) {
                    progresoTimer.stop();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

        progresoTimer = new Timer(1000, e -> tiempoTranscurrido += 1000);
        progresoTimer.start();

        displayCurrentTrackInfo();

    } catch (Exception e) {
        e.printStackTrace();
    }
}

    private AdvancedPlayer crearPlayerDesdeFrame(File mp3File, int startFrame) throws Exception {
    FileInputStream fileInputStream = new FileInputStream(mp3File);
    BufferedInputStream bufferedInput = new BufferedInputStream(fileInputStream);
    
    Bitstream bitstream = new Bitstream(bufferedInput);
    int frameCount = 0;
    Header header;

    // 🔧 CAMBIO: Leer y descartar frames hasta llegar a startFrame
    while (frameCount < startFrame && (header = bitstream.readFrame()) != null) {
        bitstream.closeFrame();
        frameCount++;
    }

    // 🔧 CAMBIO: No cerrar el bitstream, sino seguir usando el mismo input stream
    return new AdvancedPlayer(bufferedInput); // Reproducirá desde el frame actual
}

    public void pause() {
        if (state == PlayerState.PLAYING) {
            state = PlayerState.PAUSED;

            if (progresoTimer != null) {
                progresoTimer.stop();
            }

            pausedFrame = calcularFrameActual();
            System.out.println("Canción pausada en frame: " + pausedFrame);

            if (player != null) {
                player.close();
            }

            isPlaying = false;
        }
    }

    public void stop() {
        state = PlayerState.STOPPED;
        if (player != null) {
            player.close();
        }
        isPlaying = false;
        pausedFrame = 0;
        tiempoTranscurrido = 0;

        if (progresoTimer != null) {
            progresoTimer.stop();
        }
    }

    public void playNext() {
        if (!playlist.isEmpty()) {
            currentTrackIndex = (currentTrackIndex + 1) % playlist.size();
            play();
        }
    }

    public void playPrevious() {
        if (!playlist.isEmpty()) {
            currentTrackIndex = (currentTrackIndex - 1 + playlist.size()) % playlist.size();
            play();
        }
    }

    private int calcularFrameActual() {
        int frame = (int) ((tiempoTranscurrido / 1000.0) * framesPerSecond);
        return Math.min(frame, totalFrames);
    }

    private void displayCurrentTrackInfo() {
        if (playlist.isEmpty()) return;

        Song currentSong = playlist.get(currentTrackIndex);
        System.out.println("\n=== Ahora sonando ===");
        System.out.println("Título: " + currentSong.getTitle());
        System.out.println("Artista: " + currentSong.getArtist());
        System.out.println("Álbum: " + currentSong.getAlbum());
        System.out.println("Duración: " + currentSong.getFormattedDuration());
        System.out.println("Archivo: " + currentSong.getFile().getName());
        System.out.println("=====================");
    }

    // Getters y Setters

    public Song getCurrentSong() {
        if (playlist.isEmpty()) return null;
        return playlist.get(currentTrackIndex);
    }

    public List<Song> getPlaylist() {
        return playlist;
    }

    public int getCurrentTrackIndex() {
        return currentTrackIndex;
    }

    public void setCurrentTrackIndex(int index) {
        if (index >= 0 && index < playlist.size()) {
            currentTrackIndex = index;
        }
    }

    public boolean isPlaying() {
        return state == PlayerState.PLAYING;
    }

    public boolean isPaused() {
        return state == PlayerState.PAUSED;
    }

    public PlayerState getState() {
        return state;
    }

    public void setPlaylist(List<Song> playlistSongs) {
        this.playlist = playlistSongs;
    }

    public void setMusicFolderPath(String path) {
        this.musicFolderPath = path;
        playlist.clear();
        loadMusicFiles();
    }

    public void setTiempoTranscurrido(int tiempo) {
        this.tiempoTranscurrido = tiempo;
    }

    public int getTiempoTranscurrido() {
        return tiempoTranscurrido;
    }

    public long getPausedFrameTime() {
        Song current = getCurrentSong();
        if (current != null && totalFrames > 0) {
            return (long) ((pausedFrame / (double) totalFrames) * current.getDuration());
        }
        return 0;
    }
}