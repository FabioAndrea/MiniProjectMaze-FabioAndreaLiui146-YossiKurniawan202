import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
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

    // Komponen UI Control
    private JComboBox<String> algorithmSelector;

    // --- KOMPONEN STATISTIK BARU (DROPDOWN MODEL) ---
    private JComboBox<String> statsDropdown; // Dropdown untuk pilih hasil
    private JLabel lblTime, lblCost, lblVisited; // Label detail data
    private JLabel lblEfficiencySummary; // Label kesimpulan efisiensi

    // Penyimpanan Data Hasil Run
    private Map<String, AlgoResult> runHistory = new LinkedHashMap<>();

    // Palet Warna Jungle
    private final Color JUNGLE_BG_PANEL = new Color(30, 50, 30);
    private final Color JUNGLE_WOOD_DARK = new Color(90, 60, 30);
    private final Color JUNGLE_PARCHMENT = new Color(235, 225, 200);
    private final Color JUNGLE_TEXT_DARK = new Color(60, 40, 20);
    private final Color JUNGLE_ACCENT_GOLD = new Color(255, 215, 0);

    public MazeApp() {
        setTitle("Maze Solver - Jungle Logs");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Load Background
        try {
            fullBackgroundImage = ImageIO.read(new File("C:/Backgroundmaze.jpg"));
        } catch (IOException e) {
            System.err.println("Info: Backgroundmaze.jpg tidak ditemukan.");
        }

        mazeModel = new MazeGraphModel(30, 20);
        mazePanel = new MazePanel(mazeModel);

        // WRAPPER PANEL
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

        // --- PANEL STATISTIK KANAN (MODIFIKASI) ---
        JPanel rightStatsPanel = createJungleStatsPanel();

        // --- PANEL CONTROL BAWAH ---
        JPanel controlPanel = new JPanel();
        controlPanel.setBackground(new Color(60, 60, 60));
        controlPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 15, 10));

        JButton btnGenerate = createButton("Generate New Map");

        JLabel lblAlgo = new JLabel("Select Algorithm:");
        lblAlgo.setForeground(Color.WHITE);
        lblAlgo.setFont(new Font("SansSerif", Font.BOLD, 12));

        String[] algorithms = {"BFS", "DFS", "Dijkstra", "A* (A-Star)"};
        algorithmSelector = new JComboBox<>(algorithms);
        algorithmSelector.setFont(new Font("SansSerif", Font.PLAIN, 12));
        algorithmSelector.setFocusable(false);
        algorithmSelector.setPreferredSize(new Dimension(120, 30));

        JButton btnSolve = createButton("Start Solving");
        btnSolve.setBackground(new Color(144, 238, 144));
        btnSolve.setForeground(new Color(0, 100, 0));

        controlPanel.add(btnGenerate);
        controlPanel.add(new JSeparator(SwingConstants.VERTICAL));
        controlPanel.add(lblAlgo);
        controlPanel.add(algorithmSelector);
        controlPanel.add(btnSolve);

        // --- EVENT LISTENERS ---
        btnGenerate.addActionListener(e -> {
            if (isAnimating) return;
            mazeModel.generateMaze();
            mazePanel.setMazeModel(mazeModel);
            resetStats(); // Reset data statistik saat map baru
        });

        btnSolve.addActionListener(e -> {
            if (isAnimating) return;
            String selected = (String) algorithmSelector.getSelectedItem();
            String code = "BFS";
            if (selected.equals("DFS")) code = "DFS";
            else if (selected.equals("Dijkstra")) code = "DIJKSTRA";
            else if (selected.contains("A*")) code = "ASTAR";

            // Jalankan animasi (statistik muncul setelah selesai)
            solveMaze(code, selected);
        });

        // Event Listener untuk Dropdown Statistik (Ganti tampilan data saat dipilih)
        statsDropdown.addActionListener(e -> {
            String selectedAlgo = (String) statsDropdown.getSelectedItem();
            if (selectedAlgo != null && runHistory.containsKey(selectedAlgo)) {
                showAlgoDetails(runHistory.get(selectedAlgo));
            } else {
                clearInfoDisplay();
            }
        });

        add(scrollPane, BorderLayout.CENTER);
        add(rightStatsPanel, BorderLayout.EAST);
        add(controlPanel, BorderLayout.SOUTH);

        setSize(1350, 850);
        setLocationRelativeTo(null);
    }

    private JButton createButton(String text) {
        JButton btn = new JButton(text);
        btn.setFocusPainted(false);
        btn.setBackground(new Color(230, 230, 230));
        btn.setFont(new Font("SansSerif", Font.BOLD, 12));
        btn.setPreferredSize(new Dimension(150, 30));
        return btn;
    }

    // ---------------------------------------------------------
    // PANEL STATISTIK BARU (DROPDOWN + DETAIL)
    // ---------------------------------------------------------
    private JPanel createJungleStatsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setPreferredSize(new Dimension(350, 0));
        panel.setBackground(JUNGLE_BG_PANEL);
        panel.setBorder(new CompoundBorder(
                new MatteBorder(0, 8, 0, 0, JUNGLE_WOOD_DARK),
                new EmptyBorder(20, 20, 20, 20)
        ));

        // 1. HEADER
        JLabel lblTitle = new JLabel("STATISTIC", JLabel.CENTER);
        lblTitle.setFont(new Font("Georgia", Font.BOLD, 20));
        lblTitle.setForeground(JUNGLE_ACCENT_GOLD);
        lblTitle.setBorder(new EmptyBorder(0, 0, 20, 0));

        // 2. DROPDOWN SELEKSI HASIL
        JLabel lblSelect = new JLabel("Lihat Data Algoritma:");
        lblSelect.setForeground(JUNGLE_PARCHMENT);
        lblSelect.setFont(new Font("SansSerif", Font.PLAIN, 12));

        statsDropdown = new JComboBox<>();
        statsDropdown.setFont(new Font("Georgia", Font.BOLD, 14));
        statsDropdown.setBackground(JUNGLE_PARCHMENT);
        statsDropdown.setForeground(JUNGLE_TEXT_DARK);

        JPanel topSection = new JPanel(new BorderLayout(0, 5));
        topSection.setBackground(JUNGLE_BG_PANEL);
        topSection.add(lblSelect, BorderLayout.NORTH);
        topSection.add(statsDropdown, BorderLayout.CENTER);
        topSection.setBorder(new EmptyBorder(0, 0, 20, 0));

        // 3. PANEL DETAIL (KOTAK INFORMASI)
        JPanel infoPanel = new JPanel(new GridLayout(3, 1, 0, 10));
        infoPanel.setBackground(JUNGLE_BG_PANEL);

        lblTime = createDetailCard("⏳ Waktu Eksekusi");
        lblCost = createDetailCard("💎 Total Cost (Biaya)");
        lblVisited = createDetailCard("👣 Node Dikunjungi");

        infoPanel.add(lblTime);
        infoPanel.add(lblCost);
        infoPanel.add(lblVisited);

        // 4. SUMMARY (KESIMPULAN EFISIENSI)
        lblEfficiencySummary = new JLabel("<html><center>Belum ada data.<br>Jalankan algoritma!</center></html>", JLabel.CENTER);
        lblEfficiencySummary.setFont(new Font("Georgia", Font.ITALIC, 13));
        lblEfficiencySummary.setForeground(JUNGLE_PARCHMENT);
        lblEfficiencySummary.setBorder(new EmptyBorder(20, 0, 0, 0));

        // Gabungkan
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(JUNGLE_BG_PANEL);
        contentPanel.add(topSection, BorderLayout.NORTH);
        contentPanel.add(infoPanel, BorderLayout.CENTER);

        panel.add(lblTitle, BorderLayout.NORTH);
        panel.add(contentPanel, BorderLayout.CENTER);
        panel.add(lblEfficiencySummary, BorderLayout.SOUTH);

        return panel;
    }

    private JLabel createDetailCard(String title) {
        JLabel lbl = new JLabel("<html><div style='text-align:center;'><b>" + title + "</b><br><font size='5'>-</font></div></html>", JLabel.CENTER);
        lbl.setOpaque(true);
        lbl.setBackground(JUNGLE_PARCHMENT);
        lbl.setForeground(JUNGLE_TEXT_DARK);
        lbl.setFont(new Font("Georgia", Font.PLAIN, 14));
        lbl.setBorder(new LineBorder(JUNGLE_WOOD_DARK, 2, true)); // Rounded border effect hint
        return lbl;
    }

    // ---------------------------------------------------------
    // LOGIKA DATA & UPDATE UI
    // ---------------------------------------------------------

    private void resetStats() {
        runHistory.clear();
        statsDropdown.removeAllItems();
        clearInfoDisplay();
        lblEfficiencySummary.setText("<html><center>Map Baru.<br>Siap Menjelajah!</center></html>");
    }

    private void clearInfoDisplay() {
        updateCard(lblTime, "⏳ Waktu Eksekusi", "-");
        updateCard(lblCost, "💎 Total Cost", "-");
        updateCard(lblVisited, "👣 Node Dikunjungi", "-");
    }

    private void updateCard(JLabel label, String title, String value) {
        label.setText("<html><div style='text-align:center; width:250px;'><b>" + title + "</b><br><font size='5'>" + value + "</font></div></html>");
    }

    private void showAlgoDetails(AlgoResult res) {
        updateCard(lblTime, "⏳ Waktu Eksekusi", String.format("%.2f ms", res.getDurationMs()));
        updateCard(lblCost, "💎 Total Cost", (res.totalCost >= Integer.MAX_VALUE/2) ? "Gagal" : String.valueOf(res.totalCost));
        updateCard(lblVisited, "👣 Node Dikunjungi", String.valueOf(res.visitedCount));
    }

    private void updateEfficiencySummary() {
        if (runHistory.isEmpty()) return;

        AlgoResult best = null;
        AlgoResult worst = null;

        for (AlgoResult res : runHistory.values()) {
            if (res.totalCost >= Integer.MAX_VALUE/2) continue; // Skip gagal

            if (best == null || res.totalCost < best.totalCost) best = res;
            if (worst == null || res.totalCost > worst.totalCost) worst = res;
        }

        if (best != null) {
            String bestName = best.algorithmName;
            String worstName = (worst != null && !worst.algorithmName.equals(bestName)) ? worst.algorithmName : "-";

            lblEfficiencySummary.setText("<html><div style='text-align:center; border-top:1px solid #d4af37; padding-top:10px;'>" +
                    "✅ <b>Paling Efisien:</b> <font color='#90EE90'>" + bestName + "</font><br>" +
                    "❌ <b>Paling Boros:</b> <font color='#FF6347'>" + worstName + "</font>" +
                    "</div></html>");
        }
    }

    // ---------------------------------------------------------
    // SOLVING & ANIMASI (LOGIKA LAMA DISESUAIKAN)
    // ---------------------------------------------------------
    private void solveMaze(String algorithmCode, String displayName) {
        isAnimating = true;
        mazeModel.resetVisited();
        mazePanel.clearPath();
        algorithmSelector.setEnabled(false);

        new Thread(() -> {
            Node start = mazeModel.getGrid()[0][0];
            Node end = mazeModel.getGrid()[mazeModel.getCols() - 1][mazeModel.getRows() - 1];
            boolean found = false;

            long startTime = System.nanoTime();
            int visitedCount = 0;
            int finalCost = Integer.MAX_VALUE;

            // Kita perlu modifikasi sedikit runner untuk return data statistik real (bukan silent)
            // Tapi karena struktur kode sebelumnya terpisah visual/silent, kita pakai visual runner
            // lalu hitung statistik manual setelah selesai visualisasi agar akurat dengan apa yang dilihat user.

            if (algorithmCode.equals("BFS")) found = runBFS(start, end);
            else if (algorithmCode.equals("DFS")) found = runDFS(start, end);
            else if (algorithmCode.equals("DIJKSTRA")) found = (runDijkstra(start, end) != -1);
            else if (algorithmCode.equals("ASTAR")) found = (runAStar(start, end) != -1);

            long endTime = System.nanoTime();

            // Hitung statistik final setelah visualisasi selesai
            if (found) {
                // Reconstruct path untuk display & hitung cost
                List<Node> path = new ArrayList<>();
                Node current = end;
                int calcCost = 0;
                while (current != null) {
                    path.add(current);
                    calcCost += current.getCost();
                    current = current.parent;
                }
                finalCost = calcCost;
                mazePanel.setFinalPath(path);
            }

            // Hitung total visited dari grid
            for(int x=0; x<mazeModel.getCols(); x++) {
                for(int y=0; y<mazeModel.getRows(); y++) {
                    if(mazeModel.getGrid()[x][y].visited) visitedCount++;
                }
            }

            // Simpan Hasil ke History
            AlgoResult res = new AlgoResult(displayName, endTime - startTime, 0, visitedCount, found ? finalCost : Integer.MAX_VALUE);

            // Update UI di Thread Swing
            SwingUtilities.invokeLater(() -> {
                runHistory.put(displayName, res);

                // Update Dropdown (hindari duplikasi item)
                boolean exists = false;
                for (int i = 0; i < statsDropdown.getItemCount(); i++) {
                    if (statsDropdown.getItemAt(i).equals(displayName)) {
                        exists = true; break;
                    }
                }
                if (!exists) statsDropdown.addItem(displayName);

                // Auto-select yang baru dijalankan
                statsDropdown.setSelectedItem(displayName);

                updateEfficiencySummary();
                algorithmSelector.setEnabled(true);
            });

            isAnimating = false;
        }).start();
    }

    // --- ALGORITMA RUNNERS (TETAP SAMA) ---
    private boolean runBFS(Node start, Node end) {
        Queue<Node> queue = new LinkedList<>(); queue.add(start); start.visited = true;
        while (!queue.isEmpty()) {
            Node current = queue.poll(); mazePanel.addExploredNode(current); sleepDelay(5);
            if (current == end) return true;
            for (Node neighbor : current.neighbors) { if (!neighbor.visited) { neighbor.visited = true; neighbor.parent = current; queue.add(neighbor); }}
        } return false;
    }
    private boolean runDFS(Node start, Node end) {
        Stack<Node> stack = new Stack<>(); stack.push(start); start.visited = true;
        while (!stack.isEmpty()) {
            Node current = stack.pop(); mazePanel.addExploredNode(current); sleepDelay(5);
            if (current == end) return true;
            for (Node neighbor : current.neighbors) { if (!neighbor.visited) { neighbor.visited = true; neighbor.parent = current; stack.push(neighbor); }}
        } return false;
    }
    private int runDijkstra(Node start, Node end) {
        PriorityQueue<PathState> pq = new PriorityQueue<>(); Map<Node, Integer> dist = new HashMap<>();
        dist.put(start, 0); pq.add(new PathState(start, 0));
        while (!pq.isEmpty()) {
            PathState current = pq.poll(); Node u = current.node;
            if (current.cost > dist.getOrDefault(u, Integer.MAX_VALUE)) continue;
            u.visited = true; mazePanel.addExploredNode(u); sleepDelay(5);
            if (u == end) return current.cost;
            for (Node v : u.neighbors) {
                int newDist = current.cost + v.getCost();
                if (newDist < dist.getOrDefault(v, Integer.MAX_VALUE)) { dist.put(v, newDist); v.parent = u; pq.add(new PathState(v, newDist)); }
            }
        } return -1;
    }
    private int runAStar(Node start, Node end) {
        PriorityQueue<AStarState> pq = new PriorityQueue<>(); Map<Node, Integer> gScore = new HashMap<>();
        gScore.put(start, 0); pq.add(new AStarState(start, 0, Math.abs(start.x-end.x) + Math.abs(start.y-end.y)));
        while (!pq.isEmpty()) {
            AStarState current = pq.poll(); Node u = current.node;
            if (current.gCost > gScore.getOrDefault(u, Integer.MAX_VALUE)) continue;
            u.visited = true; mazePanel.addExploredNode(u); sleepDelay(5);
            if (u == end) return current.gCost;
            for (Node v : u.neighbors) {
                int newG = current.gCost + v.getCost();
                if (newG < gScore.getOrDefault(v, Integer.MAX_VALUE)) {
                    gScore.put(v, newG); v.parent = u; pq.add(new AStarState(v, newG, Math.abs(v.x-end.x) + Math.abs(v.y-end.y)));
                }
            }
        } return -1;
    }
    private void sleepDelay(int millis) { try { Thread.sleep(millis); } catch (InterruptedException e) {} }

    public static void main(String[] args) { SwingUtilities.invokeLater(() -> new MazeApp().setVisible(true)); }
}