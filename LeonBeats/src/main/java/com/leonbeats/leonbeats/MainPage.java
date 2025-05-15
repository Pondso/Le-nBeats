package com.leonbeats.leonbeats;
import com.vdurmont.emoji.EmojiParser;
import java.awt.Color;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.JOptionPane;

/**
 *
 * @author Luis Alfonso
 */

public class MainPage extends javax.swing.JFrame {
    
    //variables para el funcionamiento de varias funciones 
    int xMouse, yMouse; 
    private boolean isPlaying = false;
    private boolean isRandom = false;
    private int repeatState = 0;
    private int previousVolume = 100; 
    private boolean isMuted = false;
    List<Song> playlistSongs = new ArrayList<>();
    int currentPlaylistIndex = -1;
    DefaultListModel<String> playlistModel = new DefaultListModel<>(); 
    private Usuario usuarioActual;
    private ArrayList<String> canciones = new ArrayList<>();
    private DefaultListModel<PlaylistVisual> modeloPlaylists;
    private String caratulaRuta = "";
    private List<Song> cancionesReal = new ArrayList<>();
    private MusicPlayer player = new MusicPlayer();
    private boolean isLike = false;
    private List<Song> cancionesPendientes = new ArrayList<>();
    private MusicPlayer musicPlayer = new MusicPlayer();
    private Timer progresoTimer;
    private long tiempoTranscurrido; 

            
    
    //funcion para poder agregar canciones a la cola de reproducción dependiendo del index actual del la cola de reproducción
    public void addSongToQueue(Song newSong) {
    if (playlistSongs.isEmpty()) {
        playlistSongs.add(newSong);
        currentPlaylistIndex = 0;
    } else {
        int insertIndex;
        
        if (repeatState == 1) { // Estado en Repeat All
            insertIndex = (currentPlaylistIndex + 1) % (playlistSongs.size() + 1);
        } else {
            // Estado en Normal o Repeat One
            insertIndex = currentPlaylistIndex + 1;
        }

        playlistSongs.add(insertIndex, newSong);
    }

    updatePlaylistModel();
}
    
    //Función para agregar al final de la cola de reproducción 
    public void addSongToQueueAtEnd(Song newSong) {
    if (playlistSongs.isEmpty()) {
        playlistSongs.add(newSong);
        currentPlaylistIndex = 0;
    } else {
        int insertIndex;

        if (repeatState == 1) {
            // En Repeat All, insertar antes del actual (final lógico)
            insertIndex = (currentPlaylistIndex + playlistSongs.size()) % playlistSongs.size() + 1;
            if (insertIndex > playlistSongs.size()) insertIndex = playlistSongs.size();
        } else {
            // En otros modos, agregar al final
            insertIndex = playlistSongs.size();
        }

        playlistSongs.add(insertIndex, newSong);
    }

    updatePlaylistModel();
}
    
    private void iniciarBarraProgreso(Song song) {
    iniciarBarraProgreso(song, 0);
}
    
    //Función para agregar al inicio actual de la cola de reproducción
    public void playSongImmediately(Song newSong) {
    if (playlistSongs.isEmpty()) {
        playlistSongs.add(newSong);
        currentPlaylistIndex = 0;
    } else {
        // Insertar justo en el índice actual (antes de la que suena)
        playlistSongs.add(currentPlaylistIndex, newSong);
        // Ya que se insertó antes, la nueva canción está en currentPlaylistIndex
        // y la que estaba antes se mueve a +1, así que no cambiamos el índice
    }

    updateSongInfo(newSong);
    updatePlaylistModel();

    // 🔊 Reproducir la canción agregada inmediatamente
musicPlayer.setPlaylist(playlistSongs);
musicPlayer.setCurrentTrackIndex(currentPlaylistIndex);

// Verifica si el reproductor estaba en pausa
if (musicPlayer.getState() == MusicPlayer.PlayerState.PAUSED) {
    musicPlayer.resume(); // ✅ Reanuda desde donde se pausó
} else {
    musicPlayer.play(); // 🔁 Reproduce normalmente si no estaba pausado
    Song current = musicPlayer.getCurrentSong();
    if (current != null) {
        iniciarBarraProgreso(current); // ⏳ Solo reinicia barra si es una nueva reproducción
    }
}
    }
    
    //Función para actualizar la información de la canción que se esta reproduciendo 
    private void updateSongInfo(Song selectedSong) {
    if (selectedSong == null) return;

    NameSong.setText(selectedSong.getTitle());
    ArtistSong.setText(selectedSong.getArtist());
    AlbumSong.setText(selectedSong.getAlbum());
    DateSong.setText(selectedSong.getYear());
    NameSong2.setText(selectedSong.getTitle());
    ArtistSong2.setText(selectedSong.getArtist());
    

    ImageIcon art = selectedSong.getAlbumArt();
    if (art != null) {
        SongImg.setIcon(new ImageIcon(
            art.getImage().getScaledInstance(60, 60, Image.SCALE_SMOOTH)
        ));
        SongImg2.setIcon(new ImageIcon(
            art.getImage().getScaledInstance(240, 240, Image.SCALE_SMOOTH)
        ));
    } else {
        SongImg.setIcon(null);
        SongImg2.setIcon(null);
    }

    // Verificar si la canción está en la playlist "Me Gusta"
    String userPlaylistName = "MeGusta_" + usuarioActual.getAlias();
    File archivoPlaylist = new File("playlists/" + userPlaylistName + ".playlist");
    Playlist meGustaPlaylist = archivoPlaylist.exists() ? Playlist.cargarDesdeArchivo(archivoPlaylist) : new Playlist(userPlaylistName, usuarioActual.getAlias(), "", new ArrayList<>());

    isLike = meGustaPlaylist.getCanciones().contains(selectedSong.getRutaArchivo());

    // Actualizar visualización del botón Me Gusta
    HeartState.setForeground(isLike ? Color.GREEN : Color.GRAY);
    HeartState1.setForeground(isLike ? Color.GREEN : Color.GRAY);
}
    
    // Función para actualizar la lista de reproducción
    private void updatePlaylistModel() {
    playlistModel.clear();

    if (playlistSongs.isEmpty()) return;

    if (isRandom) {
        // Mostrar la lista de reproducción aleatoria
        for (Song song : cancionesPendientes) {
            playlistModel.addElement(song.getArtist() + " - " + song.getTitle());
        }
    } else if (repeatState == 1) {
        // Repeat All: mostrar desde la canción actual hacia adelante de forma circular
        for (int i = 0; i < playlistSongs.size(); i++) {
            int index = (currentPlaylistIndex + i) % playlistSongs.size();
            Song song = playlistSongs.get(index);
            playlistModel.addElement(song.getArtist() + " - " + song.getTitle());
        }
    } else {
        // Normal o Repeat One: mostrar desde la canción actual hasta el final
        for (int i = currentPlaylistIndex; i < playlistSongs.size(); i++) {
            Song song = playlistSongs.get(i);
            playlistModel.addElement(song.getArtist() + " - " + song.getTitle());
        }
    }
}
    
    //Función especifica para mostrar el emoji del usuario en caso de que no haya foto de perfil
    private void mostrarEmojiEnLabel() {
        String UserName = ":bust_in_silhouette:";
        String UserEmoji = EmojiParser.parseToUnicode(UserName);
        UserImg.setText(UserEmoji);
        UserImg.setIcon(null);
        UserImg.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20));
        UserImg.setForeground(Color.GRAY);
        UserImg.setHorizontalTextPosition(SwingConstants.CENTER);
        UserImg.setVerticalTextPosition(SwingConstants.CENTER);
        UserImg.setHorizontalAlignment(SwingConstants.CENTER);
        UserImg.setVerticalAlignment(SwingConstants.CENTER);
    }

    // Método para actualizar la imagen de perfil o emoji en el JLabel
    private void actualizarFotoDeUsuario() {
        if (usuarioActual != null) {
            String ruta = usuarioActual.getFotoRuta();

            if (ruta != null && !ruta.isEmpty()) {
                File imgFile = new File(ruta);
                if (imgFile.exists()) {
                    ImageIcon icono = new ImageIcon(ruta);
                    Image imagen = icono.getImage().getScaledInstance(
                        UserImg.getWidth(), UserImg.getHeight(), Image.SCALE_SMOOTH
                    );
                    UserImg.setIcon(new ImageIcon(imagen));
                    UserImg.setText(""); 
                } else {
                    mostrarEmojiEnLabel(); 
                }
            } else {
                mostrarEmojiEnLabel(); 
            }
        } else {
            mostrarEmojiEnLabel(); 
        }
    }
    
private void initListaPlaylists() {
    modeloPlaylists = new DefaultListModel<>();
    JList<PlaylistVisual> lista = new JList<>(modeloPlaylists);
    lista.setCellRenderer(new PlaylistRenderer());

    lista.addListSelectionListener(new ListSelectionListener() {
        @Override
        public void valueChanged(ListSelectionEvent e) {
            if (!e.getValueIsAdjusting()) {
                PlaylistVisual seleccionada = lista.getSelectedValue();
                if (seleccionada != null) {
                    mostrarPlaylistSeleccionada(seleccionada);
                }
            }
        }
    });

    cargarPlaylists(modeloPlaylists);
    ScrollPlaylist.setViewportView(lista);
}


public class ActualizarPlaylists {
    public static void main(String[] args) {
        File carpeta = new File("playlists");
        if (carpeta.exists() && carpeta.isDirectory()) {
            for (File archivo : carpeta.listFiles()) {
                if (archivo.getName().endsWith(".playlist")) {
                    Playlist p = Playlist.cargarDesdeArchivo(archivo);
                    if (p.getNombre().equals("Nueva Playlist")) {
                        String nombreArchivo = archivo.getName().replace(".playlist", "");
                        System.out.println("Corrigiendo playlist: " + nombreArchivo);
                        p = new Playlist(nombreArchivo, p.getCreadorAlias(), p.getCaratulaRuta(), p.getCanciones());
                        p.guardarPlaylist();
                    }
                }
            }
        }
    }
}

private void mostrarPlaylistSeleccionada(PlaylistVisual visual) {
    // Ocultar panel general y mostrar el específico
    PlaylistSongs.setVisible(true);
    AllSongs.setVisible(false);

    // Cargar archivo de la playlist seleccionada
    File archivo = new File("playlists/" + visual.getNombre() + ".playlist");
    if (!archivo.exists()) {
        JOptionPane.showMessageDialog(this, "No se encontró el archivo de la playlist.");
        return;
    }

    Playlist playlist = Playlist.cargarDesdeArchivo(archivo);

    // Asignar datos a los componentes del panel PlaylistSongs
    TittlePlaylist.setText(playlist.getNombre());
    CreatorPlaylist.setText(playlist.getCreadorAlias());

    // Cargar imagen de carátula si existe
    File caratula = new File(playlist.getCaratulaRuta());
    if (caratula.exists() && caratula.isFile()) {
        ImageIcon icon = new ImageIcon(caratula.getAbsolutePath());
        Image img = icon.getImage().getScaledInstance(180, 180, Image.SCALE_SMOOTH);
        PlaylistCover.setIcon(new ImageIcon(img));
    } else {
        PlaylistCover.setIcon(new ImageIcon("default_cover.png")); // Imagen por defecto
    }

    // Cargar canciones de la playlist en un modelo de lista
    DefaultListModel<String> songListModel = new DefaultListModel<>();
    SongsPlaylist.setModel(songListModel);

    // Lista real de canciones
    cancionesReal.clear(); // Limpiamos antes de llenar

    for (String path : playlist.getCanciones()) {
        File file = new File(path);
        if (file.exists() && file.isFile()) {
            Song song = new Song(file);
            cancionesReal.add(song);
            songListModel.addElement(song.getArtist() + " - " + song.getTitle()); // Texto que se mostrará
        }
    }

    // Validamos que `cancionesReal` no esté vacía antes de actualizar `SongsPlaylist`
    if (cancionesReal.isEmpty()) {
        System.out.println("⚠ La playlist está vacía, no se asignarán datos a SongsPlaylist.");
    }

    // Selección de canción en `SongsPlaylist`, como en la lista general
    SongsPlaylist.addListSelectionListener(e -> {
    if (!e.getValueIsAdjusting() && SongsPlaylist.getSelectedIndex() != -1) {
        int index = SongsPlaylist.getSelectedIndex();

        if (index >= 0 && index < cancionesReal.size()) {
            Song selectedSong = cancionesReal.get(index);

            // Actualizar interfaz
            updateSongInfo(selectedSong);

            // Eliminar si ya está en la cola
            playlistSongs.remove(selectedSong);

            // Insertar en el índice actual
            if (playlistSongs.isEmpty()) {
                playlistSongs.add(selectedSong);
                currentPlaylistIndex = 0;
            } else {
                playlistSongs.add(currentPlaylistIndex, selectedSong);
            }

            // Reproducir la canción seleccionada
            musicPlayer.setPlaylist(playlistSongs);
            musicPlayer.setCurrentTrackIndex(currentPlaylistIndex);
            // Verifica si el reproductor estaba en pausa
// Verifica si el reproductor estaba en pausa
if (musicPlayer.getState() == MusicPlayer.PlayerState.PAUSED) {
    musicPlayer.resume(); // ✅ Reanuda desde donde se pausó
} else {
    musicPlayer.play(); // 🔁 Reproduce normalmente si no estaba pausado
    Song current = musicPlayer.getCurrentSong();
    if (current != null) {
        iniciarBarraProgreso(current); // ⏳ Solo reinicia barra si es una nueva reproducción
    }
}

            // Actualizar visualización y estado
            actualizarEstadoMeGusta();
            updatePlaylistModel();

            PlayBt.setText(EmojiParser.parseToUnicode(":double_vertical_bar:"));
            PlayBt.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 30));
        }
    }
});

    // Menú contextual para agregar una canción a la cola manualmente
    JPopupMenu popupMenu = new JPopupMenu();
    JMenuItem addToQueueItem = new JMenuItem("Agregar al final");
    popupMenu.add(addToQueueItem);

    final int[] rightClickIndex = {-1};

    SongsPlaylist.addMouseListener(new MouseAdapter() {
        @Override
        public void mousePressed(MouseEvent e) {
            if (SwingUtilities.isRightMouseButton(e)) {
                int index = SongsPlaylist.locationToIndex(e.getPoint());
                if (index != -1) {
                    rightClickIndex[0] = index;
                    popupMenu.show(SongsPlaylist, e.getX(), e.getY());
                }
                e.consume();
            }
        }
    });

    // Agregar canción seleccionada a la cola
    addToQueueItem.addActionListener(e -> {
        int index = rightClickIndex[0];
        if (index >= 0 && index < cancionesReal.size()) {
            Song song = cancionesReal.get(index);
            addSongToQueueAtEnd(song); 
            System.out.println(song.getArtist() + " - " + song.getTitle() + " agregada a la cola");
        }
    });
}
public List<String> obtenerTodasLasCanciones() {
    File carpeta = new File("songs"); // Ajusta si usas otro directorio
    List<String> canciones = new ArrayList<>();
    
    if (carpeta.exists() && carpeta.isDirectory()) {
        for (File f : carpeta.listFiles()) {
            if (f.isFile() && f.getName().endsWith(".mp3")) {
                canciones.add(f.getAbsolutePath());
            }
        }
    }

    return canciones;
}
    
// Método para cargar playlists en el modelo visual
private void cargarPlaylists(DefaultListModel<PlaylistVisual> model) {
    model.clear();
    File carpeta = new File("playlists");

    if (carpeta.exists() && carpeta.isDirectory()) {
        for (File archivo : carpeta.listFiles()) {
            if (archivo.getName().endsWith(".playlist")) {
                Playlist p = Playlist.cargarDesdeArchivo(archivo);


                // Asignar imagen predeterminada si no tiene
                if (p.getCaratulaRuta() == null || p.getCaratulaRuta().isEmpty()) {
                    p.setCaratulaRuta("C:\\Users\\Luis Alfonso\\OneDrive\\Imágenes\\Caratulas Playlist LeónBeats\\Tus Canciones Favoritas.png");
                    p.guardarPlaylist();
                }

                model.addElement(new PlaylistVisual(p.getNombre(), p.getCaratulaRuta()));
            }
        }
    }
}

