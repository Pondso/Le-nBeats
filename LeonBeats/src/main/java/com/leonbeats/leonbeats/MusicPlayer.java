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

    public void play() {
    if (playlist.isEmpty()) {
        System.out.println("No hay canciones en la playlist.");
        return;
    }

    stop(); // 💡 Siempre paramos para evitar conflictos, incluso si estaba PAUSED

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
        });

        new Thread(() -> {
            try {
                isPlaying = true;
                player.play();
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
