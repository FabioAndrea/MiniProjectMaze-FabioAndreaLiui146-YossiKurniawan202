import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MazeGraphModel {
    private final int cols;
    private final int rows;
    private Node[][] grid;
    private final Random random;

    // Parameter Kustomisasi
    private double terrainProbability; // Peluang munculnya Air/Lumpur (0.0 - 1.0)
    private double wallDensity;        // 1.0 = Maze Sempurna, 0.0 = Open Field (banyak tembok hancur)

    public MazeGraphModel(int cols, int rows, double terrainProb, double wallDensity) {
        this.cols = cols;
        this.rows = rows;
        this.terrainProbability = terrainProb;
        this.wallDensity = wallDensity;

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

                // 1. Set Terrain (Lantai) berdasarkan terrainProbability
                // Default Terrace (Aman)
                grid[x][y].terrain = TerrainType.TERRACE;

                // Jika random < probabilitas, ubah jadi rintangan (Grass/Mud/Water)
                if (random.nextDouble() < terrainProbability) {
                    grid[x][y].terrain = TerrainType.getRandomObstacle();
                }

                // Start (0,0) & End (Max,Max) Wajib Terrace (Aman)
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

        // --- TAHAP 1: Prim's Algorithm (Membangun Struktur Utama) ---
        // Ini akan membuat maze "Sempurna" (Full Walls, 1 jalur ke mana saja)
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

        // --- TAHAP 2: Mengatur Wall Density (Menghancurkan Tembok Sisa) ---
        // Logika:
        // Wall Density 1.0 (High) -> Tidak ada tembok tambahan yang dihancurkan (Maze Sempurna).
        // Wall Density 0.0 (Low)  -> Banyak tembok dihancurkan (Jadi lapangan terbuka).

        // extraPathFactor semakin besar jika wallDensity semakin kecil
        double extraPathFactor = (1.0 - wallDensity) * 0.4; // Maksimal 40% dinding sisa dihapus

        addExtraPaths(extraPathFactor);

        // Reset visited agar siap untuk algoritma solving
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

    private void addExtraPaths(double factor) {
        int totalCells = cols * rows;
        int numExtraPaths = (int) (totalCells * factor);
        int[][] directions = {{0, -1}, {0, 1}, {-1, 0}, {1, 0}};

        for (int i = 0; i < numExtraPaths; i++) {
            int x = random.nextInt(cols);
            int y = random.nextInt(rows);
            Node nodeA = grid[x][y];

            // Coba hancurkan dinding ke tetangga acak
            for (int attempt = 0; attempt < 10; attempt++) {
                int[] dir = directions[random.nextInt(directions.length)];
                int nx = x + dir[0];
                int ny = y + dir[1];

                if (nx >= 0 && nx < cols && ny >= 0 && ny < rows) {
                    Node nodeB = grid[nx][ny];
                    // Jika ADA dinding (artinya belum bertetangga), kita hancurkan
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