// Método para manejar la selección de canciones y agregarlas a la cola
private void manejarSeleccionCancion(JList<String> listaVisual, List<Song> listaReal) {
    listaVisual.addListSelectionListener(e -> {
        if (!e.getValueIsAdjusting()) {
            int index = listaVisual.getSelectedIndex();
            if (index >= 0 && index < listaReal.size()) {
                Song selectedSong = listaReal.get(index);

                // Actualizar información de la canción
                updateSongInfo(selectedSong);

                // Agregar automáticamente a la cola si no está
                if (!playlistSongs.contains(selectedSong)) {
                    addSongToQueue(selectedSong);
                }
            } else {
                System.out.println("Selección inválida en la lista.");
            }
        }
    });
}

    private void inicializarBusqueda() {
    SearchField.getDocument().addDocumentListener(new DocumentListener() {
        @Override
        public void insertUpdate(DocumentEvent e) {
            filtrarCanciones();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            filtrarCanciones();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            filtrarCanciones();
        }
    });
}
    
    private List<String> cancionesBackup = new ArrayList<>();
    
    
    private void inicializarListaCanciones() {
    DefaultListModel<String> cancionesModel = new DefaultListModel<>();
    cancionesBackup.clear(); // Limpiar antes de llenarla

    for (Song song : cancionesReal) {
        String songText = song.getArtist() + " - " + song.getTitle();
        if (!cancionesBackup.contains(songText)) { // Evitar duplicados
            cancionesBackup.add(songText);
        }
        cancionesModel.addElement(songText);
    }

    SongsList.setModel(cancionesModel);
    System.out.println("✅ Lista de canciones inicializada con " + cancionesBackup.size() + " elementos."); // Depuración
    }

private void filtrarCanciones() {
    String query = SearchField.getText().trim().toLowerCase();
    DefaultListModel<String> modeloFiltrado = new DefaultListModel<>();

    if (cancionesBackup.isEmpty()) {
        System.out.println("🔴 La lista original está vacía. No hay elementos para buscar.");
        return;
    }

    if (query.isEmpty()) {
        // Si el campo de búsqueda está vacío, volvemos a mostrar la lista original
        for (String song : cancionesBackup) {
            modeloFiltrado.addElement(song);
        }
    } else {
        // Filtrar canciones desde `cancionesBackup`
        boolean hayResultados = false;
        for (String song : cancionesBackup) {
            if (song.toLowerCase().contains(query)) {
                modeloFiltrado.addElement(song);
                hayResultados = true;
            }
        }

        if (!hayResultados) {
            System.out.println("🟡 No se encontraron resultados para: " + query);
        }
    }

    SongsList.setModel(modeloFiltrado);
    SongsList.repaint(); // Asegurar que la lista se actualice correctamente
}

private void cargarCanciones() {
    DefaultListModel<String> cancionesModel = new DefaultListModel<>();
    cancionesReal.clear(); 
    cancionesBackup.clear(); // Limpiar antes de actualizar

    for (Song song : player.getPlaylist()) { 
        cancionesReal.add(song);
        String songText = song.getArtist() + " - " + song.getTitle();
        cancionesBackup.add(songText); // Guardamos cada canción correctamente
        cancionesModel.addElement(songText);
    }

    SongsList.setModel(cancionesModel);
    System.out.println("✅ Lista de canciones cargada con " + cancionesBackup.size() + " elementos en backup."); // Depuración
}


private void toggleLikeStatus(Song song) {
    if (song == null) return; // Validamos que la canción es válida

    // Definir el nombre de la playlist de Me gusta (visible para todos los usuarios)
    String userPlaylistName = "MeGusta_" + usuarioActual.getAlias();
    File archivoPlaylist = new File("playlists/" + userPlaylistName + ".playlist");

    Playlist meGustaPlaylist = archivoPlaylist.exists() 
            ? Playlist.cargarDesdeArchivo(archivoPlaylist) 
            : new Playlist(userPlaylistName, usuarioActual.getAlias(), 
                           "C:\\Users\\Luis Alfonso\\OneDrive\\Imágenes\\Caratulas Playlist LeónBeats\\Tus Canciones Favoritas.png", 
                           new ArrayList<>());

    if (isLike) {
        // Agregar la canción si no está ya incluida
        if (!meGustaPlaylist.getCanciones().contains(song.getRutaArchivo())) {
            meGustaPlaylist.agregarCancion(song.getRutaArchivo());
            meGustaPlaylist.guardarPlaylist();
            System.out.println("✅ Canción añadida a Me Gusta.");
        }
    } else {
        // Remover la canción si ya no está marcada como Me gusta
        meGustaPlaylist.getCanciones().remove(song.getRutaArchivo());
        meGustaPlaylist.guardarPlaylist();
        System.out.println("🗑 Canción eliminada de Me Gusta.");
    }

    // Actualizar visibilidad en la lista de playlists
    actualizarListaPlaylists();
}

private void actualizarListaPlaylists() {
    modeloPlaylists.clear();
    File carpeta = new File("playlists");

    if (carpeta.exists() && carpeta.isDirectory()) {
        for (File archivo : carpeta.listFiles()) {
            if (archivo.getName().endsWith(".playlist")) {
                Playlist p = Playlist.cargarDesdeArchivo(archivo);

                // Solo agregar si la playlist tiene canciones
                
                    // Asignar imagen predeterminada si no tiene
                    if (p.getCaratulaRuta() == null || p.getCaratulaRuta().isEmpty()) {
                        p.setCaratulaRuta("C:\\Users\\Luis Alfonso\\OneDrive\\Imágenes\\Caratulas Playlist LeónBeats\\Tus Canciones Favoritas.png");
                        p.guardarPlaylist();
                    }

                    modeloPlaylists.addElement(new PlaylistVisual(p.getNombre(), p.getCaratulaRuta()));
                
            }
        }
    }
}
private void cargarPlaylistMeGusta() {
    String userPlaylistName = "MeGusta_" + usuarioActual.getAlias();
    File archivoPlaylist = new File("playlists/" + userPlaylistName + ".playlist");

    if (!archivoPlaylist.exists()) {
        System.out.println("⚠ No hay playlist de Me gusta para este usuario.");
        return;
    }

    Playlist meGustaPlaylist = Playlist.cargarDesdeArchivo(archivoPlaylist);
    actualizarListaPlaylists(); // Mostrar solo playlists con canciones
}

private void actualizarEstadoMeGusta() {
    if (playlistSongs.isEmpty() || currentPlaylistIndex < 0 || currentPlaylistIndex >= playlistSongs.size()) {
        return;
    }

    Song selectedSong = playlistSongs.get(currentPlaylistIndex);

    String userPlaylistName = "MeGusta_" + usuarioActual.getAlias();
    File archivoPlaylist = new File("playlists/" + userPlaylistName + ".playlist");
    Playlist meGustaPlaylist = archivoPlaylist.exists() ? Playlist.cargarDesdeArchivo(archivoPlaylist) : new Playlist(userPlaylistName, usuarioActual.getAlias(), "", new ArrayList<>());

    isLike = meGustaPlaylist.getCanciones().contains(selectedSong.getRutaArchivo());

    HeartState.setForeground(isLike ? Color.GREEN : Color.GRAY); // Actualizar botón
    HeartState1.setForeground(isLike ? Color.GREEN : Color.GRAY);
}

private void reiniciarListaAleatoria() {
    cancionesPendientes.clear();
    cancionesPendientes.addAll(playlistSongs); // Copiar todas las canciones de la cola
    Collections.shuffle(cancionesPendientes); // Mezclar aleatoriamente

    System.out.println("🎵 Lista aleatoria generada con " + cancionesPendientes.size() + " canciones.");
}

public void playFromMainPage(List<Song> playlistSongs, int currentPlaylistIndex) {
    if (playlistSongs == null || playlistSongs.isEmpty()) {
        System.out.println("⚠ No hay canciones en la cola de reproducción.");
        return;
    }

    musicPlayer.setPlaylist(playlistSongs); // <-- ESTE ES CLAVE
    musicPlayer.setCurrentTrackIndex(currentPlaylistIndex);
// Verifica si el reproductor estaba en pausa
if (musicPlayer.getState() == MusicPlayer.PlayerState.PAUSED) {
    musicPlayer.resume(); // ✅ Reanuda desde donde se pausó
} else {
    musicPlayer.play(); // 🔁 Reproduce normalmente si no estaba pausado
    Song current = musicPlayer.getCurrentSong();
    if (current != null) {
        iniciarBarraProgreso(current); // ⏳ Solo reinicia barra si es una nueva reproducción
    }
}
}


private void iniciarBarraProgreso(Song song, long tiempoInicial) {
    BarRepro.setMaximum((int) song.getDuration());
    TotalTime.setText(song.getFormattedDuration());

    tiempoTranscurrido = tiempoInicial;

    if (progresoTimer != null) {
        progresoTimer.stop();
    }

    BarRepro.setValue((int) tiempoTranscurrido);
    ElapsedTime.setText(formatearTiempo(tiempoTranscurrido));

    progresoTimer = new Timer(1000, e -> {
        if (musicPlayer.isPlaying()) {
            tiempoTranscurrido += 1000;
            BarRepro.setValue((int) tiempoTranscurrido);
            ElapsedTime.setText(formatearTiempo(tiempoTranscurrido));

            if (tiempoTranscurrido >= song.getDuration()) {
                progresoTimer.stop(); // Detener al finalizar
            }
        }
    });

    progresoTimer.start();
}
    
    private String formatearTiempo(long milisegundos) {
    long segundos = milisegundos / 1000;
    long minutos = segundos / 60;
    segundos = segundos % 60;
    return String.format("%02d:%02d", minutos, segundos);
}




    //Metodo Principal
    public MainPage() {
        initComponents();
        ImageIcon icon = new ImageIcon(getClass().getResource("/images/logo.png"));
        setIconImage(icon.getImage());        
        //UsuarioManager.limpiarYCorregirArchivoUsuarios();
        
        //Ocultar paneles que no se usan al iniciar el programa
        LoginPanel.setVisible(true);
        HomePanel.setVisible(false);
        RegisterPanel.setVisible(false);
        ColaRepro.setVisible(false);
        EditUser.setVisible(false);
        CreateNewPlaylist.setVisible(false);
        initListaPlaylists();
        cargarCanciones();
        
        

        //Metodos para usar la libreria Java-Emoji en los JLabel
        String PlayName = ":arrow_forward:";
        String PlayEmoji = EmojiParser.parseToUnicode(PlayName);
        PlayBt.setText(PlayEmoji);
        PlayBt.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 30));
        
        String NextName = ":black_right_pointing_double_triangle_with_vertical_bar:";
        String NextEmoji = EmojiParser.parseToUnicode(NextName);
        NextBt.setText(NextEmoji);
        NextBt.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 30));
        NextBt.setForeground(Color.GRAY);
        
        String BackName = ":black_left_pointing_double_triangle_with_vertical_bar:";
        String BackEmoji = EmojiParser.parseToUnicode(BackName);
        BackBt.setText(BackEmoji);
        BackBt.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 30));
        BackBt.setForeground(Color.GRAY);
        
        String RandomName = ":twisted_rightwards_arrows:";
        String RandomEmoji = EmojiParser.parseToUnicode(RandomName);
        RandomBt.setText(RandomEmoji);
        RandomBt.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 30));
        RandomBt.setForeground(Color.WHITE);
        
        String RepeatName = ":repeat:";
        String RepeatEmoji = EmojiParser.parseToUnicode(RepeatName);
        RepeatBt.setText(RepeatEmoji);
        RepeatBt.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 30));
        RepeatBt.setForeground(Color.WHITE);
        
        String SoundName = ":sound:";
        String SoundEmoji = EmojiParser.parseToUnicode(SoundName);
        SoundBt.setText(SoundEmoji);
        SoundBt.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20));
        SoundBt.setForeground(Color.WHITE);
        
        String PlaylistManagerName = ":heavy_plus_sign:";
        String PlaylistManagerEmoji = EmojiParser.parseToUnicode(PlaylistManagerName);
        NewPlaylistManager.setText(PlaylistManagerEmoji);
        NewPlaylistManager.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20));
        NewPlaylistManager.setForeground(Color.GRAY);
        
        String FriendsName = ":busts_in_silhouette:";
        String FriendsEmoji = EmojiParser.parseToUnicode(FriendsName);
        FriendsImg.setText(FriendsEmoji);
        FriendsImg.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20));
        FriendsImg.setForeground(Color.GRAY);
        
        String HomeName = ":house:";
        String HomeEmoji = EmojiParser.parseToUnicode(HomeName);
        HomeIcon.setText(HomeEmoji);
        HomeIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20));
        HomeIcon.setForeground(Color.GRAY);
        
        String SearchName = ":mag:";
        String SearchEmoji = EmojiParser.parseToUnicode(SearchName);
        SearchIcon.setText(SearchEmoji);
        SearchIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 25));
        SearchIcon.setForeground(Color.GRAY);
        
        String OColaName = ":arrow_up:";
        String OCEmoji = EmojiParser.parseToUnicode(OColaName);
        OpenColaR.setText(OCEmoji);
        OpenColaR.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20));
        OpenColaR.setForeground(Color.WHITE);
        
        String CColaName = ":arrow_down:";
        String CCEmoji = EmojiParser.parseToUnicode(CColaName);
        CloseColaR.setText(CCEmoji);
        CloseColaR.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20));
        CloseColaR.setForeground(Color.WHITE);
        
        String CEditUName = ":arrow_up:";
        String CEditUEmoji = EmojiParser.parseToUnicode(CEditUName);
        CloseEditU.setText(CEditUEmoji);
        CloseEditU.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20));
        CloseEditU.setForeground(Color.WHITE);
        
        String CCPlaylistName = ":arrow_up:";
        String CCPlaylistEmoji = EmojiParser.parseToUnicode(CCPlaylistName);
        CloseCreatorPlaylist.setText(CCPlaylistEmoji);
        CloseCreatorPlaylist.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20));
        CloseCreatorPlaylist.setForeground(Color.WHITE);
        
        String HeartName = ":heart:";
        String HeartEmoji = EmojiParser.parseToUnicode(HeartName);
        HeartState.setText(HeartEmoji);
        HeartState.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 25));
        HeartState.setForeground(Color.GRAY);
        
        String Heart1Name = ":heart:";
        String Heart1Emoji = EmojiParser.parseToUnicode(Heart1Name);
        HeartState1.setText(Heart1Emoji);
        HeartState1.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 25));
        HeartState1.setForeground(Color.GRAY);
        
        String AddToQueeName = ":arrow_forward:";
        String AddToQueeEmoji = EmojiParser.parseToUnicode(AddToQueeName);
        AddtoQueePlaylist.setText(AddToQueeEmoji);
        AddtoQueePlaylist.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 30));
        AddtoQueePlaylist.setForeground(Color.GREEN);
        
        
        //Metodo para los emojis del volumen dependiendo la barra del sonido
        VolumeSlider.setMinimum(0);
        VolumeSlider.setMaximum(100);
        VolumeSlider.setValue(50);
        
        VolumeSlider.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent e) {
        int value = VolumeSlider.getValue();
        String emoji;

        if (value == 0) {
            emoji = EmojiParser.parseToUnicode(":mute:"); // 🔇
            isMuted = true;
        } else if (value <= 33) {
            emoji = EmojiParser.parseToUnicode(":speaker:"); // 🔈
        } else if (value <= 66) {
            emoji = EmojiParser.parseToUnicode(":sound:"); // 🔉
        } else {
            emoji = EmojiParser.parseToUnicode(":loud_sound:"); // 🔊
        }

        SoundBt.setText(emoji);
        SoundBt.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20));
    }
});
        
        //Metodo para mostrar y ver la lista General de canciones
        DefaultListModel<String> songListModel = new DefaultListModel<>();
        SongsList.setModel(songListModel); // SongsList es JList<String>

        // Creamos y cargamos el reproductor
        MusicPlayer player = new MusicPlayer();
        List<Song> canciones = player.getPlaylist(); // Guardamos la lista real

        // Llenamos el modelo de la lista con los nombres
        for (Song song : canciones) {
            songListModel.addElement(song.getArtist() + " - " + song.getTitle());
        }   
