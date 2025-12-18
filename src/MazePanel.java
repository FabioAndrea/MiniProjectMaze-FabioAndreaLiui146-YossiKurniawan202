import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class MazePanel extends JPanel {
    private final int CELL_SIZE = 20;
    private MazeGraphModel mazeModel;

    // List untuk menyimpan animasi
    private List<Node> exploredNodes = new ArrayList<>(); // Node yang sedang dicek (Kuning)
    private List<Node> finalPath = new ArrayList<>();     // Jalur solusi (Biru)

    public MazePanel(MazeGraphModel model) {
        this.mazeModel = model;
        int width = model.getCols() * CELL_SIZE + 1;
        int height = model.getRows() * CELL_SIZE + 1;
        this.setPreferredSize(new Dimension(width, height));
        this.setBackground(Color.WHITE);
    }

    public void setMazeModel(MazeGraphModel model) {
        this.mazeModel = model;
        clearPath(); // Bersihkan visualisasi lama saat generate baru
        repaint();
    }

    // Reset visualisasi
    public void clearPath() {
        exploredNodes.clear();
        finalPath.clear();
        repaint();
    }

    // Method untuk menambah node yang sedang dieksplorasi (dipanggil saat animasi)
    public void addExploredNode(Node n) {
        exploredNodes.add(n);
        repaint();
    }

    // Method untuk set jalur final
    public void setFinalPath(List<Node> path) {
        this.finalPath = path;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setStroke(new BasicStroke(2));

        // 1. Gambar Node yang sedang dieksplorasi (KUNING)
        g2d.setColor(new Color(255, 255, 153)); // Kuning muda
        for (Node n : exploredNodes) {
            g2d.fillRect(n.x * CELL_SIZE, n.y * CELL_SIZE, CELL_SIZE, CELL_SIZE);
        }

        // 2. Gambar Jalur Final (BIRU)
        g2d.setColor(new Color(100, 149, 237)); // Cornflower Blue
        for (Node n : finalPath) {
            g2d.fillRect(n.x * CELL_SIZE, n.y * CELL_SIZE, CELL_SIZE, CELL_SIZE);
        }

        // 3. Gambar Start (Hijau) dan End (Merah)
        g2d.setColor(Color.GREEN);
        g2d.fillRect(0, 0, CELL_SIZE, CELL_SIZE);

        g2d.setColor(Color.RED);
        g2d.fillRect((mazeModel.getCols() - 1) * CELL_SIZE, (mazeModel.getRows() - 1) * CELL_SIZE, CELL_SIZE, CELL_SIZE);

        // 4. Gambar Dinding (Hitam)
        g2d.setColor(Color.BLACK);
        Node[][] grid = mazeModel.getGrid();
        int cols = mazeModel.getCols();
        int rows = mazeModel.getRows();

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
    }
}