package prueba.pkg2.lab.ii;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.util.ArrayList;
import java.util.List;
import javax.sound.sampled.*;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import javazoom.jl.decoder.Bitstream;
import javazoom.jl.decoder.BitstreamException;
import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.Player;

class Cancion {

    private String nombre;
    private String artista;
    private long duracion;
    private ImageIcon imagen;
    private String tipo;
    private String rutaArchivo;

    public Cancion(String nombre, String artista, long duracion, ImageIcon imagen, String tipo, String rutaArchivo) {
        this.nombre = nombre;
        this.artista = artista;
        this.duracion = duracion;
        this.imagen = imagen;
        this.tipo = tipo;
        this.rutaArchivo = rutaArchivo;
    }

    @Override
    public String toString() {
        return nombre + " - " + artista;
    }

    public ImageIcon getImagen() {
        return imagen;
    }

    public String getRutaArchivo() {
        return rutaArchivo;
    }

    public String getNombre() {
        return nombre;
    }

    public String getArtista() {
        return artista;
    }
}

class Nodo {

    Cancion cancion;
    Nodo siguiente;

    public Nodo(Cancion cancion) {
        this.cancion = cancion;
        this.siguiente = null;
    }
}

class ListaEnlazada {

    private Nodo cabeza;

    public ListaEnlazada() {
        this.cabeza = null;
    }

    public void agregarCancion(Cancion cancion) {
        Nodo nuevoNodo = new Nodo(cancion);
        if (cabeza == null) {
            cabeza = nuevoNodo;
        } else {
            Nodo temp = cabeza;
            while (temp.siguiente != null) {
                temp = temp.siguiente;
            }
            temp.siguiente = nuevoNodo;
        }
    }

    public Cancion obtenerCancionSeleccionada() {
        List<Cancion> canciones = obtenerListaDeCanciones();

        if (canciones.isEmpty()) {
            JOptionPane.showMessageDialog(null, "No hay canciones disponibles.", "Lista de canciones vacía", JOptionPane.WARNING_MESSAGE);
            return null;
        }

        return (Cancion) JOptionPane.showInputDialog(
                null,
                "Seleccione una canción:",
                "Seleccionar Canción",
                JOptionPane.PLAIN_MESSAGE,
                null,
                canciones.toArray(),
                canciones.get(0)
        );
    }

    private List<Cancion> obtenerListaDeCanciones() {
        List<Cancion> canciones = new ArrayList<>();
        Nodo temp = cabeza;
        while (temp != null) {
            canciones.add(temp.cancion);
            temp = temp.siguiente;
        }
        return canciones;
    }

    public boolean isEmpty() {
        return cabeza == null;
    }
}

class SongPanel extends JPanel {

    private JLabel nameLabel;
    private JLabel artistLabel;
    private JLabel imageLabel;

    public SongPanel(Cancion cancion) {
        setLayout(new BorderLayout());

        // Formatear y establecer las etiquetas con la fuente y estilo deseado
        nameLabel = new JLabel("Canción: " + cancion.getNombre());
        nameLabel.setFont(new Font("Arial", Font.PLAIN, 18));

        artistLabel = new JLabel("Artista: " + cancion.getArtista());
        artistLabel.setFont(new Font("Arial", Font.PLAIN, 18));

        imageLabel = new JLabel(cancion.getImagen());

        // Crear un panel para la información de la canción
        JPanel infoPanel = new JPanel(new BorderLayout());
        infoPanel.add(nameLabel, BorderLayout.NORTH);  // Nombre de la canción arriba
        infoPanel.add(artistLabel, BorderLayout.CENTER);  // Artista debajo del nombre de la canción

        // Agregar la imagen y la información al panel principal
        add(infoPanel, BorderLayout.NORTH);
        add(imageLabel, BorderLayout.CENTER); // Imagen en el centro
    }
}

class AudioPlayer {

    private Clip clip;
    private Player mp3Player; // Para manejar MP3
    private Thread mp3Thread; // Hilo para reproducir MP3

    public void play(File audioFile) {
        String extension = getFileExtension(audioFile);

        if (extension.equalsIgnoreCase("wav")) {
            playWav(audioFile);
        } else if (extension.equalsIgnoreCase("mp3")) {
            playMp3(audioFile);
        } else {
            showErrorDialog("Formato de archivo no compatible.", "Error de reproducción");
        }
    }

    private void playWav(File audioFile) {
        try {
            AudioInputStream audioInput = AudioSystem.getAudioInputStream(audioFile);
            clip = AudioSystem.getClip();
            clip.open(audioInput);
            clip.start();
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            showErrorDialog("Error al reproducir el archivo de audio.", "Error de reproducción");
            e.printStackTrace();
        }
    }

