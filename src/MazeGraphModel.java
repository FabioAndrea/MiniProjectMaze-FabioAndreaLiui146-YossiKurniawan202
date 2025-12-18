import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MazeGraphModel {
    private final int cols;
    private final int rows;
    private Node[][] grid;
    private final Random random;

    // Konfigurasi Generasi
    private static final double EXTRA_PATHS_PERCENTAGE = 0.1; // 10% dinding tambahan dihapus (Multiple Paths)
    private static final double OBSTACLE_DENSITY = 0.4;       // 40% area adalah rintangan, sisanya Terrace

    public MazeGraphModel(int cols, int rows) {
        this.cols = cols;
        this.rows = rows;
        this.random = new Random();
        initializeGrid();
        generateMaze();
    }

    public Node[][] getGrid() { return grid; }
    public int getCols() { return cols; }
    public int getRows() { return rows; }

    private void initializeGrid() {
        grid = new Node[cols][rows];
        for (int x = 0; x < cols; x++) {
            for (int y = 0; y < rows; y++) {
                grid[x][y] = new Node(x, y);

                // Default: Terrace
                grid[x][y].terrain = TerrainType.TERRACE;

                // Random Obstacles
                if (random.nextDouble() < OBSTACLE_DENSITY) {
                    grid[x][y].terrain = TerrainType.getRandomObstacle();
                }

                // Start & End wajib aman (Terrace)
                if ((x == 0 && y == 0) || (x == cols - 1 && y == rows - 1)) {
                    grid[x][y].terrain = TerrainType.TERRACE;
                }
            }
        }
    }

    public void generateMaze() {
        initializeGrid();
        List<Edge> walls = new ArrayList<>();
        Node startNode = grid[0][0];
        startNode.visited = true;
        addNeighborsToWalls(startNode, walls);

        // 1. Prim's Algorithm (Perfect Maze)
        while (!walls.isEmpty()) {
            int randomIndex = random.nextInt(walls.size());
            Edge currentEdge = walls.remove(randomIndex);
            Node u = currentEdge.source;
            Node v = currentEdge.target;

            if (!v.visited) {
                u.neighbors.add(v);
                v.neighbors.add(u);
                v.visited = true;
                addNeighborsToWalls(v, walls);
            }
        }

        // 2. Add Extra Paths (Membuat Multiple Paths/Loops)
        addExtraPaths();

        // 3. Reset untuk persiapan solving
        resetVisited();
    }

    public void resetVisited() {
        for (int x = 0; x < cols; x++) {
            for (int y = 0; y < rows; y++) {
                grid[x][y].visited = false;
                grid[x][y].parent = null;
            }
        }
    }

    private void addNeighborsToWalls(Node node, List<Edge> walls) {
        int[][] directions = {{0, -1}, {0, 1}, {-1, 0}, {1, 0}};
        for (int[] dir : directions) {
            int nx = node.x + dir[0];
            int ny = node.y + dir[1];
            if (nx >= 0 && nx < cols && ny >= 0 && ny < rows) {
                Node neighbor = grid[nx][ny];
                if (!neighbor.visited) {
                    walls.add(new Edge(node, neighbor));
                }
            }
        }
    }

    private void addExtraPaths() {
        int totalCells = cols * rows;
        int numExtraPaths = (int) (totalCells * EXTRA_PATHS_PERCENTAGE);
        int[][] directions = {{0, -1}, {0, 1}, {-1, 0}, {1, 0}};

        for (int i = 0; i < numExtraPaths; i++) {
            int x = random.nextInt(cols);
            int y = random.nextInt(rows);
            Node nodeA = grid[x][y];

            // Mencoba menghancurkan dinding acak
            for (int attempt = 0; attempt < 10; attempt++) {
                int[] dir = directions[random.nextInt(directions.length)];
                int nx = x + dir[0];
                int ny = y + dir[1];
                if (nx >= 0 && nx < cols && ny >= 0 && ny < rows) {
                    Node nodeB = grid[nx][ny];
                    // Jika ada dinding (belum tetangga), hancurkan
                    if (!nodeA.neighbors.contains(nodeB)) {
                        nodeA.neighbors.add(nodeB);
                        nodeB.neighbors.add(nodeA);
                        break;
                    }
                }
            }
        }
    }
}