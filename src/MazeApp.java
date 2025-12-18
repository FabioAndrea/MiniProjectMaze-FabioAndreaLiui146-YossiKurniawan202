import javax.swing.*;
import java.awt.*;

public class MazeApp extends JFrame {
    private MazeGraphModel mazeModel;
    private MazePanel mazePanel;

    public MazeApp() {
        setTitle("Maze Generator - Step 1");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Inisialisasi Model (Ukuran 30x20)
        mazeModel = new MazeGraphModel(30, 20);

        // Inisialisasi Panel Visualisasi
        mazePanel = new MazePanel(mazeModel);

        // Tombol Generate
        JButton btnGenerate = new JButton("Generate New Maze");
        btnGenerate.addActionListener(e -> {
            mazeModel.generateMaze();
            mazePanel.repaint();
        });

        // Layouting
        JPanel controlPanel = new JPanel();
        controlPanel.add(btnGenerate);

        add(new JScrollPane(mazePanel), BorderLayout.CENTER);
        add(controlPanel, BorderLayout.SOUTH);

        pack(); // Sesuaikan ukuran window dengan konten
        setLocationRelativeTo(null); // Tengah layar
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new MazeApp().setVisible(true);
        });
    }
}