    private void playMp3(File audioFile) {
        try {
            FileInputStream fis = new FileInputStream(audioFile);
            BufferedInputStream bis = new BufferedInputStream(fis);
            mp3Player = new Player(bis);

            // Crear un hilo para reproducir el MP3, evitando que bloquee la interfaz gráfica
            mp3Thread = new Thread(() -> {
                try {
                    mp3Player.play();
                } catch (JavaLayerException e) {
                    showErrorDialog("Error al reproducir el archivo MP3.", "Error de reproducción");
                    e.printStackTrace();
                }
            });

            mp3Thread.start();
        } catch (JavaLayerException | IOException e) {
            showErrorDialog("Error al reproducir el archivo MP3.", "Error de reproducción");
            e.printStackTrace();
        }
    }

    public void stop() {
        if (clip != null && clip.isRunning()) {
            clip.stop();
        }

        if (mp3Player != null) {
            mp3Player.close();
        }

        if (mp3Thread != null && mp3Thread.isAlive()) {
            mp3Thread.interrupt();
        }
    }

    public void pause() {
        if (clip != null && clip.isRunning()) {
            clip.stop();
            showInfoDialog("Reproducción pausada.", "Pausa");
        } else if (mp3Player != null && !mp3Player.isComplete()) {
            // No se puede pausar MP3 con JLayer fácilmente, así que simplemente lo detendremos
            stop();
            showWarningDialog("La función de pausa no está soportada para archivos MP3.", "Error de pausa");
        } else {
            showWarningDialog("No hay ninguna canción reproduciéndose.", "Error de pausa");
        }
    }

    public void cleanUp() {
        if (clip != null) {
            clip.close();
        }

        if (mp3Player != null) {
            mp3Player.close();
        }
    }

    private String getFileExtension(File file) {
        String fileName = file.getName();
        int dotIndex = fileName.lastIndexOf('.');
        return (dotIndex == -1) ? "" : fileName.substring(dotIndex + 1).toLowerCase();
    }

    public long getSongLength() {
        // Verifica si el clip es nulo antes de acceder a su longitud
        if (clip == null) {
            System.err.println("Error: El clip no ha sido inicializado correctamente.");
            return 0; // Devuelve 0 o lanza una excepción según necesites manejarlo
        }
        return clip.getMicrosecondLength();
    }

    public void loadClip(File audioFile) {
        try {
            if (clip != null && clip.isOpen()) {
                clip.close();
            }

            AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioFile);
            clip = AudioSystem.getClip();
            clip.open(audioStream);
        } catch (Exception e) {
            System.err.println("Error al cargar el archivo de audio: " + e.getMessage());
            clip = null; 
        }
    }

    public long getCurrentPosition() {
        return clip.getMicrosecondPosition();
    }

    private void showErrorDialog(String message, String title) {
        JOptionPane.showMessageDialog(null, message, title, JOptionPane.ERROR_MESSAGE);
    }

    private void showInfoDialog(String message, String title) {
        JOptionPane.showMessageDialog(null, message, title, JOptionPane.INFORMATION_MESSAGE);
    }

    private void showWarningDialog(String message, String title) {
        JOptionPane.showMessageDialog(null, message, title, JOptionPane.WARNING_MESSAGE);
    }
}

public class ReproductorMusical extends JFrame {

    private static final String AUDIO_FORMAT = "WAV";
    private static final String WARNING_MESSAGE_TITLE = "Advertencia";
    private static final String ERROR_MESSAGE_TITLE = "Error";

    private JLabel songNameLabel;
    private JLabel artistNameLabel;
    private JPanel songListPanel;
    private JLabel imageLabel;
    private JPanel buttonPanel;
    private AudioPlayer audioPlayer;
    private ListaEnlazada listaCanciones;
    private JButton playButton, stopButton, pauseButton, addButton, selectButton;
    private JPanel imagePanel;
    private File currentSongFile;
    private JSlider progressBar;  // Slider para mostrar el progreso de la canción
    private Timer timer;  // Timer para actualizar la posición del slider

