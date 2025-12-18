import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class MazePanel extends JPanel {
    private static final int BASE_CELL_SIZE = 50;

    private MazeGraphModel mazeModel;

    // Animasi (Kuning)
    private List<Node> exploredNodes = new ArrayList<>();

    // Penyimpanan Multi-Path untuk Compare Mode
    private Map<String, PathLayer> pathLayers = new LinkedHashMap<>();

    private Random visualRandom = new Random();

    private static class PathLayer {
        List<Node> nodes;
        Color color;
        public PathLayer(List<Node> nodes, Color color) {
            this.nodes = nodes;
            this.color = color;
        }
    }

    public MazePanel(MazeGraphModel model) {
        this.mazeModel = model;
        this.setOpaque(false);
    }

    public void setMazeModel(MazeGraphModel model) {
        this.mazeModel = model;
        clearExplored();
        clearAllPaths();
        repaint();
    }

    public void clearExplored() {
        SwingUtilities.invokeLater(() -> {
            exploredNodes.clear();
            repaint();
        });
    }

    public void clearAllPaths() {
        SwingUtilities.invokeLater(() -> {
            pathLayers.clear();
            repaint();
        });
    }

    public void addExploredNode(Node n) {
        SwingUtilities.invokeLater(() -> {
            exploredNodes.add(n);
            repaint();
        });
    }

    public void addFinalPath(String algoName, List<Node> path, Color color) {
        SwingUtilities.invokeLater(() -> {
            pathLayers.put(algoName, new PathLayer(path, color));
            repaint();
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (mazeModel == null || getWidth() <= 0 || getHeight() <= 0) return;

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

        Node[][] grid = mazeModel.getGrid();
        int cols = mazeModel.getCols();
        int rows = mazeModel.getRows();

        // --- SCALING LOGIC ---
        AffineTransform oldTransform = g2d.getTransform();
        double contentWidth = cols * BASE_CELL_SIZE;
        double contentHeight = rows * BASE_CELL_SIZE;
        double padding = 40;
        double availableWidth = getWidth() - padding;
        double availableHeight = getHeight() - padding;
        double scaleFactor = Math.min(availableWidth / contentWidth, availableHeight / contentHeight);
        double offsetX = (getWidth() - (contentWidth * scaleFactor)) / 2.0;
        double offsetY = (getHeight() - (contentHeight * scaleFactor)) / 2.0;

        g2d.translate(offsetX, offsetY);
        g2d.scale(scaleFactor, scaleFactor);

        // 1. TERRAIN
        for (int x = 0; x < cols; x++) {
            for (int y = 0; y < rows; y++) {
                drawTerrain(g2d, grid[x][y].terrain, x * BASE_CELL_SIZE, y * BASE_CELL_SIZE);
            }
        }

        // 2. ANIMASI (Kuning Transparan)
        g2d.setColor(new Color(255, 230, 0, 150));
        List<Node> currentExplored;
        synchronized(exploredNodes) { currentExplored = new ArrayList<>(exploredNodes); }
        for (Node n : currentExplored) {
            g2d.fillRect(n.x * BASE_CELL_SIZE, n.y * BASE_CELL_SIZE, BASE_CELL_SIZE, BASE_CELL_SIZE);
        }

        // 3. MULTI-PATH RENDERING (KEMBALI KE KOTAK FULL)
        // Kita tidak menggunakan setStroke lagi karena kita menggambar kotak penuh
        for (PathLayer layer : pathLayers.values()) {
            g2d.setColor(layer.color); // Warna sudah mengandung transparansi dari MazeApp
            for (Node n : layer.nodes) {
                // KEMBALI KE KOTAK FULL
                g2d.fillRect(n.x * BASE_CELL_SIZE, n.y * BASE_CELL_SIZE, BASE_CELL_SIZE, BASE_CELL_SIZE);
            }
        }

        // 4. WALLS
        float wallThick = Math.max(1.5f, (float)(3.0 / scaleFactor));
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(wallThick, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER));

        for (int x = 0; x < cols; x++) {
            for (int y = 0; y < rows; y++) {
                Node current = grid[x][y];
                int px = x * BASE_CELL_SIZE;
                int py = y * BASE_CELL_SIZE;

                if (y == 0 || !current.neighbors.contains(grid[x][y - 1]))
                    g2d.drawLine(px, py, px + BASE_CELL_SIZE, py);
                if (y == rows - 1 || !current.neighbors.contains(grid[x][y + 1]))
                    g2d.drawLine(px, py + BASE_CELL_SIZE, px + BASE_CELL_SIZE, py + BASE_CELL_SIZE);
                if (x == 0 || !current.neighbors.contains(grid[x - 1][y]))
                    g2d.drawLine(px, py, px, py + BASE_CELL_SIZE);
                if (x == cols - 1 || !current.neighbors.contains(grid[x + 1][y]))
                    g2d.drawLine(px + BASE_CELL_SIZE, py, px + BASE_CELL_SIZE, py + BASE_CELL_SIZE);
            }
        }

        // 5. START & END MARKERS
        g2d.setColor(new Color(0, 255, 0));
        g2d.fillRect(5, 5, BASE_CELL_SIZE - 10, BASE_CELL_SIZE - 10);
        g2d.setColor(new Color(139, 0, 0));
        g2d.fillRect((cols - 1) * BASE_CELL_SIZE + 5, (rows - 1) * BASE_CELL_SIZE + 5, BASE_CELL_SIZE - 10, BASE_CELL_SIZE - 10);

        g2d.setTransform(oldTransform);
    }

    private void drawTerrain(Graphics2D g2d, TerrainType type, int x, int y) {
        switch (type) {
            case TERRACE:
                g2d.setColor(type.getColor());
                g2d.fillRect(x, y, BASE_CELL_SIZE, BASE_CELL_SIZE);
                break;
            case GRASS:
                g2d.setColor(type.getColor());
                g2d.fillRect(x, y, BASE_CELL_SIZE, BASE_CELL_SIZE);
                g2d.setColor(new Color(50, 205, 50));
                visualRandom.setSeed(x * 12345L + y);
                g2d.drawLine(x+5, y+5, x+10, y+15);
                break;
            case MUD:
                g2d.setColor(type.getColor());
                g2d.fillRect(x, y, BASE_CELL_SIZE, BASE_CELL_SIZE);
                g2d.setColor(new Color(80, 50, 20));
                visualRandom.setSeed(x * 98765L + y);
                g2d.fillOval(x + 10, y + 10, 8, 8);
                break;
            case WATER:
                GradientPaint waterGrad = new GradientPaint(x, y, new Color(0, 119, 190), x + BASE_CELL_SIZE, y + BASE_CELL_SIZE, new Color(72, 209, 204));
                g2d.setPaint(waterGrad);
                g2d.fillRect(x, y, BASE_CELL_SIZE, BASE_CELL_SIZE);
                break;
        }
    }
}