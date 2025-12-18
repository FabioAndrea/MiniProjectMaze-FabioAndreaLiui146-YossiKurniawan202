import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
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

    // Komponen UI
    private JComboBox<String> algorithmSelector;

    // --- KOMPONEN STATISTIK (TEMA JUNGLE) ---
    private DefaultTableModel statsModel;
    private JTable statsTable;
    private JLabel lblStatus;

    // Palet Warna Jungle Khusus Statistik
    private final Color JUNGLE_BG_PANEL = new Color(30, 50, 30);     // Latar belakang panel (Hutan Gelap)
    private final Color JUNGLE_WOOD_DARK = new Color(90, 60, 30);    // Warna Kayu Gelap (Header/Border)
    private final Color JUNGLE_PARCHMENT = new Color(235, 225, 200); // Warna Kertas Kuno (Isi Tabel)
    private final Color JUNGLE_TEXT_DARK = new Color(60, 40, 20);    // Teks Coklat Tua
    private final Color JUNGLE_ACCENT_GOLD = new Color(255, 215, 0); // Aksen Emas

    public MazeApp() {
        setTitle("Maze Solver - Jungle Analytics");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Load Background Image
        try {
            fullBackgroundImage = ImageIO.read(new File("C:/Backgroundmaze.jpg"));
        } catch (IOException e) {
            System.err.println("Info: Backgroundmaze.jpg tidak ditemukan.");
        }

        mazeModel = new MazeGraphModel(30, 20);
        mazePanel = new MazePanel(mazeModel);

        // WRAPPER PANEL (Center - Tetap Gelap Biasa)
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

        // --- PANEL STATISTIK (KANAN - TEMA JUNGLE BARU) ---
        JPanel rightStatsPanel = createStatsPanel();

        // --- PANEL CONTROL (BAWAH - TETAP SAMA) ---
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
            calculateAndShowStats();
        });

        btnSolve.addActionListener(e -> {
            if (isAnimating) return;
            String selected = (String) algorithmSelector.getSelectedItem();
            String code = "BFS";
            if (selected.equals("DFS")) code = "DFS";
            else if (selected.equals("Dijkstra")) code = "DIJKSTRA";
            else if (selected.contains("A*")) code = "ASTAR";
            solveMaze(code);
        });

        add(scrollPane, BorderLayout.CENTER);
        add(rightStatsPanel, BorderLayout.EAST);
        add(controlPanel, BorderLayout.SOUTH);

        setSize(1350, 850);
        setLocationRelativeTo(null);

        SwingUtilities.invokeLater(this::calculateAndShowStats);
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
    // BAGIAN YANG DIUBAH: DESAIN PANEL STATISTIK JUNGLE
    // ---------------------------------------------------------
    private JPanel createStatsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setPreferredSize(new Dimension(380, 0));
        // Latar belakang panel gelap seperti dalam hutan
        panel.setBackground(JUNGLE_BG_PANEL);

        // Border majemuk: Garis kayu tebal di luar, padding di dalam
        panel.setBorder(new CompoundBorder(
                new MatteBorder(0, 8, 0, 0, JUNGLE_WOOD_DARK), // Frame Kayu Kiri
                new EmptyBorder(15, 15, 15, 15) // Padding isi
        ));

        // Judul dengan gaya petualangan
        JLabel lblTitle = new JLabel("STATISTIC", JLabel.CENTER);
        lblTitle.setFont(new Font("Georgia", Font.BOLD, 22)); // Font Serif agar lebih klasik
        lblTitle.setForeground(JUNGLE_ACCENT_GOLD);
        lblTitle.setBorder(new EmptyBorder(0, 0, 20, 0));

        // Tabel Data
        String[] cols = {"Rank", "Algorithm", "Cost", "Visited", "Time"};
        Object[][] initData = {};

        statsModel = new DefaultTableModel(initData, cols) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };

        statsTable = new JTable(statsModel);
        statsTable.setRowHeight(50); // Baris lebih tinggi agar kokoh
        statsTable.setFont(new Font("Georgia", Font.PLAIN, 14));
        statsTable.setGridColor(JUNGLE_WOOD_DARK); // Garis grid warna kayu
        statsTable.setIntercellSpacing(new Dimension(1, 1)); // Jarak antar sel

        // Custom Renderer untuk membuat sel terlihat seperti kertas kuno (Parchment)
        DefaultTableCellRenderer jungleRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    c.setBackground(JUNGLE_PARCHMENT); // Background kertas kuno
                    c.setForeground(JUNGLE_TEXT_DARK); // Teks coklat tua
                } else {
                    c.setBackground(JUNGLE_ACCENT_GOLD.darker()); // Warna seleksi
                    c.setForeground(Color.BLACK);
                }
                setHorizontalAlignment(JLabel.CENTER);
                return c;
            }
        };

        // Terapkan renderer ke semua kolom
        for (int i = 0; i < statsTable.getColumnCount(); i++) {
            statsTable.getColumnModel().getColumn(i).setCellRenderer(jungleRenderer);
        }

        // Styling Header Tabel (Kayu Gelap)
        JTableHeader header = statsTable.getTableHeader();
        header.setFont(new Font("Georgia", Font.BOLD, 14));
        header.setBackground(JUNGLE_WOOD_DARK);
        header.setForeground(JUNGLE_PARCHMENT);
        header.setPreferredSize(new Dimension(0, 40));
        // Border header agar terlihat timbul
        header.setBorder(BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED, JUNGLE_WOOD_DARK.brighter(), JUNGLE_WOOD_DARK.darker()));

        // ScrollPane dengan border kayu
        JScrollPane tableScroll = new JScrollPane(statsTable);
        tableScroll.setBorder(new LineBorder(JUNGLE_WOOD_DARK, 3)); // Bingkai kayu tebal
        tableScroll.getViewport().setBackground(JUNGLE_BG_PANEL);

        // Status Label di bawah
        lblStatus = new JLabel("<html><center>Mengamati jejak...</center></html>", JLabel.CENTER);
        lblStatus.setFont(new Font("Georgia", Font.ITALIC, 14));
        lblStatus.setForeground(JUNGLE_PARCHMENT);
        lblStatus.setBorder(new EmptyBorder(20, 0, 0, 0));

        panel.add(lblTitle, BorderLayout.NORTH);
        panel.add(tableScroll, BorderLayout.CENTER);
        panel.add(lblStatus, BorderLayout.SOUTH);

        return panel;
    }

    // ---------------------------------------------------------
    // LOGIKA STATISTIK (TETAP SAMA DENGAN YANG SUDAH DIPERBAIKI)
    // ---------------------------------------------------------
    private void calculateAndShowStats() {
        Node start = mazeModel.getGrid()[0][0];
        Node end = mazeModel.getGrid()[mazeModel.getCols() - 1][mazeModel.getRows() - 1];

        List<AlgoResult> results = new ArrayList<>();
        mazeModel.resetVisited(); results.add(runSilentAlgo("BFS", start, end));
        mazeModel.resetVisited(); results.add(runSilentAlgo("DFS", start, end));
        mazeModel.resetVisited(); results.add(runSilentAlgo("DIJKSTRA", start, end));
        mazeModel.resetVisited(); results.add(runSilentAlgo("ASTAR", start, end));

        mazeModel.resetVisited();

        // Sorting: Cost Terendah -> Waktu Tercepat
        Collections.sort(results, (a, b) -> {
            if (a.totalCost != b.totalCost) return Integer.compare(a.totalCost, b.totalCost);
            return Double.compare(a.durationNano, b.durationNano);
        });

        statsModel.setRowCount(0);

        // Menggunakan HTML font size agar medali terlihat jelas
        String[] medals = {
                "<html><font color='#604020'>1st</font></html>",
                "<html><font color='#604020'>2nd</font></html>",
                "<html><font color='#604020'>3rd</font></html>",
                "<html><font color='#604020'>4th</font></html>"
        };

        for (int i = 0; i < results.size(); i++) {
            AlgoResult res = results.get(i);
            String rank = (i < medals.length) ? medals[i] : String.valueOf(i + 1);

            String costText = (res.totalCost >= Integer.MAX_VALUE/2) ? "X" : String.valueOf(res.totalCost);
            String visitedText = String.valueOf(res.visitedCount);
            String timeText = String.format("%.2f ms", res.getDurationMs());

            statsModel.addRow(new Object[]{rank, res.algorithmName, costText, visitedText, timeText});
        }

        String winner = results.get(0).algorithmName;
        // Update status dengan warna emas
        lblStatus.setText("<html><center>Penjelajah Terbaik:<br><b style='color:#FFDF00; font-size:16px'>" + winner + "</b> (Paling Efisien!)</center></html>");
    }

    // --- HELPER: SILENT RUNNERS (TETAP SAMA) ---
    private AlgoResult runSilentAlgo(String type, Node start, Node end) {
        long startTime = System.nanoTime();
        int visitedNodes = 0;
        int costResult = Integer.MAX_VALUE / 2;
        boolean found = false;

        if (type.equals("BFS")) {
            Queue<Node> queue = new LinkedList<>();
            queue.add(start); start.visited = true; visitedNodes++;
            while(!queue.isEmpty()){
                Node u = queue.poll();
                if(u == end) { found=true; costResult=calculatePathCost(u); break; }
                for(Node v : u.neighbors){
                    if(!v.visited){ v.visited=true; v.parent=u; visitedNodes++; queue.add(v); }
                }
            }
        } else if (type.equals("DFS")) {
            Stack<Node> stack = new Stack<>();
            stack.push(start); start.visited = true; visitedNodes++;
            while(!stack.isEmpty()){
                Node u = stack.pop();
                if(u == end) { found=true; costResult=calculatePathCost(u); break; }
                for(Node v : u.neighbors){
                    if(!v.visited){ v.visited=true; v.parent=u; visitedNodes++; stack.push(v); }
                }
            }
        } else if (type.equals("DIJKSTRA")) {
            PriorityQueue<PathState> pq = new PriorityQueue<>();
            Map<Node, Integer> dist = new HashMap<>();
            dist.put(start, 0); pq.add(new PathState(start, 0));
            while(!pq.isEmpty()){
                PathState curr = pq.poll(); Node u = curr.node;
                if(curr.cost > dist.getOrDefault(u, Integer.MAX_VALUE)) continue;
                u.visited=true; visitedNodes++;
                if(u == end) { found=true; costResult=curr.cost; break; }
                for(Node v : u.neighbors){
                    int newDist = curr.cost + v.getCost();
                    if(newDist < dist.getOrDefault(v, Integer.MAX_VALUE)){
                        dist.put(v, newDist); v.parent=u; pq.add(new PathState(v, newDist));
                    }
                }
            }
        } else if (type.equals("ASTAR")) {
            PriorityQueue<AStarState> pq = new PriorityQueue<>();
            Map<Node, Integer> gScore = new HashMap<>();
            gScore.put(start, 0);
            pq.add(new AStarState(start, 0, Math.abs(start.x-end.x)+Math.abs(start.y-end.y)));
            while(!pq.isEmpty()){
                AStarState curr = pq.poll(); Node u = curr.node;
                if(curr.gCost > gScore.getOrDefault(u, Integer.MAX_VALUE)) continue;
                u.visited=true; visitedNodes++;
                if(u == end) { found=true; costResult=curr.gCost; break; }
                for(Node v : u.neighbors){
                    int newG = curr.gCost + v.getCost();
                    if(newG < gScore.getOrDefault(v, Integer.MAX_VALUE)){
                        gScore.put(v, newG); v.parent=u;
                        pq.add(new AStarState(v, newG, Math.abs(v.x-end.x)+Math.abs(v.y-end.y)));
                    }
                }
            }
        }
        long endTime = System.nanoTime();
        return new AlgoResult(type, endTime - startTime, 0, visitedNodes, found ? costResult : Integer.MAX_VALUE);
    }

    private int calculatePathCost(Node endNode) {
        int cost = 0; Node temp = endNode;
        while(temp != null) { cost += temp.getCost(); temp = temp.parent; }
        return cost;
    }

    // --- VISUAL SOLVERS (TETAP SAMA) ---
    private void solveMaze(String algorithm) {
        isAnimating = true; mazeModel.resetVisited(); mazePanel.clearPath(); algorithmSelector.setEnabled(false);
        new Thread(() -> {
            Node start = mazeModel.getGrid()[0][0]; Node end = mazeModel.getGrid()[mazeModel.getCols() - 1][mazeModel.getRows() - 1];
            boolean found = false;
            if (algorithm.equals("BFS")) found = runBFS(start, end);
            else if (algorithm.equals("DFS")) found = runDFS(start, end);
            else if (algorithm.equals("DIJKSTRA")) found = (runDijkstra(start, end) != -1);
            else if (algorithm.equals("ASTAR")) found = (runAStar(start, end) != -1);

            if (found) {
                List<Node> path = new ArrayList<>(); Node current = end;
                while (current != null) { path.add(current); current = current.parent; }
                mazePanel.setFinalPath(path);
            } else { JOptionPane.showMessageDialog(this, "Path not found!"); }
            algorithmSelector.setEnabled(true); isAnimating = false;
        }).start();
    }

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