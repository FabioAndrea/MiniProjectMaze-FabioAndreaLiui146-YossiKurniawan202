import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class MazeApp extends JFrame {
    private MazeGraphModel mazeModel;
    private MazePanel mazePanel;
    private boolean isAnimating = false; // Mencegah klik tombol saat animasi berjalan

    public MazeApp() {
        setTitle("Maze Solver - Step 2 (BFS & DFS)");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        mazeModel = new MazeGraphModel(30, 20);
        mazePanel = new MazePanel(mazeModel);

        // Panel Tombol
        JPanel controlPanel = new JPanel();
        JButton btnGenerate = new JButton("Generate New");
        JButton btnBFS = new JButton("Solve BFS");
        JButton btnDFS = new JButton("Solve DFS");

        controlPanel.add(btnGenerate);
        controlPanel.add(btnBFS);
        controlPanel.add(btnDFS);

        // Event Listeners
        btnGenerate.addActionListener(e -> {
            if (isAnimating) return;
            mazeModel.generateMaze();
            mazePanel.setMazeModel(mazeModel);
        });

        btnBFS.addActionListener(e -> {
            if (isAnimating) return;
            solveMaze("BFS");
        });

        btnDFS.addActionListener(e -> {
            if (isAnimating) return;
            solveMaze("DFS");
        });

        add(new JScrollPane(mazePanel), BorderLayout.CENTER);
        add(controlPanel, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(null);
    }

    // Method untuk menjalankan solusi dalam Thread terpisah agar UI tidak macet
    private void solveMaze(String algorithm) {
        isAnimating = true;
        mazeModel.resetVisited(); // Bersihkan status visited
        mazePanel.clearPath();    // Bersihkan gambar lama

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
                // Backtracking untuk menemukan jalur final dari End ke Start
                List<Node> path = new ArrayList<>();
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

    // --- ALGORITMA BFS (Queue) ---
    private boolean runBFS(Node start, Node end) {
        Queue<Node> queue = new LinkedList<>();
        queue.add(start);
        start.visited = true;

        while (!queue.isEmpty()) {
            Node current = queue.poll();

            // Animasi
            mazePanel.addExploredNode(current);
            sleepDelay(10); // Atur kecepatan animasi (ms)

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

    // --- ALGORITMA DFS (Stack) ---
    private boolean runDFS(Node start, Node end) {
        Stack<Node> stack = new Stack<>();
        stack.push(start);
        start.visited = true;

        while (!stack.isEmpty()) {
            Node current = stack.pop();

            // Animasi
            mazePanel.addExploredNode(current);
            sleepDelay(10);

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

    // Helper untuk delay
    private void sleepDelay(int millis) {
        try { Thread.sleep(millis); } catch (InterruptedException e) {}
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MazeApp().setVisible(true));
    }
}