    public ReproductorMusical() {
        listaCanciones = new ListaEnlazada();
        audioPlayer = new AudioPlayer();

        // Inicialización de componentes
        playButton = new JButton("Play");
        stopButton = new JButton("Stop");
        pauseButton = new JButton("Pause");
        addButton = new JButton("Add");
        selectButton = new JButton("Select");

        playButton.addActionListener(e -> playCurrentSong());
        stopButton.addActionListener(e -> stopCurrentSong());
        pauseButton.addActionListener(e -> pauseCurrentSong());
        addButton.addActionListener(e -> addSong());
        selectButton.addActionListener(e -> selectSong());

        songNameLabel = new JLabel("Nombre de la Canción");
        artistNameLabel = new JLabel("Nombre del Artista");
        imageLabel = new JLabel();

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new GridLayout(2, 1));
        infoPanel.add(songNameLabel);
        infoPanel.add(artistNameLabel);
        add(infoPanel, BorderLayout.NORTH);

        progressBar = new JSlider(0, 1000, 0);
        progressBar.setEnabled(false);

        buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.add(playButton);
        buttonPanel.add(stopButton);
        buttonPanel.add(pauseButton);
        buttonPanel.add(addButton);
        buttonPanel.add(selectButton);
        buttonPanel.add(progressBar); // Añadir la barra de progreso después de inicializar el panel
        add(buttonPanel, BorderLayout.SOUTH);

        songListPanel = new JPanel();
        songListPanel.setLayout(new BoxLayout(songListPanel, BoxLayout.Y_AXIS));
        JScrollPane scrollPane = new JScrollPane(songListPanel);
        add(scrollPane, BorderLayout.CENTER);

        timer = new Timer(500, e -> updateProgressBar());

        setTitle("Reproductor Musical");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void playCurrentSong() {
        if (currentSongFile != null) {
            audioPlayer.play(currentSongFile);
            progressBar.setMaximum((int) (audioPlayer.getSongLength() / 1000000)); // Duración en segundos
            progressBar.setValue(0);  // Iniciar desde 0
            progressBar.setEnabled(true);
            timer.start();  // Iniciar el timer para actualizar la barra de progreso
        } else {
            showWarningDialog("No se ha seleccionado ninguna canción.", WARNING_MESSAGE_TITLE);
        }
    }

    private void stopCurrentSong() {
        audioPlayer.stop();
        progressBar.setValue(0); // Reiniciar la barra de progreso
        timer.stop();  // Detener el timer
    }

    private void pauseCurrentSong() {
        audioPlayer.pause();
        timer.stop(); // Pausar la actualización de la barra de progreso
    }

    private void updateProgressBar() {
        if (audioPlayer.getSongLength() > 0) {
            long currentPosition = audioPlayer.getCurrentPosition();
            int currentSecond = (int) (currentPosition / 1000000); // Convertir microsegundos a segundos
            progressBar.setValue(currentSecond); // Actualizar el valor del slider
        }
    }

    private void cleanUpResources() {
        audioPlayer.cleanUp();
    }

