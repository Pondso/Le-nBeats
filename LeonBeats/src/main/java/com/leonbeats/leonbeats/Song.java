package com.leonbeats.leonbeats;

/**
 *
 * @author Luis Alfonso
 */

import java.io.File;
import java.io.IOException;
import javax.swing.ImageIcon;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagException;
import org.jaudiotagger.tag.datatype.Artwork;

public class Song {
    private String title;
    private String artist;
    private String album;
    private String year;
    private String genre;
    private File file;
    private ImageIcon albumArt;
    private long duration; // en milisegundos

    public Song(File file) {
        this.file = file;
        this.title = file.getName(); // Nombre por defecto
        this.artist = "Desconocido";
        this.album = "Desconocido";
        this.year = "";
        this.genre = "";
        this.albumArt = null;
        this.duration = 0;
        
        loadMetadata(); // Cargar metadatos automáticamente al crear la canción
    }

    private void loadMetadata() {
        try {
            AudioFile audioFile = AudioFileIO.read(file);
            Tag tag = audioFile.getTag();

            if (tag != null) {
                this.title = getTagField(tag, FieldKey.TITLE, file.getName());
                this.artist = getTagField(tag, FieldKey.ARTIST, "Desconocido");
                this.album = getTagField(tag, FieldKey.ALBUM, "Desconocido");
                this.year = getTagField(tag, FieldKey.YEAR, "");
                this.genre = getTagField(tag, FieldKey.GENRE, "");
                
                // Calcular duración (aproximada)
                this.duration = audioFile.getAudioHeader().getTrackLength() * 1000L;
                
                // Cargar carátula del álbum
                Artwork artwork = tag.getFirstArtwork();
                if (artwork != null) {
                    byte[] imageData = artwork.getBinaryData();
                    this.albumArt = new ImageIcon(imageData);
                }
            }
        } catch (CannotReadException | IOException | TagException | ReadOnlyFileException 
                | InvalidAudioFrameException e) {
            System.out.println("Error al leer metadatos de: " + file.getName());
            // Mantener valores por defecto
        }
    }

    private String getTagField(Tag tag, FieldKey key, String defaultValue) {
        String value = tag.getFirst(key);
        return value == null || value.trim().isEmpty() ? defaultValue : value;
    }

    // Getters
    public String getTitle() { return title; }
    public String getArtist() { return artist; }
    public String getAlbum() { return album; }
    public String getYear() { return year; }
    public String getGenre() { return genre; }
    public File getFile() { return file; }
    public ImageIcon getAlbumArt() { return albumArt; }
    public long getDuration() { return duration; }

    // Formatear duración como string (mm:ss)
    public String getFormattedDuration() {
        long seconds = duration / 1000;
        long minutes = seconds / 60;
        seconds = seconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }
    public String getRutaArchivo() {
        return file.getAbsolutePath(); // Devuelve la ruta completa del archivo de la canción
    }

    @Override
    public String toString() {
        return artist + " - " + title;
    }
}