SongsList.addListSelectionListener(e -> {
    if (!e.getValueIsAdjusting() && SongsList.getSelectedIndex() != -1) {
        int index = SongsList.getSelectedIndex();
        if (index >= 0 && index < canciones.size()) {
            Song selectedSong = canciones.get(index);

            // Actualizar interfaz
            updateSongInfo(selectedSong);

            // Eliminar si ya está en la cola
            playlistSongs.remove(selectedSong);

            // Insertar en el índice actual
            if (playlistSongs.isEmpty()) {
                playlistSongs.add(selectedSong);
                currentPlaylistIndex = 0;
            } else {
                playlistSongs.add(currentPlaylistIndex, selectedSong);
            }

            // Reproducir la canción seleccionada
            musicPlayer.setPlaylist(playlistSongs);
            musicPlayer.setCurrentTrackIndex(currentPlaylistIndex);
// Verifica si el reproductor estaba en pausa
if (musicPlayer.getState() == MusicPlayer.PlayerState.PAUSED) {
    musicPlayer.resume(); // ✅ Reanuda desde donde se pausó
} else {
    musicPlayer.play(); // 🔁 Reproduce normalmente si no estaba pausado
    Song current = musicPlayer.getCurrentSong();
    if (current != null) {
        iniciarBarraProgreso(current); // ⏳ Solo reinicia barra si es una nueva reproducción
    }
}

            // Actualizar visualización y estado
            actualizarEstadoMeGusta();
            updatePlaylistModel();

            PlayBt.setText(EmojiParser.parseToUnicode(":double_vertical_bar:"));
            PlayBt.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 30));
        }
    }
});
        
        // Modelo de la lista de reproducción
        playlistModel = new DefaultListModel<>();
        ColaReproduccion.setModel(playlistModel);

        // Popup menu para agregar a la cola de reproducción
        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem addToPlaylistItem = new JMenuItem("Agregar al final");
        popupMenu.add(addToPlaylistItem);

        // Variable para guardar el índice del clic derecho
        final int[] rightClickIndex = {-1};

        SongsList.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger() || SwingUtilities.isRightMouseButton(e)) {
                    int index = SongsList.locationToIndex(e.getPoint());
                    if (index != -1) {
                        rightClickIndex[0] = index;
                        popupMenu.show(SongsList, e.getX(), e.getY());
                    }
                    e.consume();
                }
            }
            
    @Override
    public void mouseReleased(MouseEvent e) {
        if (e.isPopupTrigger() || SwingUtilities.isRightMouseButton(e)) {
            int index = SongsList.locationToIndex(e.getPoint());
            if (index != -1) {
                rightClickIndex[0] = index;
                popupMenu.show(SongsList, e.getX(), e.getY());
            }
            e.consume();
        }
    }
    });

        addToPlaylistItem.addActionListener(e -> {
    int index = rightClickIndex[0];
    if (index >= 0 && index < canciones.size()) {
        Song song = canciones.get(index);
        addSongToQueueAtEnd(song); 
        System.out.println(song.getArtist() + " - " + song.getTitle() + " agregada a la cola");
    }
});
        
        //Metodo para poder cambiar la foto de perfil
    AddPhoto.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setDialogTitle("Selecciona una foto de perfil");
                fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                
                int resultado = fileChooser.showOpenDialog(null);
                if (resultado == JFileChooser.APPROVE_OPTION) {
                    File archivo = fileChooser.getSelectedFile();
                    String ruta = archivo.getAbsolutePath();
                    
                    // Mostrar imagen en el JLabel (opcional: redimensionar)
                    ImageIcon icono = new ImageIcon(ruta);
                    Image imagen = icono.getImage().getScaledInstance(
                            lblUserImg.getWidth(), lblUserImg.getHeight(), Image.SCALE_SMOOTH
                    );
                    lblUserImg.setIcon(new ImageIcon(imagen));
                    
                    // Guardar la ruta en el usuario actual (debes tener acceso al objeto Usuario actual)
                    usuarioActual.setFotoRuta(ruta); // ← Asegúrate de tenerlo definido
                    UsuarioManager.actualizarUsuario(usuarioActual);
                }       }
        });
    
    
    SearchField.addKeyListener(new KeyAdapter() {
    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            filtrarCanciones(); // Llamamos al método de búsqueda cuando el usuario presiona Enter
        }
    }
    });
    
  
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        LoginPanel = new javax.swing.JPanel();
        fondoLogin = new javax.swing.JPanel();
        LabelLogo = new javax.swing.JLabel();
        LabelTitle = new javax.swing.JLabel();
        LabelCode = new javax.swing.JLabel();
        LabelPassword = new javax.swing.JLabel();
        RegisterBt = new javax.swing.JButton();
        EnterSButon = new javax.swing.JButton();
        ErrorLabel = new javax.swing.JLabel();
        roundedPanel1 = new Buttons.RoundedPanel();
        CodeField = new javax.swing.JTextField();
        roundedPanel2 = new Buttons.RoundedPanel();
        PasswordField = new javax.swing.JPasswordField();
        GoogleBt1 = new Buttons.RoundedPanel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        TaskBar = new javax.swing.JPanel();
        Xpanel = new javax.swing.JPanel();
        Xlabel = new javax.swing.JLabel();
        MinPanel = new javax.swing.JPanel();
        MinLabel = new javax.swing.JLabel();
        HomePanel = new javax.swing.JPanel();
        TaskBar1 = new javax.swing.JPanel();
        Xpanel1 = new javax.swing.JPanel();
        Xlabel1 = new javax.swing.JLabel();
        MinPanel1 = new javax.swing.JPanel();
        MinLabel1 = new javax.swing.JLabel();
        ImgTaskBar = new javax.swing.JLabel();
        UserImg = new javax.swing.JLabel();
        FriendsImg = new javax.swing.JLabel();
        SearchPanel = new Buttons.RoundedPanel();
        SearchIcon = new javax.swing.JLabel();
        SearchField = new javax.swing.JTextField();
        HomeIconPanel = new Buttons.RoundedPanel();
        HomeIcon = new javax.swing.JLabel();
        ReproBar = new javax.swing.JPanel();
        PlayBt = new javax.swing.JLabel();
        NextBt = new javax.swing.JLabel();
        BackBt = new javax.swing.JLabel();
        RandomBt = new javax.swing.JLabel();
        SongImg = new javax.swing.JLabel();
        NameSong = new javax.swing.JLabel();
        ArtistSong = new javax.swing.JLabel();
        RepeatBt = new javax.swing.JLabel();
        VolumeSlider = new javax.swing.JSlider();
        SoundBt = new javax.swing.JLabel();
        BarRepro = new javax.swing.JSlider();
        OpenColaR = new javax.swing.JLabel();
        NewPlaylistManager = new javax.swing.JLabel();
        HeartState = new javax.swing.JLabel();
        ElapsedTime = new javax.swing.JLabel();
        TotalTime = new javax.swing.JLabel();
        PlaylistsPanel = new javax.swing.JPanel();
        ViewPlaylists = new javax.swing.JPanel();
        ScrollPlaylist = new javax.swing.JScrollPane();
        CreateNewPlaylist = new javax.swing.JPanel();
        PlaylistImg = new javax.swing.JLabel();
        NamePlaylist = new javax.swing.JTextField();
        UserCreator = new javax.swing.JLabel();
        AddImgPlaylist = new javax.swing.JButton();
        SavePlaylist = new javax.swing.JButton();
        CloseCreatorPlaylist = new javax.swing.JLabel();
        SongsPanel = new javax.swing.JPanel();
        PlaylistSongs = new javax.swing.JPanel();
        PlaylistCover = new javax.swing.JLabel();
        TittlePlaylist = new javax.swing.JLabel();
        CreatorPlaylist = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        SongsPlaylist = new javax.swing.JList<>();
        roundedPanel3 = new Buttons.RoundedPanel();
        AddCover = new javax.swing.JButton();
        roundedPanel4 = new Buttons.RoundedPanel();
        AddSongs = new javax.swing.JButton();
        AddtoQueePlaylist = new javax.swing.JLabel();
        AllSongs = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        SongsList = new javax.swing.JList<>();
        ReproductorPanel = new javax.swing.JPanel();
        SongInfo = new javax.swing.JPanel();
        SongImg2 = new javax.swing.JLabel();
        ArtistSong2 = new javax.swing.JLabel();
        AlbumSong = new javax.swing.JLabel();
        DateSong = new javax.swing.JLabel();
        NameSong2 = new javax.swing.JLabel();
        HeartState1 = new javax.swing.JLabel();
        ColaRepro = new javax.swing.JPanel();
        CloseColaR = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        ColaReproduccion = new javax.swing.JList<>();
        EditUser = new javax.swing.JPanel();
        lblUserImg = new javax.swing.JLabel();
        lblUserCode = new javax.swing.JLabel();
        txtAliasPerfil = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        txtNewPassword = new javax.swing.JPasswordField();
        jLabel5 = new javax.swing.JLabel();
        txtPasswordConfirmar = new javax.swing.JPasswordField();
        btnGuardarPerfil = new javax.swing.JButton();
        lblPasswordError = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        CloseEditU = new javax.swing.JLabel();
        AddPhoto = new javax.swing.JButton();
        RegisterPanel = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        CodeL = new javax.swing.JLabel();
        NewCodeField = new javax.swing.JTextField();
        ReadCL = new javax.swing.JLabel();
        NewPasswordField = new javax.swing.JPasswordField();
        RepCLabel = new javax.swing.JLabel();
        RepeatPasswordField = new javax.swing.JPasswordField();
        RegisterFBt = new javax.swing.JButton();
        RegisterErrorLabel = new javax.swing.JLabel();
        RegisterIconImg = new javax.swing.JLabel();
        registrar = new javax.swing.JLabel();
        Usuarios = new javax.swing.JLabel();
        GoogleBt = new Buttons.RoundedPanel();
        jLabel2 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        TaskBar2 = new javax.swing.JPanel();
        XBtL = new javax.swing.JPanel();
        Xlabel2 = new javax.swing.JLabel();
        MinBtL = new javax.swing.JPanel();
        MinLabel2 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setLocationByPlatform(true);
        setUndecorated(true);
        setResizable(false);

        LoginPanel.setBackground(new java.awt.Color(0, 0, 0));
        LoginPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        fondoLogin.setBackground(new java.awt.Color(18, 18, 18));
        fondoLogin.setAutoscrolls(true);
        fondoLogin.setPreferredSize(new java.awt.Dimension(600, 600));

        LabelLogo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/LC 200.png"))); // NOI18N
        LabelLogo.setText("jLabel1");

        LabelTitle.setFont(new java.awt.Font("SansSerif", 3, 60)); // NOI18N
        LabelTitle.setForeground(new java.awt.Color(255, 209, 0));
        LabelTitle.setText("LeónBeats");

        LabelCode.setFont(new java.awt.Font("Candara", 0, 14)); // NOI18N
        LabelCode.setForeground(new java.awt.Color(255, 255, 255));
        LabelCode.setText("Codigo de estudiante");

        LabelPassword.setFont(new java.awt.Font("Candara", 0, 14)); // NOI18N
        LabelPassword.setForeground(new java.awt.Color(255, 255, 255));
        LabelPassword.setText("Contraseña");

        RegisterBt.setBackground(new java.awt.Color(18, 18, 18));
        RegisterBt.setFont(new java.awt.Font("SansSerif", 3, 14)); // NOI18N
        RegisterBt.setForeground(new java.awt.Color(255, 255, 255));
        RegisterBt.setText("Registrarme");
        RegisterBt.setBorder(javax.swing.BorderFactory.createEtchedBorder(java.awt.Color.lightGray, java.awt.Color.lightGray));
        RegisterBt.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        RegisterBt.setFocusPainted(false);
        RegisterBt.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                RegisterBtActionPerformed(evt);
            }
        });

        EnterSButon.setBackground(new java.awt.Color(18, 18, 18));
        EnterSButon.setFont(new java.awt.Font("SansSerif", 3, 14)); // NOI18N
        EnterSButon.setForeground(new java.awt.Color(255, 255, 255));
        EnterSButon.setText("Iniciar Sesión");
        EnterSButon.setBorder(javax.swing.BorderFactory.createEtchedBorder(java.awt.Color.lightGray, java.awt.Color.lightGray));
        EnterSButon.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        EnterSButon.setFocusPainted(false);
        EnterSButon.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                EnterSButonActionPerformed(evt);
            }
        });

        ErrorLabel.setForeground(new java.awt.Color(255, 0, 0));
        ErrorLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);

        roundedPanel1.setBorderColor(java.awt.Color.lightGray);
        roundedPanel1.setPanelColor(new java.awt.Color(18, 18, 18));

        CodeField.setBackground(new java.awt.Color(18, 18, 18));
        CodeField.setFont(new java.awt.Font("SansSerif", 0, 14)); // NOI18N
        CodeField.setForeground(new java.awt.Color(255, 255, 255));
        CodeField.setBorder(null);
        CodeField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CodeFieldActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout roundedPanel1Layout = new javax.swing.GroupLayout(roundedPanel1);
        roundedPanel1.setLayout(roundedPanel1Layout);
        roundedPanel1Layout.setHorizontalGroup(
            roundedPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, roundedPanel1Layout.createSequentialGroup()
                .addContainerGap(15, Short.MAX_VALUE)
                .addComponent(CodeField, javax.swing.GroupLayout.PREFERRED_SIZE, 464, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(17, 17, 17))
        );
        roundedPanel1Layout.setVerticalGroup(
            roundedPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(roundedPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(CodeField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        roundedPanel2.setBorderColor(java.awt.Color.lightGray);
        roundedPanel2.setPanelColor(new java.awt.Color(18, 18, 18));

        PasswordField.setBackground(new java.awt.Color(18, 18, 18));
        PasswordField.setFont(new java.awt.Font("SansSerif", 0, 14)); // NOI18N
        PasswordField.setForeground(new java.awt.Color(255, 255, 255));
        PasswordField.setBorder(null);

        javax.swing.GroupLayout roundedPanel2Layout = new javax.swing.GroupLayout(roundedPanel2);
        roundedPanel2.setLayout(roundedPanel2Layout);
        roundedPanel2Layout.setHorizontalGroup(
            roundedPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(roundedPanel2Layout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addComponent(PasswordField, javax.swing.GroupLayout.PREFERRED_SIZE, 468, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(18, Short.MAX_VALUE))
        );
        roundedPanel2Layout.setVerticalGroup(
            roundedPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, roundedPanel2Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(PasswordField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        GoogleBt1.setBorderColor(java.awt.Color.lightGray);
        GoogleBt1.setPanelColor(new java.awt.Color(18, 18, 18));
        GoogleBt1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                GoogleBt1MouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                GoogleBt1MouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                GoogleBt1MouseExited(evt);
            }
        });
        GoogleBt1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel7.setFont(new java.awt.Font("SansSerif", 1, 18)); // NOI18N
        jLabel7.setForeground(new java.awt.Color(255, 255, 255));
        jLabel7.setText("Continuar con Google");
        GoogleBt1.add(jLabel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(160, 0, 202, 50));

        jLabel8.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel8.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/G 40x40.png"))); // NOI18N
        GoogleBt1.add(jLabel8, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 0, -1, 50));

        javax.swing.GroupLayout fondoLoginLayout = new javax.swing.GroupLayout(fondoLogin);
        fondoLogin.setLayout(fondoLoginLayout);
        fondoLoginLayout.setHorizontalGroup(
            fondoLoginLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, fondoLoginLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(ErrorLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 312, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(140, 140, 140))
            .addGroup(fondoLoginLayout.createSequentialGroup()
                .addGap(50, 50, 50)
                .addGroup(fondoLoginLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(roundedPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(roundedPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(LabelPassword, javax.swing.GroupLayout.PREFERRED_SIZE, 500, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(fondoLoginLayout.createSequentialGroup()
                        .addComponent(LabelLogo, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(LabelTitle))
                    .addComponent(LabelCode, javax.swing.GroupLayout.PREFERRED_SIZE, 500, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(fondoLoginLayout.createSequentialGroup()
                        .addGap(2, 2, 2)
                        .addGroup(fondoLoginLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(GoogleBt1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(fondoLoginLayout.createSequentialGroup()
                                .addComponent(RegisterBt, javax.swing.GroupLayout.PREFERRED_SIZE, 240, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(EnterSButon, javax.swing.GroupLayout.PREFERRED_SIZE, 240, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                .addContainerGap(38, Short.MAX_VALUE))
        );
        fondoLoginLayout.setVerticalGroup(
            fondoLoginLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(fondoLoginLayout.createSequentialGroup()
                .addGap(6, 6, 6)
                .addGroup(fondoLoginLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(LabelLogo)
                    .addGroup(fondoLoginLayout.createSequentialGroup()
                        .addGap(35, 35, 35)
                        .addComponent(LabelTitle, javax.swing.GroupLayout.PREFERRED_SIZE, 131, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(6, 6, 6)
                .addComponent(LabelCode)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(roundedPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(LabelPassword)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(roundedPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(fondoLoginLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(RegisterBt, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(EnterSButon, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(GoogleBt1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(ErrorLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 93, Short.MAX_VALUE)
                .addGap(23, 23, 23))
        );

        LoginPanel.add(fondoLogin, new org.netbeans.lib.awtextra.AbsoluteConstraints(250, 90, -1, 530));

        TaskBar.setBackground(new java.awt.Color(18, 18, 18));
        TaskBar.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseDragged(java.awt.event.MouseEvent evt) {
                TaskBarMouseDragged(evt);
            }
        });
        TaskBar.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                TaskBarMousePressed(evt);
            }
        });
        TaskBar.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        Xpanel.setBackground(new java.awt.Color(18, 18, 18));

        Xlabel.setFont(new java.awt.Font("SansSerif", 3, 36)); // NOI18N
        Xlabel.setForeground(new java.awt.Color(255, 255, 255));
        Xlabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        Xlabel.setText("x");
        Xlabel.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        Xlabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                XlabelMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                XlabelMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                XlabelMouseExited(evt);
            }
        });

        javax.swing.GroupLayout XpanelLayout = new javax.swing.GroupLayout(Xpanel);
        Xpanel.setLayout(XpanelLayout);
        XpanelLayout.setHorizontalGroup(
            XpanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, XpanelLayout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(Xlabel, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        XpanelLayout.setVerticalGroup(
            XpanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, XpanelLayout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(Xlabel, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        TaskBar.add(Xpanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(1030, 0, -1, -1));

        MinPanel.setBackground(new java.awt.Color(18, 18, 18));

        MinLabel.setFont(new java.awt.Font("SansSerif", 3, 36)); // NOI18N
        MinLabel.setForeground(new java.awt.Color(255, 255, 255));
        MinLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        MinLabel.setText("-");
        MinLabel.setToolTipText("");
        MinLabel.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        MinLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                MinLabelMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                MinLabelMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                MinLabelMouseExited(evt);
            }
        });

        javax.swing.GroupLayout MinPanelLayout = new javax.swing.GroupLayout(MinPanel);
        MinPanel.setLayout(MinPanelLayout);
        MinPanelLayout.setHorizontalGroup(
            MinPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, MinPanelLayout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(MinLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        MinPanelLayout.setVerticalGroup(
            MinPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, MinPanelLayout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(MinLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        TaskBar.add(MinPanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(980, 0, 50, -1));

        LoginPanel.add(TaskBar, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 1080, 40));

        HomePanel.setBackground(new java.awt.Color(0, 0, 0));
        HomePanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        TaskBar1.setBackground(new java.awt.Color(0, 0, 0));
        TaskBar1.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseDragged(java.awt.event.MouseEvent evt) {
                TaskBar1MouseDragged(evt);
            }
        });
        TaskBar1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                TaskBar1MousePressed(evt);
            }
        });
        TaskBar1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        Xpanel1.setBackground(new java.awt.Color(0, 0, 0));

        Xlabel1.setFont(new java.awt.Font("SansSerif", 3, 36)); // NOI18N
        Xlabel1.setForeground(new java.awt.Color(255, 255, 255));
        Xlabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        Xlabel1.setText("x");
        Xlabel1.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        Xlabel1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                Xlabel1MouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                Xlabel1MouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                Xlabel1MouseExited(evt);
            }
        });

        javax.swing.GroupLayout Xpanel1Layout = new javax.swing.GroupLayout(Xpanel1);
        Xpanel1.setLayout(Xpanel1Layout);
        Xpanel1Layout.setHorizontalGroup(
            Xpanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, Xpanel1Layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(Xlabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        Xpanel1Layout.setVerticalGroup(
            Xpanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, Xpanel1Layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(Xlabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        TaskBar1.add(Xpanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(1030, 0, -1, -1));

        MinPanel1.setBackground(new java.awt.Color(0, 0, 0));

        MinLabel1.setFont(new java.awt.Font("SansSerif", 3, 36)); // NOI18N
        MinLabel1.setForeground(new java.awt.Color(255, 255, 255));
        MinLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        MinLabel1.setText("-");
        MinLabel1.setToolTipText("");
        MinLabel1.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        MinLabel1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                MinLabel1MouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                MinLabel1MouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                MinLabel1MouseExited(evt);
            }
        });

        javax.swing.GroupLayout MinPanel1Layout = new javax.swing.GroupLayout(MinPanel1);
        MinPanel1.setLayout(MinPanel1Layout);
        MinPanel1Layout.setHorizontalGroup(
            MinPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, MinPanel1Layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(MinLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        MinPanel1Layout.setVerticalGroup(
            MinPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, MinPanel1Layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(MinLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        TaskBar1.add(MinPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(980, 0, 50, -1));

        ImgTaskBar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/40x40.png"))); // NOI18N
        TaskBar1.add(ImgTaskBar, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 0, 50, 60));

        UserImg.setText("jLabel2");
        UserImg.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                UserImgMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                UserImgMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                UserImgMouseExited(evt);
            }
        });
        TaskBar1.add(UserImg, new org.netbeans.lib.awtextra.AbsoluteConstraints(920, 10, 40, 40));

        FriendsImg.setText("jLabel2");
        FriendsImg.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                FriendsImgMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                FriendsImgMouseExited(evt);
            }
        });
        TaskBar1.add(FriendsImg, new org.netbeans.lib.awtextra.AbsoluteConstraints(860, 10, 40, 40));

        SearchPanel.setPanelColor(new java.awt.Color(18, 18, 18));
        SearchPanel.setRadius(30);

        SearchIcon.setForeground(new java.awt.Color(18, 18, 18));
        SearchIcon.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        SearchIcon.setText("jLabel1");
        SearchIcon.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        SearchIcon.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                SearchIconMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                SearchIconMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                SearchIconMouseExited(evt);
            }
        });

        SearchField.setBackground(new java.awt.Color(18, 18, 18));
        SearchField.setFont(new java.awt.Font("SansSerif", 1, 12)); // NOI18N
        SearchField.setForeground(new java.awt.Color(255, 255, 255));
        SearchField.setBorder(null);

        javax.swing.GroupLayout SearchPanelLayout = new javax.swing.GroupLayout(SearchPanel);
        SearchPanel.setLayout(SearchPanelLayout);
        SearchPanelLayout.setHorizontalGroup(
            SearchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(SearchPanelLayout.createSequentialGroup()
                .addComponent(SearchIcon)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(SearchField, javax.swing.GroupLayout.PREFERRED_SIZE, 393, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 18, Short.MAX_VALUE))
        );
        SearchPanelLayout.setVerticalGroup(
            SearchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(SearchPanelLayout.createSequentialGroup()
                .addComponent(SearchIcon, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, SearchPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(SearchField)
                .addContainerGap())
        );

        TaskBar1.add(SearchPanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(330, 10, 460, 40));

        HomeIconPanel.setPanelColor(new java.awt.Color(18, 18, 18));

        HomeIcon.setForeground(new java.awt.Color(18, 18, 18));
        HomeIcon.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        HomeIcon.setText("a");
        HomeIcon.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        HomeIcon.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                HomeIconMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                HomeIconMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                HomeIconMouseExited(evt);
            }
        });

        javax.swing.GroupLayout HomeIconPanelLayout = new javax.swing.GroupLayout(HomeIconPanel);
        HomeIconPanel.setLayout(HomeIconPanelLayout);
        HomeIconPanelLayout.setHorizontalGroup(
            HomeIconPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, HomeIconPanelLayout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(HomeIcon, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        HomeIconPanelLayout.setVerticalGroup(
            HomeIconPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(HomeIcon, javax.swing.GroupLayout.DEFAULT_SIZE, 40, Short.MAX_VALUE)
        );

        TaskBar1.add(HomeIconPanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(290, 10, 40, 40));

        HomePanel.add(TaskBar1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 1080, 60));

        ReproBar.setBackground(new java.awt.Color(0, 0, 0));
        ReproBar.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        PlayBt.setBackground(new java.awt.Color(255, 255, 255));
        PlayBt.setFont(new java.awt.Font("Segoe UI Emoji", 0, 14)); // NOI18N
        PlayBt.setForeground(new java.awt.Color(255, 255, 255));
        PlayBt.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        PlayBt.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);
        PlayBt.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        PlayBt.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                PlayBtMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                PlayBtMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                PlayBtMouseExited(evt);
            }
        });
        ReproBar.add(PlayBt, new org.netbeans.lib.awtextra.AbsoluteConstraints(520, 10, 50, 40));

        NextBt.setBackground(new java.awt.Color(153, 153, 153));
        NextBt.setFont(new java.awt.Font("Segoe UI Emoji", 0, 14)); // NOI18N
        NextBt.setForeground(new java.awt.Color(255, 255, 255));
        NextBt.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        NextBt.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);
        NextBt.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        NextBt.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                NextBtMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                NextBtMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                NextBtMouseExited(evt);
            }
        });
        ReproBar.add(NextBt, new org.netbeans.lib.awtextra.AbsoluteConstraints(590, 10, 50, 40));

        BackBt.setBackground(new java.awt.Color(153, 153, 153));
        BackBt.setFont(new java.awt.Font("Segoe UI Emoji", 0, 14)); // NOI18N
        BackBt.setForeground(new java.awt.Color(255, 255, 255));
        BackBt.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        BackBt.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);
        BackBt.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        BackBt.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                BackBtMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                BackBtMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                BackBtMouseExited(evt);
            }
        });
        ReproBar.add(BackBt, new org.netbeans.lib.awtextra.AbsoluteConstraints(450, 10, 50, 40));

        RandomBt.setBackground(new java.awt.Color(153, 153, 153));
        RandomBt.setFont(new java.awt.Font("Segoe UI Emoji", 0, 14)); // NOI18N
        RandomBt.setForeground(new java.awt.Color(255, 255, 255));
        RandomBt.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        RandomBt.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);
        RandomBt.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        RandomBt.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                RandomBtMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                RandomBtMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                RandomBtMouseExited(evt);
            }
        });
        ReproBar.add(RandomBt, new org.netbeans.lib.awtextra.AbsoluteConstraints(350, 10, 50, 40));
        ReproBar.add(SongImg, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 10, 60, 60));

        NameSong.setFont(new java.awt.Font("SansSerif", 3, 14)); // NOI18N
        NameSong.setForeground(new java.awt.Color(255, 255, 255));
        ReproBar.add(NameSong, new org.netbeans.lib.awtextra.AbsoluteConstraints(90, 20, 150, 20));

        ArtistSong.setFont(new java.awt.Font("SansSerif", 0, 14)); // NOI18N
        ArtistSong.setForeground(new java.awt.Color(255, 255, 255));
        ReproBar.add(ArtistSong, new org.netbeans.lib.awtextra.AbsoluteConstraints(90, 40, 150, 20));

        RepeatBt.setBackground(new java.awt.Color(153, 153, 153));
        RepeatBt.setFont(new java.awt.Font("Segoe UI Emoji", 0, 14)); // NOI18N
        RepeatBt.setForeground(new java.awt.Color(255, 255, 255));
        RepeatBt.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        RepeatBt.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);
        RepeatBt.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        RepeatBt.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                RepeatBtMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                RepeatBtMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                RepeatBtMouseExited(evt);
            }
        });
        ReproBar.add(RepeatBt, new org.netbeans.lib.awtextra.AbsoluteConstraints(690, 10, 50, 40));

        VolumeSlider.setBackground(new java.awt.Color(0, 0, 0));
        VolumeSlider.setValue(100);
        ReproBar.add(VolumeSlider, new org.netbeans.lib.awtextra.AbsoluteConstraints(930, 50, 140, -1));

        SoundBt.setBackground(new java.awt.Color(153, 153, 153));
        SoundBt.setFont(new java.awt.Font("Segoe UI Emoji", 0, 14)); // NOI18N
        SoundBt.setForeground(new java.awt.Color(255, 255, 255));
        SoundBt.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        SoundBt.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        SoundBt.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        SoundBt.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                SoundBtMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                SoundBtMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                SoundBtMouseExited(evt);
            }
        });
        ReproBar.add(SoundBt, new org.netbeans.lib.awtextra.AbsoluteConstraints(890, 50, 30, 30));

        BarRepro.setBackground(new java.awt.Color(0, 0, 0));
        BarRepro.setValue(0);
        ReproBar.add(BarRepro, new org.netbeans.lib.awtextra.AbsoluteConstraints(300, 50, 470, -1));

        OpenColaR.setText("jLabel3");
        OpenColaR.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                OpenColaRMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                OpenColaRMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                OpenColaRMouseExited(evt);
            }
        });
        ReproBar.add(OpenColaR, new org.netbeans.lib.awtextra.AbsoluteConstraints(1040, 0, -1, 30));

        NewPlaylistManager.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                NewPlaylistManagerMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                NewPlaylistManagerMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                NewPlaylistManagerMouseExited(evt);
            }
        });
        ReproBar.add(NewPlaylistManager, new org.netbeans.lib.awtextra.AbsoluteConstraints(810, 0, 30, 30));

        HeartState.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        HeartState.setText("jLabel9");
        HeartState.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        HeartState.setPreferredSize(new java.awt.Dimension(40, 40));
        HeartState.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                HeartStateMouseClicked(evt);
            }
        });
        ReproBar.add(HeartState, new org.netbeans.lib.awtextra.AbsoluteConstraints(300, 10, -1, -1));
        ReproBar.add(ElapsedTime, new org.netbeans.lib.awtextra.AbsoluteConstraints(250, 50, 50, 20));
        ReproBar.add(TotalTime, new org.netbeans.lib.awtextra.AbsoluteConstraints(780, 50, 60, 20));

        HomePanel.add(ReproBar, new org.netbeans.lib.awtextra.AbsoluteConstraints(-10, 630, 1080, 90));

        PlaylistsPanel.setBackground(new java.awt.Color(18, 18, 18));

        ViewPlaylists.setBackground(new java.awt.Color(18, 18, 18));
        ViewPlaylists.setPreferredSize(new java.awt.Dimension(250, 560));

        ScrollPlaylist.setBackground(new java.awt.Color(204, 204, 0));
        ScrollPlaylist.setBorder(null);
        ScrollPlaylist.setForeground(new java.awt.Color(255, 102, 102));
        ScrollPlaylist.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        ScrollPlaylist.setHorizontalScrollBar(null);

        javax.swing.GroupLayout ViewPlaylistsLayout = new javax.swing.GroupLayout(ViewPlaylists);
        ViewPlaylists.setLayout(ViewPlaylistsLayout);
        ViewPlaylistsLayout.setHorizontalGroup(
            ViewPlaylistsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(ScrollPlaylist, javax.swing.GroupLayout.DEFAULT_SIZE, 250, Short.MAX_VALUE)
        );
        ViewPlaylistsLayout.setVerticalGroup(
            ViewPlaylistsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(ScrollPlaylist, javax.swing.GroupLayout.DEFAULT_SIZE, 560, Short.MAX_VALUE)
        );

        CreateNewPlaylist.setBackground(new java.awt.Color(18, 18, 18));

        NamePlaylist.setBackground(new java.awt.Color(18, 18, 18));
        NamePlaylist.setFont(new java.awt.Font("SansSerif", 3, 14)); // NOI18N
        NamePlaylist.setForeground(new java.awt.Color(255, 255, 255));
        NamePlaylist.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        NamePlaylist.setText("Nueva playlist");
        NamePlaylist.setBorder(null);
        NamePlaylist.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                NamePlaylistActionPerformed(evt);
            }
        });

        AddImgPlaylist.setBackground(new java.awt.Color(18, 18, 18));
        AddImgPlaylist.setFont(new java.awt.Font("SansSerif", 3, 12)); // NOI18N
        AddImgPlaylist.setForeground(new java.awt.Color(255, 255, 255));
        AddImgPlaylist.setText("Agregar imagen");
        AddImgPlaylist.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                AddImgPlaylistActionPerformed(evt);
            }
        });

        SavePlaylist.setBackground(new java.awt.Color(0, 255, 0));
        SavePlaylist.setFont(new java.awt.Font("SansSerif", 3, 12)); // NOI18N
        SavePlaylist.setForeground(new java.awt.Color(255, 255, 255));
        SavePlaylist.setText("Crear Playlist");
        SavePlaylist.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                SavePlaylistActionPerformed(evt);
            }
        });

        CloseCreatorPlaylist.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                CloseCreatorPlaylistMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                CloseCreatorPlaylistMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                CloseCreatorPlaylistMouseExited(evt);
            }
        });

        javax.swing.GroupLayout CreateNewPlaylistLayout = new javax.swing.GroupLayout(CreateNewPlaylist);
        CreateNewPlaylist.setLayout(CreateNewPlaylistLayout);
        CreateNewPlaylistLayout.setHorizontalGroup(
            CreateNewPlaylistLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(CreateNewPlaylistLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(CreateNewPlaylistLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(PlaylistImg, javax.swing.GroupLayout.DEFAULT_SIZE, 220, Short.MAX_VALUE)
                    .addComponent(NamePlaylist)
                    .addComponent(UserCreator, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, CreateNewPlaylistLayout.createSequentialGroup()
                        .addComponent(CloseCreatorPlaylist, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(CreateNewPlaylistLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(AddImgPlaylist, javax.swing.GroupLayout.DEFAULT_SIZE, 125, Short.MAX_VALUE)
                            .addComponent(SavePlaylist, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap())
        );
        CreateNewPlaylistLayout.setVerticalGroup(
            CreateNewPlaylistLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(CreateNewPlaylistLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(PlaylistImg, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(NamePlaylist, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(UserCreator, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(AddImgPlaylist)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 201, Short.MAX_VALUE)
                .addGroup(CreateNewPlaylistLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(SavePlaylist)
                    .addComponent(CloseCreatorPlaylist, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        javax.swing.GroupLayout PlaylistsPanelLayout = new javax.swing.GroupLayout(PlaylistsPanel);
        PlaylistsPanel.setLayout(PlaylistsPanelLayout);
        PlaylistsPanelLayout.setHorizontalGroup(
            PlaylistsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(CreateNewPlaylist, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(PlaylistsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(PlaylistsPanelLayout.createSequentialGroup()
                    .addGap(0, 0, Short.MAX_VALUE)
                    .addComponent(ViewPlaylists, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(0, 0, Short.MAX_VALUE)))
        );
        PlaylistsPanelLayout.setVerticalGroup(
            PlaylistsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(CreateNewPlaylist, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(PlaylistsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(PlaylistsPanelLayout.createSequentialGroup()
                    .addGap(0, 0, Short.MAX_VALUE)
                    .addComponent(ViewPlaylists, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(0, 0, Short.MAX_VALUE)))
        );

        HomePanel.add(PlaylistsPanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 60, 250, 560));

        SongsPanel.setBackground(new java.awt.Color(18, 18, 18));

        PlaylistSongs.setBackground(new java.awt.Color(18, 18, 18));

        TittlePlaylist.setFont(new java.awt.Font("SansSerif", 3, 24)); // NOI18N
        TittlePlaylist.setForeground(new java.awt.Color(255, 255, 255));
        TittlePlaylist.setText("Tu Playlist de favoritos");

        CreatorPlaylist.setFont(new java.awt.Font("SansSerif", 0, 12)); // NOI18N
        CreatorPlaylist.setForeground(new java.awt.Color(255, 255, 255));

        jScrollPane3.setBackground(new java.awt.Color(18, 18, 18));
        jScrollPane3.setBorder(null);

        SongsPlaylist.setBackground(new java.awt.Color(18, 18, 18));
        SongsPlaylist.setBorder(null);
        SongsPlaylist.setFont(new java.awt.Font("SansSerif", 0, 14)); // NOI18N
        SongsPlaylist.setForeground(new java.awt.Color(255, 255, 255));
        jScrollPane3.setViewportView(SongsPlaylist);

        AddCover.setBackground(new java.awt.Color(18, 18, 18));
        AddCover.setFont(new java.awt.Font("SansSerif", 3, 12)); // NOI18N
        AddCover.setForeground(new java.awt.Color(255, 255, 255));
        AddCover.setText("Agregar Caratula");
        AddCover.setBorderPainted(false);
        AddCover.setContentAreaFilled(false);
        AddCover.setFocusPainted(false);
        AddCover.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                AddCoverActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout roundedPanel3Layout = new javax.swing.GroupLayout(roundedPanel3);
        roundedPanel3.setLayout(roundedPanel3Layout);
        roundedPanel3Layout.setHorizontalGroup(
            roundedPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(roundedPanel3Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(AddCover, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        roundedPanel3Layout.setVerticalGroup(
            roundedPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(AddCover, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        AddSongs.setBackground(new java.awt.Color(18, 18, 18));
        AddSongs.setFont(new java.awt.Font("SansSerif", 3, 12)); // NOI18N
        AddSongs.setForeground(new java.awt.Color(255, 255, 255));
        AddSongs.setText("Agregar canciones");
        AddSongs.setBorderPainted(false);
        AddSongs.setContentAreaFilled(false);
        AddSongs.setFocusPainted(false);
        AddSongs.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                AddSongsActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout roundedPanel4Layout = new javax.swing.GroupLayout(roundedPanel4);
        roundedPanel4.setLayout(roundedPanel4Layout);
        roundedPanel4Layout.setHorizontalGroup(
            roundedPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(AddSongs, javax.swing.GroupLayout.DEFAULT_SIZE, 151, Short.MAX_VALUE)
        );
        roundedPanel4Layout.setVerticalGroup(
            roundedPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(AddSongs, javax.swing.GroupLayout.DEFAULT_SIZE, 43, Short.MAX_VALUE)
        );

        AddtoQueePlaylist.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                AddtoQueePlaylistMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                AddtoQueePlaylistMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                AddtoQueePlaylistMouseExited(evt);
            }
        });

        javax.swing.GroupLayout PlaylistSongsLayout = new javax.swing.GroupLayout(PlaylistSongs);
        PlaylistSongs.setLayout(PlaylistSongsLayout);
        PlaylistSongsLayout.setHorizontalGroup(
            PlaylistSongsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(PlaylistSongsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(PlaylistSongsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(PlaylistSongsLayout.createSequentialGroup()
                        .addComponent(PlaylistCover, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(PlaylistSongsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(TittlePlaylist, javax.swing.GroupLayout.PREFERRED_SIZE, 313, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(PlaylistSongsLayout.createSequentialGroup()
                                .addComponent(AddtoQueePlaylist, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(CreatorPlaylist, javax.swing.GroupLayout.PREFERRED_SIZE, 176, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(PlaylistSongsLayout.createSequentialGroup()
                                .addComponent(roundedPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(roundedPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(0, 13, Short.MAX_VALUE))
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.Alignment.TRAILING))
                .addContainerGap())
        );
        PlaylistSongsLayout.setVerticalGroup(
            PlaylistSongsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(PlaylistSongsLayout.createSequentialGroup()
                .addGroup(PlaylistSongsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(PlaylistSongsLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(PlaylistCover, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(PlaylistSongsLayout.createSequentialGroup()
                        .addGap(21, 21, 21)
                        .addComponent(TittlePlaylist, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(PlaylistSongsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(PlaylistSongsLayout.createSequentialGroup()
                                .addComponent(CreatorPlaylist, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addComponent(AddtoQueePlaylist, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(18, 18, 18)
                        .addGroup(PlaylistSongsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(roundedPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(roundedPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addGap(18, 18, 18)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 350, Short.MAX_VALUE)
                .addContainerGap())
        );

        AllSongs.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jScrollPane1.setBackground(new java.awt.Color(18, 18, 18));
        jScrollPane1.setBorder(null);
        jScrollPane1.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        SongsList.setBackground(new java.awt.Color(18, 18, 18));
        SongsList.setBorder(null);
        SongsList.setFont(new java.awt.Font("SansSerif", 1, 14)); // NOI18N
        SongsList.setForeground(new java.awt.Color(255, 255, 255));
        SongsList.setFocusable(false);
        SongsList.setRequestFocusEnabled(false);
        SongsList.setSelectionBackground(new java.awt.Color(18, 18, 18));
        jScrollPane1.setViewportView(SongsList);

        AllSongs.add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 530, 560));

        javax.swing.GroupLayout SongsPanelLayout = new javax.swing.GroupLayout(SongsPanel);
        SongsPanel.setLayout(SongsPanelLayout);
        SongsPanelLayout.setHorizontalGroup(
            SongsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(AllSongs, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(SongsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(PlaylistSongs, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        SongsPanelLayout.setVerticalGroup(
            SongsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(AllSongs, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(SongsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(PlaylistSongs, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        HomePanel.add(SongsPanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(270, 60, 530, 560));

        ReproductorPanel.setBackground(new java.awt.Color(18, 18, 18));
        ReproductorPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        SongInfo.setBackground(new java.awt.Color(18, 18, 18));
        SongInfo.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());
        SongInfo.add(SongImg2, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 10, 240, 240));

        ArtistSong2.setFont(new java.awt.Font("SansSerif", 0, 18)); // NOI18N
        ArtistSong2.setForeground(new java.awt.Color(255, 255, 255));
        SongInfo.add(ArtistSong2, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 290, 220, 20));

        AlbumSong.setFont(new java.awt.Font("SansSerif", 0, 18)); // NOI18N
        AlbumSong.setForeground(new java.awt.Color(255, 255, 255));
        SongInfo.add(AlbumSong, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 320, 230, 20));

        DateSong.setFont(new java.awt.Font("SansSerif", 0, 18)); // NOI18N
        DateSong.setForeground(new java.awt.Color(255, 255, 255));
        SongInfo.add(DateSong, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 350, 220, 20));

        NameSong2.setFont(new java.awt.Font("SansSerif", 3, 18)); // NOI18N
        NameSong2.setForeground(new java.awt.Color(255, 255, 255));
        SongInfo.add(NameSong2, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 260, 220, 20));

        HeartState1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        HeartState1.setText("jLabel9");
        HeartState1.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        HeartState1.setPreferredSize(new java.awt.Dimension(40, 40));
        HeartState1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                HeartState1MouseClicked(evt);
            }
        });
        SongInfo.add(HeartState1, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 400, 50, 50));

        ReproductorPanel.add(SongInfo, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 260, 560));

        ColaRepro.setBackground(new java.awt.Color(18, 18, 18));
        ColaRepro.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        CloseColaR.setText("jLabel3");
        CloseColaR.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                CloseColaRMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                CloseColaRMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                CloseColaRMouseExited(evt);
            }
        });
        ColaRepro.add(CloseColaR, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, -1, 30));

        jScrollPane2.setBackground(new java.awt.Color(18, 18, 18));
        jScrollPane2.setBorder(null);
        jScrollPane2.setHorizontalScrollBar(null);

        ColaReproduccion.setBackground(new java.awt.Color(18, 18, 18));
        ColaReproduccion.setBorder(null);
        ColaReproduccion.setFont(new java.awt.Font("SansSerif", 0, 14)); // NOI18N
        ColaReproduccion.setForeground(new java.awt.Color(255, 255, 255));
        ColaReproduccion.setModel(new javax.swing.AbstractListModel<String>() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public String getElementAt(int i) { return strings[i]; }
        });
        jScrollPane2.setViewportView(ColaReproduccion);

        ColaRepro.add(jScrollPane2, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 30, 240, 520));

        ReproductorPanel.add(ColaRepro, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 260, 560));

        EditUser.setBackground(new java.awt.Color(18, 18, 18));

        lblUserCode.setFont(new java.awt.Font("SansSerif", 1, 14)); // NOI18N
        lblUserCode.setForeground(new java.awt.Color(255, 255, 255));

        txtAliasPerfil.setBackground(new java.awt.Color(18, 18, 18));
        txtAliasPerfil.setFont(new java.awt.Font("SansSerif", 1, 14)); // NOI18N
        txtAliasPerfil.setForeground(new java.awt.Color(255, 255, 255));
        txtAliasPerfil.setBorder(null);

        jLabel3.setBackground(new java.awt.Color(18, 18, 18));
        jLabel3.setFont(new java.awt.Font("SansSerif", 0, 12)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(255, 255, 255));
        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel3.setText("Cambiar contraseña");

        jLabel4.setBackground(new java.awt.Color(18, 18, 18));
        jLabel4.setFont(new java.awt.Font("SansSerif", 0, 12)); // NOI18N
        jLabel4.setForeground(new java.awt.Color(255, 255, 255));
        jLabel4.setText("nueva contraseña");

        txtNewPassword.setBackground(new java.awt.Color(18, 18, 18));
        txtNewPassword.setFont(new java.awt.Font("SansSerif", 3, 12)); // NOI18N
        txtNewPassword.setForeground(new java.awt.Color(255, 255, 255));

        jLabel5.setFont(new java.awt.Font("SansSerif", 0, 12)); // NOI18N
        jLabel5.setForeground(new java.awt.Color(255, 255, 255));
        jLabel5.setText("Confirma la contraseña");

        txtPasswordConfirmar.setBackground(new java.awt.Color(18, 18, 18));
        txtPasswordConfirmar.setFont(new java.awt.Font("SansSerif", 3, 12)); // NOI18N
        txtPasswordConfirmar.setForeground(new java.awt.Color(255, 255, 255));
        txtPasswordConfirmar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtPasswordConfirmarActionPerformed(evt);
            }
        });

        btnGuardarPerfil.setBackground(new java.awt.Color(18, 18, 18));
        btnGuardarPerfil.setFont(new java.awt.Font("SansSerif", 3, 12)); // NOI18N
        btnGuardarPerfil.setForeground(new java.awt.Color(255, 255, 255));
        btnGuardarPerfil.setText("Guardar");
        btnGuardarPerfil.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnGuardarPerfilActionPerformed(evt);
            }
        });

        jLabel6.setFont(new java.awt.Font("SansSerif", 0, 12)); // NOI18N
        jLabel6.setForeground(new java.awt.Color(153, 153, 153));
        jLabel6.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel6.setText("Alias");

        CloseEditU.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                CloseEditUMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                CloseEditUMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                CloseEditUMouseExited(evt);
            }
        });

        AddPhoto.setBackground(new java.awt.Color(18, 18, 18));
        AddPhoto.setFont(new java.awt.Font("SansSerif", 3, 12)); // NOI18N
        AddPhoto.setForeground(new java.awt.Color(255, 255, 255));
        AddPhoto.setText("Agregar foto de perfil");
        AddPhoto.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                AddPhotoActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout EditUserLayout = new javax.swing.GroupLayout(EditUser);
        EditUser.setLayout(EditUserLayout);
        EditUserLayout.setHorizontalGroup(
            EditUserLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, EditUserLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(EditUserLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, EditUserLayout.createSequentialGroup()
                        .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 64, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(95, 95, 95))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, EditUserLayout.createSequentialGroup()
                        .addComponent(CloseEditU, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap())))
            .addGroup(EditUserLayout.createSequentialGroup()
                .addGap(37, 37, 37)
                .addGroup(EditUserLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(lblUserCode, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblUserImg, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 180, Short.MAX_VALUE)
                    .addComponent(txtAliasPerfil, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel4, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(txtNewPassword, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel5, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(txtPasswordConfirmar, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnGuardarPerfil)
                    .addComponent(lblPasswordError, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(AddPhoto, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(43, Short.MAX_VALUE))
        );
        EditUserLayout.setVerticalGroup(
            EditUserLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(EditUserLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblUserImg, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblUserCode, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel6)
                .addGap(2, 2, 2)
                .addComponent(txtAliasPerfil, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(txtNewPassword, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtPasswordConfirmar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnGuardarPerfil)
                .addGap(4, 4, 4)
                .addComponent(lblPasswordError, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(AddPhoto)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 43, Short.MAX_VALUE)
                .addComponent(CloseEditU, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        ReproductorPanel.add(EditUser, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 260, 560));

        HomePanel.add(ReproductorPanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(810, 60, 260, 560));

        RegisterPanel.setBackground(new java.awt.Color(0, 0, 0));
        RegisterPanel.setMaximumSize(new java.awt.Dimension(1080, 720));
        RegisterPanel.setMinimumSize(new java.awt.Dimension(1080, 720));

        jPanel1.setBackground(new java.awt.Color(18, 18, 18));
        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        CodeL.setFont(new java.awt.Font("SansSerif", 3, 12)); // NOI18N
        CodeL.setForeground(new java.awt.Color(255, 255, 255));
        CodeL.setText("Codigo de estudiante");
        jPanel1.add(CodeL, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 200, 520, 20));

        NewCodeField.setBackground(new java.awt.Color(18, 18, 18));
        NewCodeField.setFont(new java.awt.Font("SansSerif", 3, 12)); // NOI18N
        NewCodeField.setForeground(new java.awt.Color(255, 255, 255));
        NewCodeField.setBorder(javax.swing.BorderFactory.createEtchedBorder(java.awt.Color.lightGray, java.awt.Color.lightGray));
        NewCodeField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                NewCodeFieldActionPerformed(evt);
            }
        });
        jPanel1.add(NewCodeField, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 240, 500, 30));

        ReadCL.setFont(new java.awt.Font("SansSerif", 3, 12)); // NOI18N
        ReadCL.setForeground(new java.awt.Color(255, 255, 255));
        ReadCL.setText("Escriba una contraseña");
        jPanel1.add(ReadCL, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 290, 500, 20));

        NewPasswordField.setBackground(new java.awt.Color(18, 18, 18));
        NewPasswordField.setFont(new java.awt.Font("SansSerif", 3, 14)); // NOI18N
        NewPasswordField.setForeground(new java.awt.Color(255, 255, 255));
        NewPasswordField.setBorder(javax.swing.BorderFactory.createEtchedBorder(java.awt.Color.lightGray, java.awt.Color.lightGray));
        NewPasswordField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                NewPasswordFieldActionPerformed(evt);
            }
        });
        jPanel1.add(NewPasswordField, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 330, 500, 30));

        RepCLabel.setFont(new java.awt.Font("SansSerif", 3, 12)); // NOI18N
        RepCLabel.setForeground(new java.awt.Color(255, 255, 255));
        RepCLabel.setText("Repita la contraseña");
        jPanel1.add(RepCLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 380, 500, 20));

        RepeatPasswordField.setBackground(new java.awt.Color(18, 18, 18));
        RepeatPasswordField.setFont(new java.awt.Font("SansSerif", 3, 14)); // NOI18N
        RepeatPasswordField.setForeground(new java.awt.Color(255, 255, 255));
        RepeatPasswordField.setBorder(javax.swing.BorderFactory.createEtchedBorder(java.awt.Color.lightGray, java.awt.Color.lightGray));
        jPanel1.add(RepeatPasswordField, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 420, 500, 30));

        RegisterFBt.setBackground(new java.awt.Color(18, 18, 18));
        RegisterFBt.setFont(new java.awt.Font("SansSerif", 3, 14)); // NOI18N
        RegisterFBt.setForeground(new java.awt.Color(255, 255, 255));
        RegisterFBt.setText("registrar");
        RegisterFBt.setBorder(javax.swing.BorderFactory.createEtchedBorder(java.awt.Color.lightGray, java.awt.Color.lightGray));
        RegisterFBt.setFocusPainted(false);
        RegisterFBt.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                RegisterFBtActionPerformed(evt);
            }
        });
        jPanel1.add(RegisterFBt, new org.netbeans.lib.awtextra.AbsoluteConstraints(300, 480, 230, -1));
        jPanel1.add(RegisterErrorLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 460, 520, -1));

        RegisterIconImg.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/170x170.png"))); // NOI18N
        jPanel1.add(RegisterIconImg, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 10, 170, 170));

        registrar.setFont(new java.awt.Font("SansSerif", 3, 36)); // NOI18N
        registrar.setForeground(new java.awt.Color(255, 255, 255));
        registrar.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        registrar.setText("Registar");
        jPanel1.add(registrar, new org.netbeans.lib.awtextra.AbsoluteConstraints(230, 30, 300, 40));

        Usuarios.setFont(new java.awt.Font("SansSerif", 3, 48)); // NOI18N
        Usuarios.setForeground(new java.awt.Color(255, 255, 255));
        Usuarios.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        Usuarios.setText("Usuarios");
        jPanel1.add(Usuarios, new org.netbeans.lib.awtextra.AbsoluteConstraints(220, 70, 310, 40));

        GoogleBt.setBorderColor(java.awt.Color.lightGray);
        GoogleBt.setPanelColor(new java.awt.Color(18, 18, 18));
        GoogleBt.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                GoogleBtMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                GoogleBtMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                GoogleBtMouseExited(evt);
            }
        });
        GoogleBt.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel2.setFont(new java.awt.Font("SansSerif", 1, 18)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(255, 255, 255));
        jLabel2.setText("Continuar con Google");
        GoogleBt.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(160, 0, 202, 50));

        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/G 40x40.png"))); // NOI18N
        GoogleBt.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 0, -1, 50));

        jPanel1.add(GoogleBt, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 530, 500, 50));

        TaskBar2.setBackground(new java.awt.Color(0, 0, 0));
        TaskBar2.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseDragged(java.awt.event.MouseEvent evt) {
                TaskBar2MouseDragged(evt);
            }
        });
        TaskBar2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                TaskBar2MousePressed(evt);
            }
        });
        TaskBar2.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        XBtL.setBackground(new java.awt.Color(0, 0, 0));

        Xlabel2.setFont(new java.awt.Font("SansSerif", 3, 36)); // NOI18N
        Xlabel2.setForeground(new java.awt.Color(255, 255, 255));
        Xlabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        Xlabel2.setText("x");
        Xlabel2.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        Xlabel2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                Xlabel2MouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                Xlabel2MouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                Xlabel2MouseExited(evt);
            }
        });

        javax.swing.GroupLayout XBtLLayout = new javax.swing.GroupLayout(XBtL);
        XBtL.setLayout(XBtLLayout);
        XBtLLayout.setHorizontalGroup(
            XBtLLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, XBtLLayout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(Xlabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        XBtLLayout.setVerticalGroup(
            XBtLLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(XBtLLayout.createSequentialGroup()
                .addComponent(Xlabel2)
                .addGap(0, 3, Short.MAX_VALUE))
        );

        TaskBar2.add(XBtL, new org.netbeans.lib.awtextra.AbsoluteConstraints(1020, 0, 60, 50));

        MinBtL.setBackground(new java.awt.Color(0, 0, 0));

        MinLabel2.setBackground(new java.awt.Color(0, 0, 0));
        MinLabel2.setFont(new java.awt.Font("SansSerif", 3, 36)); // NOI18N
        MinLabel2.setForeground(new java.awt.Color(255, 255, 255));
        MinLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        MinLabel2.setText("-");
        MinLabel2.setToolTipText("");
        MinLabel2.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        MinLabel2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                MinLabel2MouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                MinLabel2MouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                MinLabel2MouseExited(evt);
            }
        });

        javax.swing.GroupLayout MinBtLLayout = new javax.swing.GroupLayout(MinBtL);
        MinBtL.setLayout(MinBtLLayout);
        MinBtLLayout.setHorizontalGroup(
            MinBtLLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, MinBtLLayout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(MinLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        MinBtLLayout.setVerticalGroup(
            MinBtLLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(MinBtLLayout.createSequentialGroup()
                .addComponent(MinLabel2)
                .addGap(0, 6, Short.MAX_VALUE))
        );

        TaskBar2.add(MinBtL, new org.netbeans.lib.awtextra.AbsoluteConstraints(960, 0, 60, 50));

        javax.swing.GroupLayout RegisterPanelLayout = new javax.swing.GroupLayout(RegisterPanel);
        RegisterPanel.setLayout(RegisterPanelLayout);
        RegisterPanelLayout.setHorizontalGroup(
            RegisterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, RegisterPanelLayout.createSequentialGroup()
                .addGap(263, 263, 263)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addComponent(TaskBar2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        RegisterPanelLayout.setVerticalGroup(
            RegisterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(RegisterPanelLayout.createSequentialGroup()
                .addComponent(TaskBar2, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(32, 32, 32)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 609, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(31, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(LoginPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addGap(0, 0, Short.MAX_VALUE)
                    .addComponent(HomePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(0, 0, Short.MAX_VALUE)))
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addGap(0, 0, Short.MAX_VALUE)
                    .addComponent(RegisterPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(0, 0, Short.MAX_VALUE)))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(LoginPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addGap(0, 0, Short.MAX_VALUE)
                    .addComponent(HomePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(0, 0, Short.MAX_VALUE)))
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addGap(0, 0, Short.MAX_VALUE)
                    .addComponent(RegisterPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(0, 0, Short.MAX_VALUE)))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void RegisterBtActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_RegisterBtActionPerformed
        LoginPanel.setVisible(false);
        RegisterPanel.setVisible(true);

        // Limpiar posibles mensajes o campos previos
        RegisterErrorLabel.setText("");
        NewCodeField.setText("");
        NewPasswordField.setText("");
        RepeatPasswordField.setText("");
    }//GEN-LAST:event_RegisterBtActionPerformed

    private void EnterSButonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_EnterSButonActionPerformed
    String codigo = CodeField.getText();
    String contraseña = new String(PasswordField.getPassword());

    if (codigo.isEmpty() || contraseña.isEmpty()) {
        ErrorLabel.setText("Por favor, llena todos los campos.");
        return;
    }

    // Obtener el usuario completo desde el archivo
    Usuario usuario = UsuarioManager.obtenerUsuario(codigo, contraseña);

    if (usuario != null) {
        usuarioActual = usuario; // ← aquí lo guardas correctamente
        LoginPanel.setVisible(false);
        HomePanel.setVisible(true);
        ErrorLabel.setText(""); // limpiar por si había mensaje anterior
        actualizarFotoDeUsuario();
    } else {
        ErrorLabel.setText("Código o contraseña incorrectos.");
    }
    
    if (usuarioActual != null && usuarioActual.isAdmin()) {
        NewPlaylistManager.setVisible(true);
    } else {
        NewPlaylistManager.setVisible(false);
    }
    }//GEN-LAST:event_EnterSButonActionPerformed

    private void XlabelMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_XlabelMouseEntered
        Xpanel.setBackground(Color.red);
    }//GEN-LAST:event_XlabelMouseEntered

    private void XlabelMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_XlabelMouseExited
        Xpanel.setBackground(new Color(18, 18, 18));
    }//GEN-LAST:event_XlabelMouseExited

    private void XlabelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_XlabelMouseClicked
        System.exit(0);
    }//GEN-LAST:event_XlabelMouseClicked

    private void MinLabelMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_MinLabelMouseEntered
        MinPanel.setBackground(Color.GRAY);
    }//GEN-LAST:event_MinLabelMouseEntered

    private void MinLabelMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_MinLabelMouseExited
        MinPanel.setBackground(new Color(18, 18, 18));
    }//GEN-LAST:event_MinLabelMouseExited

    private void TaskBarMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_TaskBarMousePressed
        xMouse = evt.getX();
        yMouse = evt.getY();
    }//GEN-LAST:event_TaskBarMousePressed

    private void TaskBarMouseDragged(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_TaskBarMouseDragged
        int x = evt.getXOnScreen();
        int y = evt.getYOnScreen();
        this.setLocation(x - xMouse, y - yMouse);
    }//GEN-LAST:event_TaskBarMouseDragged

    private void Xlabel1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_Xlabel1MouseClicked
        System.exit(0);
    }//GEN-LAST:event_Xlabel1MouseClicked

    private void Xlabel1MouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_Xlabel1MouseEntered
        Xpanel1.setBackground(Color.red);
    }//GEN-LAST:event_Xlabel1MouseEntered

    private void Xlabel1MouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_Xlabel1MouseExited
        Xpanel1.setBackground(Color.BLACK);
    }//GEN-LAST:event_Xlabel1MouseExited

    private void MinLabel1MouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_MinLabel1MouseEntered
        MinPanel1.setBackground(Color.GRAY);
    }//GEN-LAST:event_MinLabel1MouseEntered

    private void MinLabel1MouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_MinLabel1MouseExited
        MinPanel1.setBackground(Color.BLACK);
    }//GEN-LAST:event_MinLabel1MouseExited

    private void TaskBar1MouseDragged(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_TaskBar1MouseDragged
        int x = evt.getXOnScreen();
        int y = evt.getYOnScreen();
        this.setLocation(x - xMouse, y - yMouse);
    }//GEN-LAST:event_TaskBar1MouseDragged

    private void TaskBar1MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_TaskBar1MousePressed
        xMouse = evt.getX();
        yMouse = evt.getY();
    }//GEN-LAST:event_TaskBar1MousePressed

    private void MinLabel1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_MinLabel1MouseClicked
        this.setState(JFrame.ICONIFIED); 
    }//GEN-LAST:event_MinLabel1MouseClicked

    private void MinLabelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_MinLabelMouseClicked
        this.setState(JFrame.ICONIFIED); 
    }//GEN-LAST:event_MinLabelMouseClicked

    private void PlayBtMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_PlayBtMouseClicked
    if (musicPlayer.isPlaying()) {

        musicPlayer.pause();
        PlayBt.setText(EmojiParser.parseToUnicode(":arrow_forward:")); // ▶️
    } else if (musicPlayer.isPaused()) {
        // Reanudar desde PAUSED
// Verifica si el reproductor estaba en pausa
if (musicPlayer.getState() == MusicPlayer.PlayerState.PAUSED) {
    musicPlayer.resume(); // ✅ Reanuda desde donde se pausó
} else {
    musicPlayer.play(); // 🔁 Reproduce normalmente si no estaba pausado
    Song current = musicPlayer.getCurrentSong();
    if (current != null) {
        iniciarBarraProgreso(current); // ⏳ Solo reinicia barra si es una nueva reproducción
    }
}
        PlayBt.setText(EmojiParser.parseToUnicode(":double_vertical_bar:")); // ⏸️
    } else {
        // Reproducción nueva
        if (!playlistSongs.isEmpty() && currentPlaylistIndex >= 0 && currentPlaylistIndex < playlistSongs.size()) {
            musicPlayer.setPlaylist(playlistSongs); // Aseguramos que tenga la lista actual
            musicPlayer.setCurrentTrackIndex(currentPlaylistIndex);
            // Verifica si el reproductor estaba en pausa
if (musicPlayer.getState() == MusicPlayer.PlayerState.PAUSED) {
    musicPlayer.play(); // Reanuda desde donde se pausó (ya lo implementaste en la clase)
} else {
    musicPlayer.play(); // Reproduce normalmente si no estaba pausado
    Song current = musicPlayer.getCurrentSong();
    if (current != null) {
        iniciarBarraProgreso(current); // Solo reinicia barra si es una nueva reproducción
    }
}
            PlayBt.setText(EmojiParser.parseToUnicode(":double_vertical_bar:")); // ⏸️
        } else {
            JOptionPane.showMessageDialog(this, "⚠ No hay canciones en la cola de reproducción.", "Error", JOptionPane.WARNING_MESSAGE);
        }
    }

    PlayBt.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 30));
    }//GEN-LAST:event_PlayBtMouseClicked

    private void PlayBtMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_PlayBtMouseEntered
        PlayBt.setForeground(Color.GRAY);
    }//GEN-LAST:event_PlayBtMouseEntered

    private void PlayBtMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_PlayBtMouseExited
        PlayBt.setForeground(Color.WHITE);
    }//GEN-LAST:event_PlayBtMouseExited

    private void NextBtMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_NextBtMouseClicked
    if (playlistSongs.isEmpty()) return;

    if (isRandom) {
        if (!cancionesPendientes.isEmpty()) {
            int randomIndex = new Random().nextInt(cancionesPendientes.size());
            Song selectedSong = cancionesPendientes.remove(randomIndex);
            currentPlaylistIndex = playlistSongs.indexOf(selectedSong);
        } else {
            reiniciarListaAleatoria(); // Vuelve a llenar cancionesPendientes
            return;
        }
    } else {
        if (repeatState == 2) {
            // Repeat One: se queda en la misma canción
        } else if (repeatState == 1) {
            // Repeat All: avanzar y dar la vuelta si se pasa del final
            currentPlaylistIndex = (currentPlaylistIndex + 1) % playlistSongs.size();
        } else {
            // Normal
            if (currentPlaylistIndex < playlistSongs.size() - 1) {
                currentPlaylistIndex++;
            } else {
                System.out.println("⚠ Fin de la lista.");
                return;
            }
        }
    }

    Song currentSong = playlistSongs.get(currentPlaylistIndex);
    updateSongInfo(currentSong);
    musicPlayer.setPlaylist(playlistSongs);
    musicPlayer.setCurrentTrackIndex(currentPlaylistIndex);
