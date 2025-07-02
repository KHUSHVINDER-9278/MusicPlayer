import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class MusicPlayer extends JFrame {
    private Clip clip;
    private AudioInputStream audioStream;
    private File audioFile;
    private long clipTimePosition = 0;

    private JButton playButton, pauseButton, stopButton, favButton;
    private JButton removeFromAllButton, removeFromFavButton;
    private JLabel statusLabel;

    private DefaultListModel<String> favListModel;
    private DefaultListModel<String> allSongsModel;
    private JList<String> favList, allSongsList;

    private List<File> favFiles = new ArrayList<>();
    private List<File> allAudioFiles = new ArrayList<>();

    private static final String FAVORITES_FILE = "favorites.txt";

    public MusicPlayer() {
        setTitle("Java Music Player with Favorites and Persistence");
        setSize(800, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // ==== Control Buttons ====
        JPanel controlPanel = new JPanel();
        playButton = new JButton("Play");
        pauseButton = new JButton("Pause");
        stopButton = new JButton("Stop");
        favButton = new JButton("Add to Favorites");
        removeFromAllButton = new JButton("Remove from All");
        removeFromFavButton = new JButton("Remove from Favorites");

        controlPanel.add(playButton);
        controlPanel.add(pauseButton);
        controlPanel.add(stopButton);
        controlPanel.add(favButton);
        controlPanel.add(removeFromAllButton);
        controlPanel.add(removeFromFavButton);

        // ==== Status Label ====
        statusLabel = new JLabel("Scanning for audio files...", SwingConstants.CENTER);

        // ==== Lists ====
        favListModel = new DefaultListModel<>();
        allSongsModel = new DefaultListModel<>();
        favList = new JList<>(favListModel);
        allSongsList = new JList<>(allSongsModel);

        JScrollPane favScroll = new JScrollPane(favList);
        JScrollPane allScroll = new JScrollPane(allSongsList);

        favScroll.setBorder(BorderFactory.createTitledBorder("Favorites"));
        allScroll.setBorder(BorderFactory.createTitledBorder("All Songs"));

        favScroll.setPreferredSize(new Dimension(300, 300));
        allScroll.setPreferredSize(new Dimension(300, 300));

        JPanel listsPanel = new JPanel(new GridLayout(1, 2));
        listsPanel.add(allScroll);
        listsPanel.add(favScroll);

        // ==== Add to Frame ====
        add(statusLabel, BorderLayout.NORTH);
        add(controlPanel, BorderLayout.SOUTH);
        add(listsPanel, BorderLayout.CENTER);

        // ==== Event Listeners ====
        playButton.addActionListener(e -> playAudio());
        pauseButton.addActionListener(e -> pauseAudio());
        stopButton.addActionListener(e -> stopAudio());
        favButton.addActionListener(e -> addToFavorites());
        removeFromAllButton.addActionListener(e -> removeFromAllSongs());
        removeFromFavButton.addActionListener(e -> removeFromFavorites());

        allSongsList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    int index = allSongsList.locationToIndex(evt.getPoint());
                    if (index >= 0) {
                        audioFile = allAudioFiles.get(index);
                        loadAndPlayFile(audioFile);
                    }
                }
            }
        });

        favList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    int index = favList.locationToIndex(evt.getPoint());
                    if (index >= 0) {
                        audioFile = favFiles.get(index);
                        loadAndPlayFile(audioFile);
                    }
                }
            }
        });

        setVisible(true);

        // ==== Load Favorites and Scan Files ====
        loadFavoritesFromFile();
        new Thread(this::scanAudioFiles).start();
    }

    // ==== File Scan ====
    private void scanAudioFiles() {
        File userDir = new File(System.getProperty("user.home"));
        findAudioFiles(userDir);

        SwingUtilities.invokeLater(() -> {
            for (File file : allAudioFiles) {
                allSongsModel.addElement(file.getName());
            }
            statusLabel.setText("Found " + allAudioFiles.size() + " .wav files");
        });
    }

    private void findAudioFiles(File dir) {
        File[] files = dir.listFiles();
        if (files == null) return;

        for (File file : files) {
            if (file.isDirectory() && !file.isHidden()) {
                findAudioFiles(file); // Recurse
            } else if (file.getName().toLowerCase().endsWith(".wav")) {
                allAudioFiles.add(file);
            }
        }
    }

    // ==== Audio Playback ====
    private void loadClip(File file) throws Exception {
        if (clip != null && clip.isOpen()) clip.close();
        audioStream = AudioSystem.getAudioInputStream(file);
        clip = AudioSystem.getClip();
        clip.open(audioStream);
        clipTimePosition = 0;
    }

    private void loadAndPlayFile(File file) {
        try {
            audioFile = file;
            loadClip(audioFile);
            playAudio();
        } catch (Exception ex) {
            ex.printStackTrace();
            statusLabel.setText("Error playing: " + file.getName());
        }
    }

    private void playAudio() {
        if (clip != null) {
            clip.setMicrosecondPosition(clipTimePosition);
            clip.start();
            statusLabel.setText("Playing: " + audioFile.getName());
        }
    }

    private void pauseAudio() {
        if (clip != null && clip.isRunning()) {
            clipTimePosition = clip.getMicrosecondPosition();
            clip.stop();
            statusLabel.setText("Paused");
        }
    }

    private void stopAudio() {
        if (clip != null) {
            clip.stop();
            clip.setMicrosecondPosition(0);
            clipTimePosition = 0;
            statusLabel.setText("Stopped");
        }
    }

    // ==== Favorites ====
    private void addToFavorites() {
        if (audioFile != null && !favFiles.contains(audioFile)) {
            favFiles.add(audioFile);
            favListModel.addElement(audioFile.getName());
            statusLabel.setText("Added to favorites: " + audioFile.getName());
        } else {
            statusLabel.setText("Already in favorites or nothing selected");
        }
    }

    private void removeFromFavorites() {
        int index = favList.getSelectedIndex();
        if (index >= 0) {
            File removedFile = favFiles.remove(index);
            favListModel.remove(index);
            statusLabel.setText("Removed from favorites: " + removedFile.getName());
        } else {
            statusLabel.setText("Select a song to remove from favorites");
        }
    }

    private void removeFromAllSongs() {
        int index = allSongsList.getSelectedIndex();
        if (index >= 0) {
            File removedFile = allAudioFiles.remove(index);
            allSongsModel.remove(index);

            // Also remove from favorites if present
            int favIndex = favFiles.indexOf(removedFile);
            if (favIndex >= 0) {
                favFiles.remove(favIndex);
                favListModel.remove(favIndex);
            }

            statusLabel.setText("Removed from All Songs: " + removedFile.getName());
        } else {
            statusLabel.setText("Select a song to remove from All Songs");
        }
    }

    // ==== Persistence ====
    private void loadFavoritesFromFile() {
        File favFile = new File(FAVORITES_FILE);
        if (favFile.exists()) {
            try (Scanner scanner = new Scanner(favFile)) {
                while (scanner.hasNextLine()) {
                    String path = scanner.nextLine();
                    File file = new File(path);
                    if (file.exists() && file.getName().toLowerCase().endsWith(".wav")) {
                        favFiles.add(file);
                        favListModel.addElement(file.getName());
                    }
                }
                System.out.println("Favorites loaded.");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void saveFavoritesToFile() {
        try (PrintWriter writer = new PrintWriter(FAVORITES_FILE)) {
            for (File file : favFiles) {
                writer.println(file.getAbsolutePath());
            }
            System.out.println("Favorites saved.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ==== Main ====
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MusicPlayer app = new MusicPlayer();

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                app.saveFavoritesToFile();
            }));
        });
    }
}

