import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MazePanel extends JPanel {
    private final int CELL_SIZE = 30;
    private MazeGraphModel mazeModel;

    private List<Node> exploredNodes = new ArrayList<>();
    private List<Node> finalPath = new ArrayList<>();
    private Random visualRandom = new Random();

    public MazePanel(MazeGraphModel model) {
        this.mazeModel = model;
        int width = model.getCols() * CELL_SIZE + 1;
        int height = model.getRows() * CELL_SIZE + 1;
        this.setPreferredSize(new Dimension(width, height));

        // Transparan agar background di Wrapper (MazeApp) terlihat di pinggiran
        this.setOpaque(false);
    }

    public void setMazeModel(MazeGraphModel model) {
        this.mazeModel = model;
        clearPath();
        repaint();
    }

    public void clearPath() {
        SwingUtilities.invokeLater(() -> {
            exploredNodes.clear();
            finalPath.clear();
            repaint();
        });
    }

    public void addExploredNode(Node n) {
        SwingUtilities.invokeLater(() -> {
            exploredNodes.add(n);
            repaint();
        });
    }

    public void setFinalPath(List<Node> path) {
        SwingUtilities.invokeLater(() -> {
            this.finalPath = path;
            repaint();
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Node[][] grid = mazeModel.getGrid();
        int cols = mazeModel.getCols();
        int rows = mazeModel.getRows();

        // 1. TERRAIN LAYERS
        for (int x = 0; x < cols; x++) {
            for (int y = 0; y < rows; y++) {
                drawTerrain(g2d, grid[x][y].terrain, x * CELL_SIZE, y * CELL_SIZE);
            }
        }

        // 2. GAMBAR ANIMASI (Kuning)
        g2d.setColor(new Color(255, 230, 0, 170));
        List<Node> currentExplored;
        synchronized(exploredNodes) { currentExplored = new ArrayList<>(exploredNodes); }
        for (Node n : currentExplored) {
            g2d.fillRect(n.x * CELL_SIZE, n.y * CELL_SIZE, CELL_SIZE, CELL_SIZE);
        }

        // 3. FINAL PATH (Merah)
        g2d.setColor(new Color(255, 0, 0, 180));
        for (Node n : finalPath) {
            g2d.fillRect(n.x * CELL_SIZE, n.y * CELL_SIZE, CELL_SIZE, CELL_SIZE);
        }

        // 4. WALLS
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(4.0f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER));

        for (int x = 0; x < cols; x++) {
            for (int y = 0; y < rows; y++) {
                Node current = grid[x][y];
                int px = x * CELL_SIZE;
                int py = y * CELL_SIZE;

                if (y == 0 || !current.neighbors.contains(grid[x][y - 1]))
                    g2d.drawLine(px, py, px + CELL_SIZE, py);
                if (y == rows - 1 || !current.neighbors.contains(grid[x][y + 1]))
                    g2d.drawLine(px, py + CELL_SIZE, px + CELL_SIZE, py + CELL_SIZE);
                if (x == 0 || !current.neighbors.contains(grid[x - 1][y]))
                    g2d.drawLine(px, py, px, py + CELL_SIZE);
                if (x == cols - 1 || !current.neighbors.contains(grid[x + 1][y]))
                    g2d.drawLine(px + CELL_SIZE, py, px + CELL_SIZE, py + CELL_SIZE);
            }
        }

        // 5. START & END MARKERS
        g2d.setColor(new Color(0, 255, 0));
        g2d.fillRect(5, 5, CELL_SIZE - 10, CELL_SIZE - 10);

        g2d.setColor(new Color(139, 0, 0));
        g2d.fillRect((cols - 1) * CELL_SIZE + 5, (rows - 1) * CELL_SIZE + 5, CELL_SIZE - 10, CELL_SIZE - 10);
    }

    private void drawTerrain(Graphics2D g2d, TerrainType type, int x, int y) {
        switch (type) {
            case TERRACE:
                // PENTING: Gambar warna hijau terang solid
                g2d.setColor(type.getColor());
                g2d.fillRect(x, y, CELL_SIZE, CELL_SIZE);
                break;
            case GRASS:
                g2d.setColor(type.getColor());
                g2d.fillRect(x, y, CELL_SIZE, CELL_SIZE);
                g2d.setColor(new Color(50, 205, 50));
                visualRandom.setSeed(x * 12345L + y);
                for(int i=0; i<3; i++) {
                    int rx = x + visualRandom.nextInt(CELL_SIZE - 5);
                    int ry = y + visualRandom.nextInt(CELL_SIZE - 5);
                    g2d.drawLine(rx, ry, rx+3, ry-4);
                }
                break;
            case MUD:
                g2d.setColor(type.getColor());
                g2d.fillRect(x, y, CELL_SIZE, CELL_SIZE);
                g2d.setColor(new Color(80, 50, 20));
                visualRandom.setSeed(x * 98765L + y);
                g2d.fillOval(x + visualRandom.nextInt(CELL_SIZE/2), y + visualRandom.nextInt(CELL_SIZE/2), 6, 6);
                break;
            case WATER:
                GradientPaint waterGrad = new GradientPaint(x, y, new Color(0, 119, 190), x + CELL_SIZE, y + CELL_SIZE, new Color(72, 209, 204));
                g2d.setPaint(waterGrad);
                g2d.fillRect(x, y, CELL_SIZE, CELL_SIZE);
                g2d.setColor(new Color(255, 255, 255, 80));
                g2d.drawArc(x + 5, y + 5, CELL_SIZE - 10, 10, 0, -180);
                break;
        }
    }
}