// Verifica si el reproductor estaba en pausa
if (musicPlayer.getState() == MusicPlayer.PlayerState.PAUSED) {
    musicPlayer.resume(); // ✅ Reanuda desde donde se pausó
} else {
    musicPlayer.play(); // 🔁 Reproduce normalmente si no estaba pausado
    Song current = musicPlayer.getCurrentSong();
    if (current != null) {
        iniciarBarraProgreso(current); // ⏳ Solo reinicia barra si es una nueva reproducción
    }
}
    PlayBt.setText(EmojiParser.parseToUnicode(":double_vertical_bar:")); // ⏸️
    PlayBt.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 30));

    actualizarEstadoMeGusta();
    updatePlaylistModel();
    }//GEN-LAST:event_NextBtMouseClicked

    private void NextBtMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_NextBtMouseEntered
        NextBt.setForeground(Color.WHITE);
    }//GEN-LAST:event_NextBtMouseEntered

    private void NextBtMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_NextBtMouseExited
        NextBt.setForeground(Color.GRAY);
    }//GEN-LAST:event_NextBtMouseExited

    private void RandomBtMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_RandomBtMouseClicked
    isRandom = !isRandom; // Alternar estado aleatorio

    if (isRandom) {
        RandomBt.setForeground(Color.BLUE);
        System.out.println("🔀 Modo aleatorio activado.");
        reiniciarListaAleatoria();
    } else {
        RandomBt.setForeground(Color.WHITE);
        System.out.println("➡ Modo normal activado.");
    }
    }//GEN-LAST:event_RandomBtMouseClicked

    private void RandomBtMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_RandomBtMouseEntered
        
    }//GEN-LAST:event_RandomBtMouseEntered

    private void RandomBtMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_RandomBtMouseExited
        
    }//GEN-LAST:event_RandomBtMouseExited

    private void BackBtMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_BackBtMouseClicked
    if (playlistSongs.isEmpty()) return;

    if (isRandom) {
        if (!cancionesPendientes.isEmpty()) {
            Song selectedSong = cancionesPendientes.remove(cancionesPendientes.size() - 1);
            currentPlaylistIndex = playlistSongs.indexOf(selectedSong);
        } else {
            System.out.println("⚠ No hay canciones previas aleatorias.");
            return;
        }
    } else if (repeatState == 2) {
        // Repeat One: quedarse en la misma canción
    } else if (repeatState == 1) {
        // Repeat All: retroceder circularmente
        currentPlaylistIndex = (currentPlaylistIndex - 1 + playlistSongs.size()) % playlistSongs.size();
    } else {
        if (currentPlaylistIndex > 0) {
            currentPlaylistIndex--;
        } else {
            System.out.println("⚠ Inicio de la lista.");
            return;
        }
    }

    Song currentSong = playlistSongs.get(currentPlaylistIndex);
    updateSongInfo(currentSong);
    musicPlayer.setPlaylist(playlistSongs);
    musicPlayer.setCurrentTrackIndex(currentPlaylistIndex);