    private void addSong() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("Archivos de Audio (WAV, MP3)", "wav", "mp3"));
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedAudioFile = fileChooser.getSelectedFile();
            String rutaArchivo = selectedAudioFile.getAbsolutePath();

            String artista = JOptionPane.showInputDialog(this, "Ingrese el nombre del artista:");
            if (artista == null || artista.trim().isEmpty()) {
                showWarningDialog("El nombre del artista no puede estar vacío.", WARNING_MESSAGE_TITLE);
                return;
            }

            long duracion = getAudioDuration(selectedAudioFile);

            JOptionPane.showMessageDialog(this, "Ahora seleccione una imagen para la canción.");

            JFileChooser imageFileChooser = new JFileChooser();
            imageFileChooser.setFileFilter(new FileNameExtensionFilter("Archivos de Imagen", "jpg", "jpeg", "png"));
            int imageResult = imageFileChooser.showOpenDialog(this);
            if (imageResult == JFileChooser.APPROVE_OPTION) {
                File selectedImageFile = imageFileChooser.getSelectedFile();

                System.out.println(String.format("Nombre del archivo: %s\nRuta del archivo: %s\nTamaño del archivo: %d bytes\nFormato de archivo: %s",
                        selectedAudioFile.getName(),
                        selectedAudioFile.getAbsolutePath(),
                        selectedAudioFile.length(),
                        getFileExtension(selectedAudioFile)));

                try {
                    ImageIcon icon = getScaledImageIcon(selectedImageFile, 150, 150);
                    actualizarImagenCancion(icon);

                    Cancion nuevaCancion = new Cancion(selectedAudioFile.getName(), artista, duracion, icon, AUDIO_FORMAT, rutaArchivo);

                    listaCanciones.agregarCancion(nuevaCancion);
                    addSongPanel(nuevaCancion);

                    JOptionPane.showMessageDialog(this, "Canción agregada exitosamente.");
                } catch (IOException ex) {
                    ex.printStackTrace();
                    showErrorDialog("Error al cargar la imagen.", ERROR_MESSAGE_TITLE);
                }
            }
        }
    }

    private void selectSong() {
        Cancion cancionSeleccionada = listaCanciones.obtenerCancionSeleccionada();
        if (cancionSeleccionada != null) {
            currentSongFile = new File(cancionSeleccionada.getRutaArchivo());
            if (songNameLabel != null) {
                songNameLabel.setText(cancionSeleccionada.getNombre());
            } else {
                System.out.println("songNameLabel is null.");
            }
            if (artistNameLabel != null) {
                artistNameLabel.setText(cancionSeleccionada.getArtista());
            } else {
                System.out.println("artistNameLabel is null.");
            }
        }
    }

    private void addSongPanel(Cancion cancion) {
        SongPanel songPanel = new SongPanel(cancion);
        songPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) { // Double-click to select and play
                    currentSongFile = new File(cancion.getRutaArchivo());
                    actualizarImagenCancion(cancion.getImagen());
                    songNameLabel.setText(cancion.getNombre());
                    artistNameLabel.setText(cancion.getArtista());
                    playCurrentSong();
                }
            }
        });
        songListPanel.add(songPanel);
        songListPanel.revalidate();
        songListPanel.repaint();
    }

    private void actualizarImagenCancion(ImageIcon icon) {
        if (this.imageLabel != null) {
            this.imageLabel.setIcon(icon);
        } else {
            System.out.println("imageLabel es null, no se puede actualizar la imagen.");
        }
    }

    private String getFileExtension(File file) {
        String fileName = file.getName();
        int dotIndex = fileName.lastIndexOf('.');
        return (dotIndex == -1) ? "" : fileName.substring(dotIndex + 1).toLowerCase();
    }

    private ImageIcon getScaledImageIcon(File imageFile, int width, int height) throws IOException {
        BufferedImage originalImage = ImageIO.read(imageFile);
        int originalWidth = originalImage.getWidth();
        int originalHeight = originalImage.getHeight();
        double aspectRatio = (double) originalWidth / originalHeight;

        if (originalWidth > originalHeight) {
            height = (int) (width / aspectRatio);
        } else {
            width = (int) (height * aspectRatio);
        }

        Image scaledImage = originalImage.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        return new ImageIcon(scaledImage);
    }

    private long getAudioDuration(File audioFile) {
        String extension = getFileExtension(audioFile);

        if (extension.equalsIgnoreCase("wav")) {
            // Duración para archivos WAV
            try {
                AudioInputStream audioInput = AudioSystem.getAudioInputStream(audioFile);
                AudioFormat format = audioInput.getFormat();
                long frames = audioInput.getFrameLength();
                double durationInSeconds = (frames + 0.0) / format.getFrameRate();
                return (long) (durationInSeconds * 1000); // Duración en milisegundos
            } catch (UnsupportedAudioFileException | IOException e) {
                showErrorDialog("Error al obtener la duración del archivo WAV.", "Error de duración");
                e.printStackTrace();
            }
        } else if (extension.equalsIgnoreCase("mp3")) {
            // Duración estimada para archivos MP3
            return estimateMp3Duration(audioFile);
        }

        // Retorna 0 si no se puede calcular la duración
        return 0;
    }

// Método para estimar la duración de archivos MP3 (puedes ajustar esto según tus necesidades)
    private long estimateMp3Duration(File mp3File) {
        try (FileInputStream fis = new FileInputStream(mp3File); BufferedInputStream bis = new BufferedInputStream(fis)) {

            Bitstream bitstream = new Bitstream(bis);
            javazoom.jl.decoder.Header header = bitstream.readFrame();

            if (header != null) {
                int bitrate = header.bitrate();
                int sampleRate = header.frequency();

                if (bitrate > 0) {
                    long fileSize = mp3File.length();
                    // Calcular la duración: tamaño del archivo / (bitrate en bits/seg)
                    long duration = (fileSize * 8) / bitrate; // Duración en segundos
                    return duration * 1000; // Convertir a milisegundos
                } else {
                    showErrorDialog("Bitrate no válido al estimar la duración del archivo MP3.", "Error de duración");
                }
            } else {
                showErrorDialog("No se pudo leer el encabezado del archivo MP3.", "Error de duración");
            }
        } catch (IOException | BitstreamException e) {
            showErrorDialog("Error al estimar la duración del archivo MP3.", "Error de duración");
            e.printStackTrace();
        }
        return 0;
    }

    private void showWarningDialog(String message, String title) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.WARNING_MESSAGE);
    }

    private void showErrorDialog(String message, String title) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.ERROR_MESSAGE);
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(ReproductorMusical::new);
    }

}
