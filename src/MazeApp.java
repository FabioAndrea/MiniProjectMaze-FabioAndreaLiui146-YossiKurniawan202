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

    // UI Components Kanan
    private JComboBox<String> algorithmSelector;
    private JComboBox<String> statsDropdown;
    private JLabel lblTime, lblCost, lblVisited;

    // UI Summary & Ranking
    private JLabel lblEfficiencySummary;
    private JTable rankingTable;
    private DefaultTableModel rankingModel;

    // UI Components Kiri (Kustomisasi)
    private JComboBox<String> cbMapSize;
    private JComboBox<String> cbTerrainDensity;
    private JComboBox<String> cbWallDensity;

    // Data History
    private Map<String, AlgoResult> runHistory = new LinkedHashMap<>();

    // Colors Theme
    private final Color JUNGLE_BG_PANEL = new Color(30, 50, 30);
    private final Color JUNGLE_WOOD_DARK = new Color(90, 60, 30);
    private final Color JUNGLE_PARCHMENT = new Color(235, 225, 200);
    private final Color JUNGLE_TEXT_DARK = new Color(60, 40, 20);
    private final Color JUNGLE_ACCENT_GOLD = new Color(255, 215, 0);
    private final Color JUNGLE_BTN_GREEN = new Color(34, 139, 34);

    public MazeApp() {
        setTitle("Maze Solver - Jungle Expedition");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        try {
            fullBackgroundImage = ImageIO.read(new File("C:/Backgroundmaze.jpg"));
        } catch (IOException e) {
            System.err.println("Info: Backgroundmaze.jpg tidak ditemukan.");
        }

        // --- 1. SETUP DEFAULT MODEL ---
        mazeModel = new MazeGraphModel(30, 20, 0.3, 0.9);
        mazePanel = new MazePanel(mazeModel);

        // --- 2. CENTER PANEL (Fixed Layout) ---
        JPanel centerWrapper = new JPanel(new BorderLayout()) {
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
        centerWrapper.add(mazePanel, BorderLayout.CENTER);

        JScrollPane scrollPane = new JScrollPane(centerWrapper);
        scrollPane.setBorder(null);

        // --- 3. PANELS ---
        JPanel leftPanel = createLeftPanel();
        JPanel rightPanel = createRightPanel();

        add(leftPanel, BorderLayout.WEST);
        add(scrollPane, BorderLayout.CENTER);
        add(rightPanel, BorderLayout.EAST);

        setSize(1400, 850);
        setLocationRelativeTo(null);
    }

    // --- PANEL KIRI: CUSTOMIZATION ---
    private JPanel createLeftPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setPreferredSize(new Dimension(240, 0));
        panel.setBackground(JUNGLE_BG_PANEL);
        panel.setBorder(new CompoundBorder(
                new MatteBorder(0, 0, 0, 8, JUNGLE_WOOD_DARK),
                new EmptyBorder(20, 20, 20, 20)
        ));

        JLabel lblTitle = new JLabel("SETUP MAP", JLabel.CENTER);
        lblTitle.setFont(new Font("Georgia", Font.BOLD, 18));
        lblTitle.setForeground(JUNGLE_ACCENT_GOLD);
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblSize = createLabel("Map Size:");
        String[] sizes = {"Small", "Medium", "Large"};
        cbMapSize = createComboBox(sizes);
        cbMapSize.setSelectedIndex(1);

        JLabel lblTerrain = createLabel("Terrain Obstacles:");
        String[] terrains = {"Clean", "Light", "Heavy"};
        cbTerrainDensity = createComboBox(terrains);
        cbTerrainDensity.setSelectedIndex(1);

        JLabel lblWall = createLabel("Wall Structure:");
        String[] walls = {"High", "Medium", "Low"};
        cbWallDensity = createComboBox(walls);
        cbWallDensity.setSelectedIndex(0);

        JLabel lblDecor = new JLabel("<html><br><center><i>\"Adjust parameters<br>to test the AI.\"</i></center></html>");
        lblDecor.setForeground(new Color(150, 150, 150));
        lblDecor.setFont(new Font("Georgia", Font.ITALIC, 12));
        lblDecor.setAlignmentX(Component.CENTER_ALIGNMENT);

        panel.add(lblTitle);
        panel.add(Box.createVerticalStrut(25));
        panel.add(lblSize); panel.add(Box.createVerticalStrut(5)); panel.add(cbMapSize);
        panel.add(Box.createVerticalStrut(15));
        panel.add(lblTerrain); panel.add(Box.createVerticalStrut(5)); panel.add(cbTerrainDensity);
        panel.add(Box.createVerticalStrut(15));
        panel.add(lblWall); panel.add(Box.createVerticalStrut(5)); panel.add(cbWallDensity);
        panel.add(Box.createVerticalStrut(30));
        panel.add(lblDecor);
        panel.add(Box.createVerticalGlue());

        return panel;
    }

    // --- PANEL KANAN: STATS + CONTROLS ---
    private JPanel createRightPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setPreferredSize(new Dimension(300, 0));
        mainPanel.setBackground(JUNGLE_BG_PANEL);
        mainPanel.setBorder(new CompoundBorder(
                new MatteBorder(0, 8, 0, 0, JUNGLE_WOOD_DARK),
                new EmptyBorder(10, 15, 10, 15)
        ));

        // --- STATS SECTION ---
        JPanel statsPanel = new JPanel(new BorderLayout());
        statsPanel.setBackground(JUNGLE_BG_PANEL);

        JLabel lblTitleStats = new JLabel("JOURNAL", JLabel.CENTER);
        lblTitleStats.setFont(new Font("Georgia", Font.BOLD, 18));
        lblTitleStats.setForeground(JUNGLE_ACCENT_GOLD);
        lblTitleStats.setBorder(new EmptyBorder(10, 0, 15, 0));

        JLabel lblSelect = new JLabel("Algorithm History:");
        lblSelect.setForeground(JUNGLE_PARCHMENT);
        statsDropdown = createComboBox(new String[]{});
        statsDropdown.addActionListener(e -> {
            String selectedAlgo = (String) statsDropdown.getSelectedItem();
            if (selectedAlgo != null && runHistory.containsKey(selectedAlgo)) {
                showAlgoDetails(runHistory.get(selectedAlgo));
            }
        });

        JPanel infoPanel = new JPanel(new GridLayout(3, 1, 0, 8));
        infoPanel.setBackground(JUNGLE_BG_PANEL);
        infoPanel.setBorder(new EmptyBorder(15, 0, 15, 0));

        lblTime = createDetailCard("⏳ Execution Time");
        lblCost = createDetailCard("💎 Total Cost");
        lblVisited = createDetailCard("👣 Nodes Visited");

        infoPanel.add(lblTime);
        infoPanel.add(lblCost);
        infoPanel.add(lblVisited);

        // --- SUMMARY & RANKING TABLE ---
        JPanel summaryContainer = new JPanel();
        summaryContainer.setLayout(new BoxLayout(summaryContainer, BoxLayout.Y_AXIS));
        summaryContainer.setBackground(JUNGLE_BG_PANEL);
        summaryContainer.setBorder(new EmptyBorder(10, 0, 0, 0));

        lblEfficiencySummary = new JLabel("<html><center>No Data.</center></html>", JLabel.CENTER);
        lblEfficiencySummary.setFont(new Font("Georgia", Font.ITALIC, 13));
        lblEfficiencySummary.setForeground(JUNGLE_PARCHMENT);
        lblEfficiencySummary.setAlignmentX(Component.CENTER_ALIGNMENT);

        String[] columns = {"Algorithm", "Time", "Weight"};
        rankingModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        rankingTable = new JTable(rankingModel);
        rankingTable.setRowHeight(25);
        rankingTable.setShowVerticalLines(false);
        rankingTable.setFillsViewportHeight(true);
        rankingTable.setBackground(JUNGLE_BG_PANEL.darker());
        rankingTable.setForeground(JUNGLE_PARCHMENT);
        rankingTable.setFont(new Font("SansSerif", Font.PLAIN, 12));
        rankingTable.setGridColor(JUNGLE_WOOD_DARK);

        JTableHeader header = rankingTable.getTableHeader();
        header.setBackground(JUNGLE_WOOD_DARK);
        header.setForeground(JUNGLE_ACCENT_GOLD);
        header.setFont(new Font("Georgia", Font.BOLD, 12));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        centerRenderer.setBackground(JUNGLE_BG_PANEL);
        centerRenderer.setForeground(JUNGLE_PARCHMENT);

        for (int i = 0; i < rankingTable.getColumnCount(); i++) {
            rankingTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        JScrollPane tableScroll = new JScrollPane(rankingTable);
        tableScroll.setPreferredSize(new Dimension(250, 120));
        tableScroll.setBorder(new LineBorder(JUNGLE_WOOD_DARK, 1));
        tableScroll.getViewport().setBackground(JUNGLE_BG_PANEL);

        summaryContainer.add(lblEfficiencySummary);
        summaryContainer.add(Box.createVerticalStrut(10));
        summaryContainer.add(tableScroll);

        statsPanel.add(lblTitleStats, BorderLayout.NORTH);
        JPanel statsCenter = new JPanel(new BorderLayout());
        statsCenter.setBackground(JUNGLE_BG_PANEL);
        statsCenter.add(lblSelect, BorderLayout.NORTH);
        statsCenter.add(statsDropdown, BorderLayout.CENTER);
        statsCenter.add(infoPanel, BorderLayout.SOUTH);

        statsPanel.add(statsCenter, BorderLayout.CENTER);
        statsPanel.add(summaryContainer, BorderLayout.SOUTH);

        // --- CONTROLS SECTION ---
        JPanel controlsPanel = new JPanel(new GridLayout(5, 1, 0, 10));
        controlsPanel.setBackground(JUNGLE_BG_PANEL);
        controlsPanel.setBorder(new CompoundBorder(
                new MatteBorder(2, 0, 0, 0, JUNGLE_WOOD_DARK),
                new EmptyBorder(20, 0, 0, 0)
        ));

        JButton btnGenerate = createStyledButton("Generate Map", JUNGLE_PARCHMENT, JUNGLE_TEXT_DARK);
        JLabel lblAlgo = new JLabel("Select Strategy:");
        lblAlgo.setForeground(Color.WHITE);
        lblAlgo.setHorizontalAlignment(SwingConstants.CENTER);

        String[] algorithms = {"BFS", "DFS", "Dijkstra", "A* (A-Star)"};
        algorithmSelector = createComboBox(algorithms);
        JButton btnSolve = createStyledButton("Start Mission", JUNGLE_BTN_GREEN, Color.WHITE);

        controlsPanel.add(btnGenerate);
        controlsPanel.add(lblAlgo);
        controlsPanel.add(algorithmSelector);
        controlsPanel.add(Box.createVerticalStrut(5));
        controlsPanel.add(btnSolve);

        // Actions
        btnGenerate.addActionListener(e -> {
            if (isAnimating) return;
            handleGenerateMap();
        });

        btnSolve.addActionListener(e -> {
            if (isAnimating) return;
            String selected = (String) algorithmSelector.getSelectedItem();
            String code = "BFS";
            if (selected.equals("DFS")) code = "DFS";
            else if (selected.equals("Dijkstra")) code = "DIJKSTRA";
            else if (selected.contains("A*")) code = "ASTAR";
            solveMaze(code, selected);
        });

        mainPanel.add(statsPanel, BorderLayout.NORTH);
        mainPanel.add(controlsPanel, BorderLayout.SOUTH);

        return mainPanel;
    }

    // --- UI HELPER METHODS ---
    private JLabel createLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setForeground(Color.WHITE);
        lbl.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    private JComboBox<String> createComboBox(String[] items) {
        JComboBox<String> cb = new JComboBox<>(items);
        cb.setFont(new Font("SansSerif", Font.PLAIN, 13));
        cb.setBackground(JUNGLE_PARCHMENT);
        cb.setForeground(JUNGLE_TEXT_DARK);
        cb.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        cb.setAlignmentX(Component.LEFT_ALIGNMENT);
        return cb;
    }

    private JButton createStyledButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setFocusPainted(false);
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFont(new Font("Georgia", Font.BOLD, 14));
        btn.setBorder(BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JLabel createDetailCard(String title) {
        JLabel lbl = new JLabel("<html><div style='text-align:center;'><b>" + title + "</b><br><font size='4'>-</font></div></html>", JLabel.CENTER);
        lbl.setOpaque(true);
        lbl.setBackground(JUNGLE_PARCHMENT);
        lbl.setForeground(JUNGLE_TEXT_DARK);
        lbl.setFont(new Font("Georgia", Font.PLAIN, 12));
        lbl.setBorder(new LineBorder(JUNGLE_WOOD_DARK, 1, true));
        lbl.setPreferredSize(new Dimension(0, 50));
        return lbl;
    }

    // --- LOGIC METHODS ---
    private void handleGenerateMap() {
        int cols = 30, rows = 20;
        String sizeSel = (String) cbMapSize.getSelectedItem();
        if (sizeSel.contains("Small")) { cols = 20; rows = 15; }
        else if (sizeSel.contains("Large")) { cols = 45; rows = 30; }

        double terrainProb = 0.0;
        String terrSel = (String) cbTerrainDensity.getSelectedItem();
        if (terrSel.contains("Light")) terrainProb = 0.2;
        else if (terrSel.contains("Heavy")) terrainProb = 0.5;

        double wallDens = 0.9;
        String wallSel = (String) cbWallDensity.getSelectedItem();
        if (wallSel.contains("High")) wallDens = 1.0;
        else if (wallSel.contains("Medium")) wallDens = 0.7;
        else if (wallSel.contains("Low")) wallDens = 0.4;

        mazeModel = new MazeGraphModel(cols, rows, terrainProb, wallDens);
        mazePanel.setMazeModel(mazeModel);

        runHistory.clear();
        statsDropdown.removeAllItems();
        clearInfoDisplay();
        lblEfficiencySummary.setText("<html><center>Map Generated!<br>Terrain: " + terrSel + "<br>Walls: " + wallSel + "</center></html>");
        rankingModel.setRowCount(0);
    }

    private void clearInfoDisplay() {
        updateCard(lblTime, "⏳ Execution Time", "-");
        updateCard(lblCost, "💎 Total Cost", "-");
        updateCard(lblVisited, "👣 Nodes Visited", "-");
    }

    private void updateCard(JLabel label, String title, String value) {
        label.setText("<html><div style='text-align:center; width:200px;'><b>" + title + "</b><br><font size='4'>" + value + "</font></div></html>");
    }

    private void showAlgoDetails(AlgoResult res) {
        updateCard(lblTime, "⏳ Execution Time", String.format("%.2f ms", res.getDurationMs()));

        // --- MODIFIKASI 1: Jika BFS/DFS, tampilkan "-" di Detail Card ---
        String costVal;
        if (res.algorithmName.equals("BFS") || res.algorithmName.equals("DFS")) {
            costVal = "-";
        } else {
            costVal = (res.totalCost >= Integer.MAX_VALUE/2) ? "Fail" : String.valueOf(res.totalCost);
        }
        updateCard(lblCost, "💎 Total Cost", costVal);

        updateCard(lblVisited, "👣 Nodes Visited", String.valueOf(res.visitedCount));
    }

    // --- UPDATED: ISI TABEL RANKING DENGAN LOGIKA NO-WEIGHT ---
    private void updateEfficiencySummary() {
        if (runHistory.isEmpty()) return;

        List<AlgoResult> sortedResults = new ArrayList<>(runHistory.values());

        // Sorting: Cost Rendah -> Waktu Cepat
        sortedResults.sort((a, b) -> {
            if (a.totalCost != b.totalCost) {
                return Integer.compare(a.totalCost, b.totalCost);
            }
            return Double.compare(a.durationNano, b.durationNano);
        });

        AlgoResult best = sortedResults.get(0);
        if (best.totalCost < Integer.MAX_VALUE/2) {
            lblEfficiencySummary.setText("<html><center>Best Strategy:<br><b style='color:#FFD700; font-size:14px'>" + best.algorithmName + "</b></center></html>");
        } else {
            lblEfficiencySummary.setText("<html><center>All Failed.</center></html>");
        }

        rankingModel.setRowCount(0);
        for (AlgoResult r : sortedResults) {
            String timeStr = String.format("%.2f ms", r.getDurationMs());

            // --- MODIFIKASI 2: Jika BFS/DFS, tampilkan "-" di Tabel ---
            String costStr;
            if (r.algorithmName.equals("BFS") || r.algorithmName.equals("DFS")) {
                costStr = "-";
            } else {
                costStr = (r.totalCost >= Integer.MAX_VALUE/2) ? "Fail" : String.valueOf(r.totalCost);
            }

            rankingModel.addRow(new Object[]{r.algorithmName, timeStr, costStr});
        }
    }

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

            if (algorithmCode.equals("BFS")) found = runBFS(start, end);
            else if (algorithmCode.equals("DFS")) found = runDFS(start, end);
            else if (algorithmCode.equals("DIJKSTRA")) found = (runDijkstra(start, end) != -1);
            else if (algorithmCode.equals("ASTAR")) found = (runAStar(start, end) != -1);

            long endTime = System.nanoTime();

            if (found) {
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

            for(int x=0; x<mazeModel.getCols(); x++) {
                for(int y=0; y<mazeModel.getRows(); y++) {
                    if(mazeModel.getGrid()[x][y].visited) visitedCount++;
                }
            }

            AlgoResult res = new AlgoResult(displayName, endTime - startTime, 0, visitedCount, found ? finalCost : Integer.MAX_VALUE);

            SwingUtilities.invokeLater(() -> {
                runHistory.put(displayName, res);
                boolean exists = false;
                for (int i = 0; i < statsDropdown.getItemCount(); i++) {
                    if (statsDropdown.getItemAt(i).equals(displayName)) { exists = true; break; }
                }
                if (!exists) statsDropdown.addItem(displayName);
                statsDropdown.setSelectedItem(displayName);

                updateEfficiencySummary();

                algorithmSelector.setEnabled(true);
                isAnimating = false;
            });
        }).start();
    }

    // --- ALGORITHMS ---
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