// Verifica si el reproductor estaba en pausa
if (musicPlayer.getState() == MusicPlayer.PlayerState.PAUSED) {
    musicPlayer.resume(); // ✅ Reanuda desde donde se pausó
} else {
    musicPlayer.play(); // 🔁 Reproduce normalmente si no estaba pausado
    Song current = musicPlayer.getCurrentSong();
    if (current != null) {
        iniciarBarraProgreso(current); // ⏳ Solo reinicia barra si es una nueva reproducción
    }
}
    PlayBt.setText(EmojiParser.parseToUnicode(":double_vertical_bar:")); // ⏸️
    PlayBt.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 30));

    actualizarEstadoMeGusta();
    updatePlaylistModel();
    }//GEN-LAST:event_BackBtMouseClicked

    private void BackBtMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_BackBtMouseEntered
        BackBt.setForeground(Color.WHITE);
    }//GEN-LAST:event_BackBtMouseEntered

    private void BackBtMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_BackBtMouseExited
        BackBt.setForeground(Color.GRAY);
    }//GEN-LAST:event_BackBtMouseExited

    private void RepeatBtMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_RepeatBtMouseClicked
    repeatState = (repeatState + 1) % 3; // Avanzar al siguiente estado cíclico

    switch (repeatState) {
        case 1 -> {
            // Estado 1: repeat azul
            RepeatBt.setText(EmojiParser.parseToUnicode(":repeat:"));
            RepeatBt.setForeground(Color.BLUE);
            }
        case 2 -> {
            // Estado 2: repeat_one azul
            RepeatBt.setText(EmojiParser.parseToUnicode(":repeat_one:"));
            RepeatBt.setForeground(Color.BLUE);
            }
        default -> {
            // Estado 0: repeat blanco
            RepeatBt.setText(EmojiParser.parseToUnicode(":repeat:"));
            RepeatBt.setForeground(Color.WHITE);
            }
    }

    RepeatBt.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 30));
    }//GEN-LAST:event_RepeatBtMouseClicked

    private void RepeatBtMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_RepeatBtMouseEntered
        // TODO add your handling code here:
    }//GEN-LAST:event_RepeatBtMouseEntered

    private void RepeatBtMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_RepeatBtMouseExited
        // TODO add your handling code here:
    }//GEN-LAST:event_RepeatBtMouseExited

    private void SoundBtMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_SoundBtMouseClicked
        if (!isMuted) {
            // Guardar volumen actual y silenciar
            previousVolume = VolumeSlider.getValue();
            VolumeSlider.setValue(0); // Esto actualizará el emoji automáticamente si tienes el listener
            isMuted = true;
        } else {
            // Restaurar volumen anterior
            VolumeSlider.setValue(previousVolume);
            isMuted = false;
        }
    }//GEN-LAST:event_SoundBtMouseClicked

    private void SoundBtMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_SoundBtMouseEntered
        SoundBt.setForeground(Color.GRAY);
    }//GEN-LAST:event_SoundBtMouseEntered

    private void SoundBtMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_SoundBtMouseExited
        SoundBt.setForeground(Color.WHITE);
    }//GEN-LAST:event_SoundBtMouseExited

    private void NewCodeFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_NewCodeFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_NewCodeFieldActionPerformed

    private void RegisterFBtActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_RegisterFBtActionPerformed
    String codigo = NewCodeField.getText().trim();
    String contraseña = new String(NewPasswordField.getPassword());
    String repetirContraseña = new String(RepeatPasswordField.getPassword());

    if (codigo.isEmpty() || contraseña.isEmpty() || repetirContraseña.isEmpty()) {
        RegisterErrorLabel.setForeground(Color.RED);
        RegisterErrorLabel.setText("Todos los campos son obligatorios.");
        return;
    }

    if (!codigo.matches("\\d+")) {
        RegisterErrorLabel.setForeground(Color.RED);
        RegisterErrorLabel.setText("El código debe ser solo números.");
        return;
    }

    if (!contraseña.equals(repetirContraseña)) {
        RegisterErrorLabel.setForeground(Color.RED);
        RegisterErrorLabel.setText("Las contraseñas no coinciden.");
        return;
    }

    try (BufferedWriter writer = new BufferedWriter(new FileWriter("usuarios.txt", true))) {
        writer.write(codigo + "," + contraseña);
        writer.newLine();

        RegisterErrorLabel.setForeground(Color.GREEN);
        RegisterErrorLabel.setText("Usuario registrado con éxito.");

        // Limpiar campos
        NewCodeField.setText("");
        NewPasswordField.setText("");
        RepeatPasswordField.setText("");

        // Volver al LoginPanel luego de 5 segundos
        Timer timer = new Timer(5000, (ActionEvent e) -> {
            RegisterErrorLabel.setText(""); // Limpiar mensaje
            LoginPanel.setVisible(true);
            RegisterPanel.setVisible(false);
            UsuarioManager.limpiarYCorregirArchivoUsuarios();
        });
        timer.setRepeats(false); // Solo una vez
        timer.start();

    } catch (IOException e) {
        RegisterErrorLabel.setForeground(Color.RED);
        RegisterErrorLabel.setText("Error al guardar el usuario.");
    }
    }//GEN-LAST:event_RegisterFBtActionPerformed

    private void NewPasswordFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_NewPasswordFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_NewPasswordFieldActionPerformed

    private void UserImgMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_UserImgMouseEntered
        UserImg.setForeground(Color.WHITE);
    }//GEN-LAST:event_UserImgMouseEntered

    private void UserImgMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_UserImgMouseExited
        UserImg.setForeground(Color.GRAY);
    }//GEN-LAST:event_UserImgMouseExited

    private void FriendsImgMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_FriendsImgMouseEntered
        FriendsImg.setForeground(Color.WHITE);
    }//GEN-LAST:event_FriendsImgMouseEntered

    private void FriendsImgMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_FriendsImgMouseExited
        FriendsImg.setForeground(Color.GRAY);
    }//GEN-LAST:event_FriendsImgMouseExited

    private void HomeIconMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_HomeIconMouseEntered
        HomeIcon.setForeground(Color.WHITE);
    }//GEN-LAST:event_HomeIconMouseEntered

    private void HomeIconMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_HomeIconMouseExited
        HomeIcon.setForeground(Color.GRAY);
    }//GEN-LAST:event_HomeIconMouseExited

    private void SearchIconMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_SearchIconMouseEntered
        SearchIcon.setForeground(Color.WHITE);
    }//GEN-LAST:event_SearchIconMouseEntered

    private void SearchIconMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_SearchIconMouseExited
        SearchIcon.setForeground(Color.GRAY);
    }//GEN-LAST:event_SearchIconMouseExited

    private void GoogleBtMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_GoogleBtMouseEntered
        GoogleBt.setBorderColor(Color.WHITE);
    }//GEN-LAST:event_GoogleBtMouseEntered

    private void GoogleBtMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_GoogleBtMouseExited
        GoogleBt.setBorderColor(Color.GRAY);
    }//GEN-LAST:event_GoogleBtMouseExited

    private void Xlabel2MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_Xlabel2MouseClicked
        System.exit(0);
    }//GEN-LAST:event_Xlabel2MouseClicked

    private void Xlabel2MouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_Xlabel2MouseEntered
        XBtL.setBackground(Color.red);
    }//GEN-LAST:event_Xlabel2MouseEntered

    private void Xlabel2MouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_Xlabel2MouseExited
        XBtL.setBackground(Color.BLACK);
    }//GEN-LAST:event_Xlabel2MouseExited

    private void MinLabel2MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_MinLabel2MouseClicked
        this.setState(JFrame.ICONIFIED);
    }//GEN-LAST:event_MinLabel2MouseClicked

    private void MinLabel2MouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_MinLabel2MouseEntered
        MinBtL.setBackground(Color.GRAY);
    }//GEN-LAST:event_MinLabel2MouseEntered

    private void MinLabel2MouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_MinLabel2MouseExited
        MinBtL.setBackground(Color.BLACK);
    }//GEN-LAST:event_MinLabel2MouseExited

    private void TaskBar2MouseDragged(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_TaskBar2MouseDragged
        int x = evt.getXOnScreen();
        int y = evt.getYOnScreen();
        this.setLocation(x - xMouse, y - yMouse);
    }//GEN-LAST:event_TaskBar2MouseDragged

    private void TaskBar2MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_TaskBar2MousePressed
        xMouse = evt.getX();
        yMouse = evt.getY();
    }//GEN-LAST:event_TaskBar2MousePressed

    private void CloseColaRMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_CloseColaRMouseEntered
        CloseColaR.setForeground(Color.GRAY);
    }//GEN-LAST:event_CloseColaRMouseEntered

    private void CloseColaRMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_CloseColaRMouseExited
        CloseColaR.setForeground(Color.WHITE);
    }//GEN-LAST:event_CloseColaRMouseExited

    private void CloseColaRMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_CloseColaRMouseClicked
        ColaRepro.setVisible(false);
        SongInfo.setVisible(true);
        EditUser.setVisible(false);
    }//GEN-LAST:event_CloseColaRMouseClicked

    private void OpenColaRMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_OpenColaRMouseEntered
        OpenColaR.setForeground(Color.GRAY);
    }//GEN-LAST:event_OpenColaRMouseEntered

    private void OpenColaRMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_OpenColaRMouseExited
        OpenColaR.setForeground(Color.WHITE);
    }//GEN-LAST:event_OpenColaRMouseExited

    private void OpenColaRMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_OpenColaRMouseClicked
        ColaRepro.setVisible(true);
        SongInfo.setVisible(false);
        EditUser.setVisible(false);
    }//GEN-LAST:event_OpenColaRMouseClicked

    private void UserImgMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_UserImgMouseClicked
    EditUser.setVisible(true); 
    SongInfo.setVisible(false);
    ColaRepro.setVisible(false);
    
    if (usuarioActual == null) return;

    // Llena los campos con los datos actuales
    lblUserCode.setText("Usuario: " + usuarioActual.getCodigo());
    txtAliasPerfil.setText(usuarioActual.getNombre() != null ? usuarioActual.getNombre() : "");
    txtNewPassword.setText("");
    txtPasswordConfirmar.setText("");

    // Mostrar la imagen de perfil si existe
    String ruta = usuarioActual.getFotoRuta();
    if (ruta != null && !ruta.isEmpty()) {
        File imgFile = new File(ruta);
        if (imgFile.exists()) {
            ImageIcon icono = new ImageIcon(ruta);
            Image imagen = icono.getImage().getScaledInstance(
                lblUserImg.getWidth(), lblUserImg.getHeight(), Image.SCALE_SMOOTH
            );
            lblUserImg.setIcon(new ImageIcon(imagen));
        } else {
            System.out.println("La imagen no existe: " + ruta);
            lblUserImg.setIcon(null);
        }
    } else {
        lblUserImg.setIcon(null);
    }
    }//GEN-LAST:event_UserImgMouseClicked

    private void btnGuardarPerfilActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGuardarPerfilActionPerformed
    if (usuarioActual == null) {
        JOptionPane.showMessageDialog(this, "Error: No hay usuario cargado.");
        return;
    }
    
    String nuevoAlias = txtAliasPerfil.getText();
    String nuevaPassword = new String(txtNewPassword.getPassword());
    String confirmarPassword = new String(txtPasswordConfirmar.getPassword());

    

    if (!nuevaPassword.isEmpty() && !nuevaPassword.equals(confirmarPassword)) {
        lblPasswordError.setText("Las contraseñas no coinciden");
        return;
    }

    boolean cambios = false;

    if (!nuevoAlias.equals(usuarioActual.getNombre())) {
        usuarioActual.setNombre(nuevoAlias);
        cambios = true;
    }

    if (!nuevaPassword.isEmpty()) {
        usuarioActual.setContraseña(nuevaPassword);
        cambios = true;
    }

    if (cambios) {
        UsuarioManager.actualizarUsuario(usuarioActual);
        JOptionPane.showMessageDialog(this, "Perfil actualizado correctamente.");
    }

    UsuarioManager.limpiarYCorregirArchivoUsuarios();
    EditUser.setVisible(false);
    SongInfo.setVisible(true);  
    if (usuarioActual != null && usuarioActual.isAdmin()) {
        NewPlaylistManager.setVisible(true);
    } else {
        NewPlaylistManager.setVisible(false);
    }
    }//GEN-LAST:event_btnGuardarPerfilActionPerformed

    private void CloseEditUMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_CloseEditUMouseClicked
    EditUser.setVisible(false);
    SongInfo.setVisible(true); 
    ColaRepro.setVisible(false);
    }//GEN-LAST:event_CloseEditUMouseClicked

    private void CloseEditUMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_CloseEditUMouseEntered
        CloseEditU.setForeground(Color.GRAY);
    }//GEN-LAST:event_CloseEditUMouseEntered

    private void CloseEditUMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_CloseEditUMouseExited
        CloseEditU.setForeground(Color.WHITE);
    }//GEN-LAST:event_CloseEditUMouseExited

    private void AddPhotoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_AddPhotoActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_AddPhotoActionPerformed

    private void txtPasswordConfirmarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtPasswordConfirmarActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtPasswordConfirmarActionPerformed

    private void NamePlaylistActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_NamePlaylistActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_NamePlaylistActionPerformed

    private void CloseCreatorPlaylistMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_CloseCreatorPlaylistMouseClicked
        CreateNewPlaylist.setVisible(false);
        ViewPlaylists.setVisible(true);
    }//GEN-LAST:event_CloseCreatorPlaylistMouseClicked

    private void CloseCreatorPlaylistMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_CloseCreatorPlaylistMouseEntered
        CloseCreatorPlaylist.setForeground(Color.GRAY);
    }//GEN-LAST:event_CloseCreatorPlaylistMouseEntered

    private void CloseCreatorPlaylistMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_CloseCreatorPlaylistMouseExited
        CloseCreatorPlaylist.setForeground(Color.WHITE);
    }//GEN-LAST:event_CloseCreatorPlaylistMouseExited

    private void NewPlaylistManagerMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_NewPlaylistManagerMouseClicked
        CreateNewPlaylist.setVisible(true);
        ViewPlaylists.setVisible(false);
    }//GEN-LAST:event_NewPlaylistManagerMouseClicked

    private void NewPlaylistManagerMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_NewPlaylistManagerMouseEntered
        NewPlaylistManager.setForeground(Color.WHITE);
    }//GEN-LAST:event_NewPlaylistManagerMouseEntered

    private void NewPlaylistManagerMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_NewPlaylistManagerMouseExited
        NewPlaylistManager.setForeground(Color.GRAY);
    }//GEN-LAST:event_NewPlaylistManagerMouseExited

    private void SavePlaylistActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SavePlaylistActionPerformed
    String nombre = NamePlaylist.getText().trim();
    if (nombre.isEmpty()) {
        nombre = "Nueva Playlist";
    }

    String creadorAlias = usuarioActual.getNombre();
    UserCreator.setText(creadorAlias);

    // Crear la nueva playlist con la carátula y lista de canciones
    Playlist nueva = new Playlist(nombre, creadorAlias, caratulaRuta, canciones);

    // Guardar la playlist y actualizar la lista de playlists
    if (nueva.guardarPlaylist()) {
        JOptionPane.showMessageDialog(this, "Playlist guardada exitosamente.");
        cargarPlaylists(modeloPlaylists); // ← Recarga la lista sin duplicar
    } else {
        JOptionPane.showMessageDialog(this, "Error al guardar la playlist.");
    }
    }//GEN-LAST:event_SavePlaylistActionPerformed

    private void CodeFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CodeFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_CodeFieldActionPerformed

    private void GoogleBt1MouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_GoogleBt1MouseEntered
        GoogleBt1.setBorderColor(Color.WHITE);
    }//GEN-LAST:event_GoogleBt1MouseEntered

    private void GoogleBt1MouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_GoogleBt1MouseExited
        GoogleBt1.setBorderColor(Color.LIGHT_GRAY);
    }//GEN-LAST:event_GoogleBt1MouseExited

    private void AddSongsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_AddSongsActionPerformed
