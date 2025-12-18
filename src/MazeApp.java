import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.util.*;
import java.util.List;

public class MazeApp extends JFrame {
    private MazeGraphModel mazeModel;
    private MazePanel mazePanel;
    private boolean isAnimating = false;
    private Image fullBackgroundImage;

    // Komponen UI untuk Dropdown
    private JComboBox<String> algorithmSelector;

    public MazeApp() {
        setTitle("Maze Solver - Step 4 (Dropdown UI)");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Load Background Image
        try {
            fullBackgroundImage = ImageIO.read(new File("C:/Backgroundmaze.jpg"));
        } catch (IOException e) {
            System.err.println("Info: Backgroundmaze.jpg tidak ditemukan. Menggunakan warna solid.");
        }

        mazeModel = new MazeGraphModel(30, 20);
        mazePanel = new MazePanel(mazeModel);

        // WRAPPER PANEL (Center & Background)
        JPanel centerWrapper = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (fullBackgroundImage != null) {
                    g.drawImage(fullBackgroundImage, 0, 0, getWidth(), getHeight(), this);
                } else {
                    g.setColor(new Color(45, 45, 45));
                    g.fillRect(0, 0, getWidth(), getHeight());
                }
            }
        };
        centerWrapper.setBackground(new Color(45, 45, 45));
        centerWrapper.add(mazePanel);

        JScrollPane scrollPane = new JScrollPane(centerWrapper);
        scrollPane.setBorder(null);

        // --- PANEL CONTROL (BAGIAN YANG DIUBAH) ---
        JPanel controlPanel = new JPanel();
        controlPanel.setBackground(new Color(60, 60, 60));
        controlPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 15, 10)); // Tambah jarak antar elemen

        // 1. Tombol Generate
        JButton btnGenerate = createButton("Generate New Map");

        // 2. Label & Dropdown
        JLabel lblAlgo = new JLabel("Select Algorithm:");
        lblAlgo.setForeground(Color.WHITE);
        lblAlgo.setFont(new Font("SansSerif", Font.BOLD, 12));

        String[] algorithms = {"BFS", "DFS", "Dijkstra", "A* (A-Star)"};
        algorithmSelector = new JComboBox<>(algorithms);
        algorithmSelector.setFont(new Font("SansSerif", Font.PLAIN, 12));
        algorithmSelector.setFocusable(false); // Agar tidak highlight aneh
        algorithmSelector.setPreferredSize(new Dimension(120, 30));

        // 3. Tombol Eksekusi (Solve)
        JButton btnSolve = createButton("Start Solving");
        btnSolve.setBackground(new Color(144, 238, 144)); // Hijau terang untuk tombol start
        btnSolve.setForeground(new Color(0, 100, 0));

        // Tambahkan ke Panel
        controlPanel.add(btnGenerate);
        controlPanel.add(new JSeparator(SwingConstants.VERTICAL)); // Garis pemisah
        controlPanel.add(lblAlgo);
        controlPanel.add(algorithmSelector);
        controlPanel.add(btnSolve);

        // --- EVENT LISTENERS ---

        // Generate Map
        btnGenerate.addActionListener(e -> {
            if (isAnimating) return;
            mazeModel.generateMaze();
            mazePanel.setMazeModel(mazeModel);
        });

        // Start Solving (Satu tombol untuk semua)
        btnSolve.addActionListener(e -> {
            if (isAnimating) return;

            // Ambil pilihan dari Dropdown
            String selected = (String) algorithmSelector.getSelectedItem();

            // Mapping nama di dropdown ke kode internal
            String code = "BFS";
            if (selected.equals("DFS")) code = "DFS";
            else if (selected.equals("Dijkstra")) code = "DIJKSTRA";
            else if (selected.contains("A*")) code = "ASTAR";

            solveMaze(code);
        });

        add(scrollPane, BorderLayout.CENTER);
        add(controlPanel, BorderLayout.SOUTH);

        setSize(1100, 850);
        setLocationRelativeTo(null);
    }

    private JButton createButton(String text) {
        JButton btn = new JButton(text);
        btn.setFocusPainted(false);
        btn.setBackground(new Color(230, 230, 230));
        btn.setFont(new Font("SansSerif", Font.BOLD, 12));
        btn.setPreferredSize(new Dimension(150, 30)); // Ukuran konsisten
        return btn;
    }

    private void solveMaze(String algorithm) {
        isAnimating = true;
        mazeModel.resetVisited();
        mazePanel.clearPath();

        // Disable tombol saat animasi berjalan
        algorithmSelector.setEnabled(false);

        new Thread(() -> {
            Node start = mazeModel.getGrid()[0][0];
            Node end = mazeModel.getGrid()[mazeModel.getCols() - 1][mazeModel.getRows() - 1];

            boolean found = false;
            int totalCost = 0;

            long startTime = System.currentTimeMillis();

            if (algorithm.equals("BFS")) {
                found = runBFS(start, end);
            } else if (algorithm.equals("DFS")) {
                found = runDFS(start, end);
            } else if (algorithm.equals("DIJKSTRA")) {
                totalCost = runDijkstra(start, end);
                found = (totalCost != -1);
            } else if (algorithm.equals("ASTAR")) {
                totalCost = runAStar(start, end);
                found = (totalCost != -1);
            }

            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;

            if (found) {
                List<Node> path = new ArrayList<>();
                Node current = end;
                while (current != null) {
                    path.add(current);
                    current = current.parent;
                }
                mazePanel.setFinalPath(path);

                String msg = "Algorithm: " + algorithm + "\n" +
                        "Time: " + duration + " ms\n";
                if (algorithm.equals("DIJKSTRA") || algorithm.equals("ASTAR")) {
                    msg += "Total Cost: " + totalCost;
                } else {
                    msg += "Steps: " + path.size();
                }
                JOptionPane.showMessageDialog(this, msg);
            } else {
                JOptionPane.showMessageDialog(this, "Path not found!");
            }

            // Re-enable tombol setelah selesai
            algorithmSelector.setEnabled(true);
            isAnimating = false;
        }).start();
    }

    // --- ALGORITMA BFS & DFS ---

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

    // --- ALGORITMA DIJKSTRA ---
    private int runDijkstra(Node start, Node end) {
        PriorityQueue<PathState> pq = new PriorityQueue<>();
        Map<Node, Integer> dist = new HashMap<>();

        dist.put(start, 0);
        pq.add(new PathState(start, 0));

        while (!pq.isEmpty()) {
            PathState current = pq.poll();
            Node u = current.node;
            int currentCost = current.cost;

            if (currentCost > dist.getOrDefault(u, Integer.MAX_VALUE)) continue;

            u.visited = true;
            mazePanel.addExploredNode(u);
            sleepDelay(5);

            if (u == end) return currentCost;

            for (Node v : u.neighbors) {
                int weight = v.getCost();
                int newDist = currentCost + weight;

                if (newDist < dist.getOrDefault(v, Integer.MAX_VALUE)) {
                    dist.put(v, newDist);
                    v.parent = u;
                    pq.add(new PathState(v, newDist));
                }
            }
        }
        return -1;
    }

    // --- ALGORITMA A* (A-STAR) ---
    private int runAStar(Node start, Node end) {
        PriorityQueue<AStarState> pq = new PriorityQueue<>();
        Map<Node, Integer> gScore = new HashMap<>();

        gScore.put(start, 0);
        int hStart = calculateHeuristic(start, end);
        pq.add(new AStarState(start, 0, hStart));

        while (!pq.isEmpty()) {
            AStarState current = pq.poll();
            Node u = current.node;

            if (current.gCost > gScore.getOrDefault(u, Integer.MAX_VALUE)) continue;

            u.visited = true;
            mazePanel.addExploredNode(u);
            sleepDelay(5);

            if (u == end) return current.gCost;

            for (Node v : u.neighbors) {
                int weight = v.getCost();
                int newG = current.gCost + weight;

                if (newG < gScore.getOrDefault(v, Integer.MAX_VALUE)) {
                    gScore.put(v, newG);
                    v.parent = u;

                    int newH = calculateHeuristic(v, end);
                    pq.add(new AStarState(v, newG, newH));
                }
            }
        }
        return -1;
    }

    private int calculateHeuristic(Node a, Node b) {
        return Math.abs(a.x - b.x) + Math.abs(a.y - b.y);
    }

    private void sleepDelay(int millis) {
        try { Thread.sleep(millis); } catch (InterruptedException e) {}
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MazeApp().setVisible(true));
    }
}