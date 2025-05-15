<<<<<<< HEAD
package com.leonbeats.leonbeats;

import javazoom.jl.player.advanced.AdvancedPlayer;
import javazoom.jl.player.advanced.PlaybackEvent;
import javazoom.jl.player.advanced.PlaybackListener;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class MusicPlayer {
    private AdvancedPlayer player;
    private boolean isPlaying;
    private List<Song> playlist;
    private int currentTrackIndex;
    private String musicFolderPath = "C:\\Users\\Luis Alfonso\\Music\\LeonBeats";
    private int pausedFrame = 0;

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

    public void play(int startFrame) {
    if (playlist.isEmpty()) {
        System.out.println("No hay canciones en la playlist.");
        return;
    }

    stop(); // evitar conflictos

    state = PlayerState.PLAYING;

    try {
        Song currentSong = playlist.get(currentTrackIndex);
        InputStream input = new BufferedInputStream(new FileInputStream(currentSong.getFile()));
        player = new AdvancedPlayer(input);

        player.setPlayBackListener(new PlaybackListener() {
            @Override
            public void playbackFinished(PlaybackEvent evt) {
                isPlaying = false;
                state = PlayerState.STOPPED;
                playNext();
            }

            @Override
            public void playbackStarted(PlaybackEvent evt) {
                isPlaying = true;
            }
        });

        new Thread(() -> {
            try {
                player.play(startFrame, Integer.MAX_VALUE);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

        displayCurrentTrackInfo();

    } catch (Exception e) {
        e.printStackTrace();
    }
}

    public void pause() {
    if (state == PlayerState.PLAYING) {
        state = PlayerState.PAUSED;
        if (player != null) {
            pausedFrame = player.getPosition(); // Guarda la posición en bytes, no frames
            player.close();
            isPlaying = false;
        }
    }
}

    public void stop() {
        state = PlayerState.STOPPED;
        if (player != null) {
            player.close();
            isPlaying = false;
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
}
=======
package com.leonbeats.leonbeats;

import javazoom.jl.player.advanced.AdvancedPlayer;
import javazoom.jl.player.advanced.PlaybackEvent;
import javazoom.jl.player.advanced.PlaybackListener;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class MusicPlayer {
    private AdvancedPlayer player;
    private boolean isPlaying;
    private List<Song> playlist;
    private int currentTrackIndex;
    private String musicFolderPath = "C:\\Users\\Luis Alfonso\\Music\\LeonBeats";
    
    // Estados del reproductor
    public enum PlayerState {
        PLAYING, PAUSED, STOPPED
    }
    private PlayerState state = PlayerState.STOPPED;

    public MusicPlayer() {
        playlist = new ArrayList<>();
        currentTrackIndex = 0;
        loadMusicFiles();
    }

    // Carga todos los archivos MP3 de la carpeta especificada
    private void loadMusicFiles() {
        File folder = new File(musicFolderPath);
        File[] files = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".mp3"));

        if (files != null) {
            for (File file : files) {
                System.out.println("Cargando: " + file.getName()); // Debug temporal
                playlist.add(new Song(file));
            }
        } else {
            System.out.println("No se encontraron archivos en: " + musicFolderPath);
        }
}

    // Reproduce la canción actual
    public void play() {
        if (playlist.isEmpty()) {
            System.out.println("No hay canciones en la playlist.");
            return;
        }

        // Si ya está reproduciendo, no hacer nada
        if (state == PlayerState.PLAYING) {
            return;
        }

        // Si estaba pausado, continuar reproducción
        if (state == PlayerState.PAUSED) {
            state = PlayerState.PLAYING;
            return;
        }

        // Nueva reproducción
        state = PlayerState.PLAYING;
        stop(); // Asegurarse de detener cualquier reproducción previa

        try {
            Song currentSong = playlist.get(currentTrackIndex);
            InputStream input = new BufferedInputStream(new FileInputStream(currentSong.getFile()));
            player = new AdvancedPlayer(input);

            player.setPlayBackListener(new PlaybackListener() {
                @Override
                public void playbackFinished(PlaybackEvent evt) {
                    isPlaying = false;
                    state = PlayerState.STOPPED;
                    playNext(); // Reproduce la siguiente canción al terminar
                }
            });

            new Thread(() -> {
                try {
                    isPlaying = true;
                    player.play();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();

            // Mostrar información de la canción actual
            displayCurrentTrackInfo();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Pausa la reproducción
    public void pause() {
        if (state == PlayerState.PLAYING) {
            state = PlayerState.PAUSED;
            if (player != null) {
                player.close();
                isPlaying = false;
            }
        }
    }

    // Detiene la reproducción
    public void stop() {
        state = PlayerState.STOPPED;
        if (player != null) {
            player.close();
            isPlaying = false;
        }
    }

    // Reproduce la siguiente canción
    public void playNext() {
        if (!playlist.isEmpty()) {
            currentTrackIndex = (currentTrackIndex + 1) % playlist.size();
            play();
        }
    }

    // Reproduce la canción anterior
    public void playPrevious() {
        if (!playlist.isEmpty()) {
            currentTrackIndex = (currentTrackIndex - 1 + playlist.size()) % playlist.size();
            play();
        }
    }

    // Muestra información de la canción actual
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

    // Obtiene la canción actual
    public Song getCurrentSong() {
        if (playlist.isEmpty()) return null;
        return playlist.get(currentTrackIndex);
    }

    // Obtiene la lista de reproducción
    public List<Song> getPlaylist() {
        return playlist;
    }

    // Obtiene el índice de la canción actual
    public int getCurrentTrackIndex() {
        return currentTrackIndex;
    }

    // Establece la canción actual por índice
    public void setCurrentTrackIndex(int index) {
        if (index >= 0 && index < playlist.size()) {
            currentTrackIndex = index;
        }
    }

    // Verifica si está reproduciendo
    public boolean isPlaying() {
        return state == PlayerState.PLAYING;
    }

    // Verifica si está pausado
    public boolean isPaused() {
        return state == PlayerState.PAUSED;
    }

    // Obtiene el estado actual del reproductor
    public PlayerState getState() {
        return state;
    }

    // Establece una nueva carpeta de música
    public void setMusicFolderPath(String path) {
        this.musicFolderPath = path;
        playlist.clear();
        loadMusicFiles();
    }
    
}
>>>>>>> 1654dcc8dba7b5d09d3e6dabbaabff0c335b0855