// Cargar playlist actual
    File archivo = new File("playlists/" + TittlePlaylist.getText() + ".playlist");
    if (!archivo.exists()) {
        JOptionPane.showMessageDialog(this, "No se encontró la playlist.");
        return;
    }
    Playlist playlist = Playlist.cargarDesdeArchivo(archivo);

    // Obtener todas las canciones del MusicPlayer
    MusicPlayer player = new MusicPlayer(); // o usa el que ya tienes instanciado
    List<Song> disponibles = new ArrayList<>();
    for (Song song : player.getPlaylist()) {
        if (!playlist.getCanciones().contains(song.getFile().getAbsolutePath())) {
            disponibles.add(song);
        }
    }

    // Crear lista visual de canciones
    DefaultListModel<Song> model = new DefaultListModel<>();
    for (Song s : disponibles) model.addElement(s);
    JList<Song> list = new JList<>(model);
    list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

    // Mostrar metadatos
    list.setCellRenderer((JList<? extends Song> l, Song value, int i, boolean sel, boolean foc) -> {
        JLabel label = new JLabel(value.getTitle() + " - " + value.getArtist());
        label.setOpaque(true);
        label.setBackground(sel ? new Color(30, 30, 30) : Color.DARK_GRAY);
        label.setForeground(Color.WHITE);
        label.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        return label;
    });

    // Agregar al hacer clic
    list.addMouseListener(new java.awt.event.MouseAdapter() {
        public void mouseClicked(java.awt.event.MouseEvent evt) {
            if (evt.getClickCount() == 1) {
                Song seleccionada = list.getSelectedValue();
                if (seleccionada != null) {
                    playlist.agregarCancion(seleccionada.getFile().getAbsolutePath());
                    playlist.guardarPlaylist();
                    ((DefaultListModel<Song>) list.getModel()).removeElement(seleccionada);
                }
            }
        }
    });

    // Mostrar en JScrollPane dentro de JOptionPane personalizado
    JScrollPane scrollPane = new JScrollPane(list);
    scrollPane.setPreferredSize(new java.awt.Dimension(400, 300));

    JOptionPane.showMessageDialog(this, scrollPane, "Agregar canciones a la playlist", JOptionPane.PLAIN_MESSAGE);
    }//GEN-LAST:event_AddSongsActionPerformed

    private void AddCoverActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_AddCoverActionPerformed
    JFileChooser fileChooser = new JFileChooser();
    fileChooser.setDialogTitle("Selecciona una imagen de carátula");
    fileChooser.setFileFilter(new FileNameExtensionFilter("Imágenes", "jpg", "jpeg", "png"));

    int result = fileChooser.showOpenDialog(this);
    if (result == JFileChooser.APPROVE_OPTION) {
        File selectedFile = fileChooser.getSelectedFile();
        String rutaCaratula = selectedFile.getAbsolutePath();

        // Cargar la playlist actual
        String nombrePlaylist = TittlePlaylist.getText();
        File archivo = new File("playlists/" + nombrePlaylist + ".playlist");
        if (!archivo.exists()) {
            JOptionPane.showMessageDialog(this, "Archivo de playlist no encontrado.");
            return;
        }

        Playlist playlist = Playlist.cargarDesdeArchivo(archivo);
        playlist.setCaratulaRuta(rutaCaratula);
        playlist.guardarPlaylist();

        // Mostrar carátula en UI
        ImageIcon icon = new ImageIcon(rutaCaratula);
        Image img = icon.getImage().getScaledInstance(120, 120, Image.SCALE_SMOOTH);
        PlaylistCover.setIcon(new ImageIcon(img));

        // Refrescar lista de playlists (si es necesario)
        initListaPlaylists();
    }
    }//GEN-LAST:event_AddCoverActionPerformed

    private void HomeIconMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_HomeIconMouseClicked
        PlaylistSongs.setVisible(false);
        AllSongs.setVisible(true);
    
        // Restaurar la lista principal con la copia original
        DefaultListModel<String> modeloOriginal = new DefaultListModel<>();
        for (String song : cancionesBackup) {
            modeloOriginal.addElement(song);
        }
    
        SongsList.setModel(modeloOriginal);
        SongsList.repaint(); // Asegurar la actualización
    }//GEN-LAST:event_HomeIconMouseClicked

    private void AddImgPlaylistActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_AddImgPlaylistActionPerformed
    JFileChooser fileChooser = new JFileChooser();
    fileChooser.setDialogTitle("Selecciona una imagen de carátula");
    fileChooser.setFileFilter(new FileNameExtensionFilter("Imágenes", "jpg", "jpeg", "png"));

    int result = fileChooser.showOpenDialog(this);
    if (result == JFileChooser.APPROVE_OPTION) {
        File selectedFile = fileChooser.getSelectedFile();
        caratulaRuta = selectedFile.getAbsolutePath(); // Guardamos la ruta de la imagen

        // Mostrar carátula en UI
        ImageIcon icon = new ImageIcon(caratulaRuta);
        Image img = icon.getImage().getScaledInstance(220, 220, Image.SCALE_SMOOTH);
        PlaylistImg.setIcon(new ImageIcon(img));
    }
    }//GEN-LAST:event_AddImgPlaylistActionPerformed

    private void SearchIconMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_SearchIconMouseClicked
        filtrarCanciones();
    }//GEN-LAST:event_SearchIconMouseClicked

    private void HeartStateMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_HeartStateMouseClicked
    isLike = !isLike; // Alternar estado

    if (isLike) {
        HeartState.setForeground(Color.GREEN); // Mostrar que la canción es favorita
        HeartState1.setForeground(Color.GREEN);
    } else {
        HeartState.setForeground(Color.GRAY); // Quitar favorito
        HeartState1.setForeground(Color.GRAY);
    }

    toggleLikeStatus(playlistSongs.get(currentPlaylistIndex));
    }//GEN-LAST:event_HeartStateMouseClicked

    private void HeartState1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_HeartState1MouseClicked
    isLike = !isLike; // Alternar estado

    if (isLike) {
        HeartState1.setForeground(Color.GREEN);
        HeartState.setForeground(Color.GREEN);// Mostrar que la canción es favorita
    } else {
        HeartState1.setForeground(Color.GRAY);
        HeartState.setForeground(Color.GRAY);// Quitar favorito
    }

    toggleLikeStatus(playlistSongs.get(currentPlaylistIndex));
    }//GEN-LAST:event_HeartState1MouseClicked

    private void AddtoQueePlaylistMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_AddtoQueePlaylistMouseEntered
        AddtoQueePlaylist.setForeground(Color.CYAN);
    }//GEN-LAST:event_AddtoQueePlaylistMouseEntered

    private void AddtoQueePlaylistMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_AddtoQueePlaylistMouseExited
        AddtoQueePlaylist.setForeground(Color.GREEN);
    }//GEN-LAST:event_AddtoQueePlaylistMouseExited

    private void AddtoQueePlaylistMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_AddtoQueePlaylistMouseClicked
        if (cancionesReal.isEmpty()) {
        System.out.println("⚠ No hay canciones en la playlist seleccionada.");
        return;
    }

    // Agregar todas las canciones de la playlist al inicio de `playlistSongs`
    for (int i = cancionesReal.size() - 1; i >= 0; i--) { // Recorrer desde la última para mantener el orden
        addSongToQueue(cancionesReal.get(i));
    }

    System.out.println("🎵 Se agregaron " + cancionesReal.size() + " canciones al inicio de la cola.");
    updatePlaylistModel(); // Refrescar la UI
    }//GEN-LAST:event_AddtoQueePlaylistMouseClicked

    private void GoogleBtMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_GoogleBtMouseClicked
    // Simular que el servicio no está disponible
    boolean servicioDisponible = false; // Ajusta según sea necesario

    if (!servicioDisponible) {
        JOptionPane.showMessageDialog(
            this, 
            "Error 404 - Servicio no disponible.\nNo se pudo cargar la página.", 
            "Google - Error de conexión", 
            JOptionPane.ERROR_MESSAGE
        );
    }
    }//GEN-LAST:event_GoogleBtMouseClicked

    private void GoogleBt1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_GoogleBt1MouseClicked
    // Simular que el servicio no está disponible
    boolean servicioDisponible = false; // Ajusta según sea necesario

    if (!servicioDisponible) {
        JOptionPane.showMessageDialog(
            this, 
            "Error 404 - Servicio no disponible.\nNo se pudo cargar la página.", 
            "Google - Error de conexión", 
            JOptionPane.ERROR_MESSAGE
        );
    }
    }//GEN-LAST:event_GoogleBt1MouseClicked

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(MainPage.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> {
            new MainPage().setVisible(true);
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton AddCover;
    private javax.swing.JButton AddImgPlaylist;
    private javax.swing.JButton AddPhoto;
    private javax.swing.JButton AddSongs;
    private javax.swing.JLabel AddtoQueePlaylist;
    private javax.swing.JLabel AlbumSong;
    private javax.swing.JPanel AllSongs;
    private javax.swing.JLabel ArtistSong;
    private javax.swing.JLabel ArtistSong2;
    private javax.swing.JLabel BackBt;
    private javax.swing.JSlider BarRepro;
    private javax.swing.JLabel CloseColaR;
    private javax.swing.JLabel CloseCreatorPlaylist;
    private javax.swing.JLabel CloseEditU;
    private javax.swing.JTextField CodeField;
    private javax.swing.JLabel CodeL;
    private javax.swing.JPanel ColaRepro;
    private javax.swing.JList<String> ColaReproduccion;
    private javax.swing.JPanel CreateNewPlaylist;
    private javax.swing.JLabel CreatorPlaylist;
    private javax.swing.JLabel DateSong;
    private javax.swing.JPanel EditUser;
    private javax.swing.JLabel ElapsedTime;
    private javax.swing.JButton EnterSButon;
    private javax.swing.JLabel ErrorLabel;
    private javax.swing.JLabel FriendsImg;
    private Buttons.RoundedPanel GoogleBt;
    private Buttons.RoundedPanel GoogleBt1;
    private javax.swing.JLabel HeartState;
    private javax.swing.JLabel HeartState1;
    private javax.swing.JLabel HomeIcon;
    private Buttons.RoundedPanel HomeIconPanel;
    private javax.swing.JPanel HomePanel;
    private javax.swing.JLabel ImgTaskBar;
    private javax.swing.JLabel LabelCode;
    private javax.swing.JLabel LabelLogo;
    private javax.swing.JLabel LabelPassword;
    private javax.swing.JLabel LabelTitle;
    private javax.swing.JPanel LoginPanel;
    private javax.swing.JPanel MinBtL;
    private javax.swing.JLabel MinLabel;
    private javax.swing.JLabel MinLabel1;
    private javax.swing.JLabel MinLabel2;
    private javax.swing.JPanel MinPanel;
    private javax.swing.JPanel MinPanel1;
    private javax.swing.JTextField NamePlaylist;
    private javax.swing.JLabel NameSong;
    private javax.swing.JLabel NameSong2;
    private javax.swing.JTextField NewCodeField;
    private javax.swing.JPasswordField NewPasswordField;
    private javax.swing.JLabel NewPlaylistManager;
    private javax.swing.JLabel NextBt;
    private javax.swing.JLabel OpenColaR;
    private javax.swing.JPasswordField PasswordField;
    private javax.swing.JLabel PlayBt;
    private javax.swing.JLabel PlaylistCover;
    private javax.swing.JLabel PlaylistImg;
    private javax.swing.JPanel PlaylistSongs;
    private javax.swing.JPanel PlaylistsPanel;
    private javax.swing.JLabel RandomBt;
    private javax.swing.JLabel ReadCL;
    private javax.swing.JButton RegisterBt;
    private javax.swing.JLabel RegisterErrorLabel;
    private javax.swing.JButton RegisterFBt;
    private javax.swing.JLabel RegisterIconImg;
    private javax.swing.JPanel RegisterPanel;
    private javax.swing.JLabel RepCLabel;
    private javax.swing.JLabel RepeatBt;
    private javax.swing.JPasswordField RepeatPasswordField;
    private javax.swing.JPanel ReproBar;
    private javax.swing.JPanel ReproductorPanel;
    private javax.swing.JButton SavePlaylist;
    private javax.swing.JScrollPane ScrollPlaylist;
    private javax.swing.JTextField SearchField;
    private javax.swing.JLabel SearchIcon;
    private Buttons.RoundedPanel SearchPanel;
    private javax.swing.JLabel SongImg;
    private javax.swing.JLabel SongImg2;
    private javax.swing.JPanel SongInfo;
    private javax.swing.JList<String> SongsList;
    private javax.swing.JPanel SongsPanel;
    private javax.swing.JList<String> SongsPlaylist;
    private javax.swing.JLabel SoundBt;
    private javax.swing.JPanel TaskBar;
    private javax.swing.JPanel TaskBar1;
    private javax.swing.JPanel TaskBar2;
    private javax.swing.JLabel TittlePlaylist;
    private javax.swing.JLabel TotalTime;
    private javax.swing.JLabel UserCreator;
    private javax.swing.JLabel UserImg;
    private javax.swing.JLabel Usuarios;
    private javax.swing.JPanel ViewPlaylists;
    private javax.swing.JSlider VolumeSlider;
    private javax.swing.JPanel XBtL;
    private javax.swing.JLabel Xlabel;
    private javax.swing.JLabel Xlabel1;
    private javax.swing.JLabel Xlabel2;
    private javax.swing.JPanel Xpanel;
    private javax.swing.JPanel Xpanel1;
    private javax.swing.JButton btnGuardarPerfil;
    private javax.swing.JPanel fondoLogin;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JLabel lblPasswordError;
    private javax.swing.JLabel lblUserCode;
    private javax.swing.JLabel lblUserImg;
    private javax.swing.JLabel registrar;
    private Buttons.RoundedPanel roundedPanel1;
    private Buttons.RoundedPanel roundedPanel2;
    private Buttons.RoundedPanel roundedPanel3;
    private Buttons.RoundedPanel roundedPanel4;
    private javax.swing.JTextField txtAliasPerfil;
    private javax.swing.JPasswordField txtNewPassword;
    private javax.swing.JPasswordField txtPasswordConfirmar;
    // End of variables declaration//GEN-END:variables
}
