import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Stack;

public class MazeApp extends JFrame {
    private MazeGraphModel mazeModel;
    private MazePanel mazePanel;
    private boolean isAnimating = false;
    private Image fullBackgroundImage; // Variable untuk gambar background

    public MazeApp() {
        setTitle("Maze Solver - Forest Adventure");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // 1. LOAD IMAGE BACKGROUND
        try {
            fullBackgroundImage = ImageIO.read(new File("C:/Backgroundmaze.jpg"));
        } catch (IOException e) {
            System.err.println("Gambar background tidak ditemukan, menggunakan warna fallback.");
        }

        mazeModel = new MazeGraphModel(30, 20);
        mazePanel = new MazePanel(mazeModel);

        // 2. WRAPPER DENGAN GAMBAR
        JPanel centerWrapper = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                // Jika gambar berhasil dimuat, gambar memenuhi layar
                if (fullBackgroundImage != null) {
                    g.drawImage(fullBackgroundImage, 0, 0, getWidth(), getHeight(), this);
                } else {
                    // Fallback jika gambar error: Hijau Tua Hutan
                    g.setColor(new Color(34, 139, 34));
                    g.fillRect(0, 0, getWidth(), getHeight());
                }
            }
        };

        centerWrapper.add(mazePanel);

        JScrollPane scrollPane = new JScrollPane(centerWrapper);
        scrollPane.setBorder(null);

        JPanel controlPanel = new JPanel();
        controlPanel.setBackground(new Color(60, 60, 60)); // Panel bawah tetap gelap agar kontras

        JButton btnGenerate = createButton("Generate Map");
        JButton btnBFS = createButton("Solve BFS");
        JButton btnDFS = createButton("Solve DFS");

        controlPanel.add(btnGenerate);
        controlPanel.add(btnBFS);
        controlPanel.add(btnDFS);

        btnGenerate.addActionListener(e -> {
            if (isAnimating) return;
            mazeModel.generateMaze();
            mazePanel.setMazeModel(mazeModel);
        });

        btnBFS.addActionListener(e -> { if (!isAnimating) solveMaze("BFS"); });
        btnDFS.addActionListener(e -> { if (!isAnimating) solveMaze("DFS"); });

        add(scrollPane, BorderLayout.CENTER);
        add(controlPanel, BorderLayout.SOUTH);

        setSize(1000, 800);
        setLocationRelativeTo(null);
    }

    private JButton createButton(String text) {
        JButton btn = new JButton(text);
        btn.setFocusPainted(false);
        btn.setBackground(new Color(245, 245, 220)); // Warna krem/kayu muda
        btn.setFont(new Font("SansSerif", Font.BOLD, 12));
        btn.setForeground(new Color(101, 67, 33)); // Teks coklat
        return btn;
    }

    private void solveMaze(String algorithm) {
        isAnimating = true;
        mazeModel.resetVisited();
        mazePanel.clearPath();

        new Thread(() -> {
            Node start = mazeModel.getGrid()[0][0];
            Node end = mazeModel.getGrid()[mazeModel.getCols() - 1][mazeModel.getRows() - 1];
            boolean found = false;

            if (algorithm.equals("BFS")) {
                found = runBFS(start, end);
            } else {
                found = runDFS(start, end);
            }

            if (found) {
                java.util.List<Node> path = new ArrayList<>();
                Node current = end;
                while (current != null) {
                    path.add(current);
                    current = current.parent;
                }
                mazePanel.setFinalPath(path);
            } else {
                JOptionPane.showMessageDialog(this, "Path not found!");
            }
            isAnimating = false;
        }).start();
    }

    private boolean runBFS(Node start, Node end) {
        Queue<Node> queue = new LinkedList<>();
        queue.add(start);
        start.visited = true;

        while (!queue.isEmpty()) {
            Node current = queue.poll();
            mazePanel.addExploredNode(current);
            sleepDelay(5);

            if (current == end) return true;

            for (Node neighbor : current.neighbors) {
                if (!neighbor.visited) {
                    neighbor.visited = true;
                    neighbor.parent = current;
                    queue.add(neighbor);
                }
            }
        }
        return false;
    }

    private boolean runDFS(Node start, Node end) {
        Stack<Node> stack = new Stack<>();
        stack.push(start);
        start.visited = true;

        while (!stack.isEmpty()) {
            Node current = stack.pop();
            mazePanel.addExploredNode(current);
            sleepDelay(5);

            if (current == end) return true;

            for (Node neighbor : current.neighbors) {
                if (!neighbor.visited) {
                    neighbor.visited = true;
                    neighbor.parent = current;
                    stack.push(neighbor);
                }
            }
        }
        return false;
    }

    private void sleepDelay(int millis) {
        try { Thread.sleep(millis); } catch (InterruptedException e) {}
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MazeApp().setVisible(true));
    }
}