import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class MazeGraphModel {
    private final int cols;
    private final int rows;
    private Node[][] grid;
    private final Random random;

    // Parameter Kustomisasi
    private double terrainProbability;
    private double wallDensity;
    private String generationAlgo; // "Prim's" atau "Kruskal's"

    public MazeGraphModel(int cols, int rows, double terrainProb, double wallDensity, String genAlgo) {
        this.cols = cols;
        this.rows = rows;
        this.terrainProbability = terrainProb;
        this.wallDensity = wallDensity;
        this.generationAlgo = genAlgo;

        this.random = new Random();
        initializeGrid();
    }

    public Node[][] getGrid() { return grid; }
    public int getCols() { return cols; }
    public int getRows() { return rows; }

    private void initializeGrid() {
        grid = new Node[cols][rows];
        for (int x = 0; x < cols; x++) {
            for (int y = 0; y < rows; y++) {
                grid[x][y] = new Node(x, y);
                grid[x][y].terrain = TerrainType.TERRACE; // Default

                // Set Terrain Obstacles (Air/Lumpur)
                if (random.nextDouble() < terrainProbability) {
                    grid[x][y].terrain = TerrainType.getRandomObstacle();
                }

                // Start & End Wajib Aman
                if ((x == 0 && y == 0) || (x == cols - 1 && y == rows - 1)) {
                    grid[x][y].terrain = TerrainType.TERRACE;
                }
            }
        }

        // Pilih Algoritma Generasi
        if (generationAlgo.equals("Kruskal's")) {
            generateKruskal();
        } else {
            generatePrim(); // Default
        }

        // Terapkan Wall Density (Membuat Loop/Jalan Pintas)
        double extraPathFactor = (1.0 - wallDensity) * 0.4;
        addExtraPaths(extraPathFactor);

        resetVisited();
    }

    // --- 1. PRIM'S ALGORITHM (Tumbuh dari satu titik) ---
    private void generatePrim() {
        List<Edge> walls = new ArrayList<>();
        Node startNode = grid[0][0];
        startNode.visited = true;
        addNeighborsToWalls(startNode, walls);

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
    }

    // --- 2. KRUSKAL'S ALGORITHM (Menggabungkan set acak) ---
    private void generateKruskal() {
        List<Edge> allEdges = new ArrayList<>();

        // Kumpulkan semua kemungkinan dinding (Horizontal & Vertikal)
        for (int x = 0; x < cols; x++) {
            for (int y = 0; y < rows; y++) {
                if (x + 1 < cols) allEdges.add(new Edge(grid[x][y], grid[x+1][y])); // Kanan
                if (y + 1 < rows) allEdges.add(new Edge(grid[x][y], grid[x][y+1])); // Bawah
            }
        }

        // Acak urutan dinding
        Collections.shuffle(allEdges, random);

        // Disjoint Set Union (DSU) Structure
        int[] parent = new int[cols * rows];
        for (int i = 0; i < parent.length; i++) parent[i] = i;

        for (Edge edge : allEdges) {
            Node u = edge.source;
            Node v = edge.target;

            int idU = u.y * cols + u.x;
            int idV = v.y * cols + v.x;

            int rootU = findSet(parent, idU);
            int rootV = findSet(parent, idV);

            // Jika node u dan v berada di set berbeda, hubungkan (hancurkan dinding)
            if (rootU != rootV) {
                u.neighbors.add(v);
                v.neighbors.add(u);
                unionSet(parent, rootU, rootV);
            }
        }
    }

    // Helper DSU Find
    private int findSet(int[] parent, int i) {
        if (parent[i] == i) return i;
        return parent[i] = findSet(parent, parent[i]); // Path compression
    }

    // Helper DSU Union
    private void unionSet(int[] parent, int i, int j) {
        int rootI = findSet(parent, i);
        int rootJ = findSet(parent, j);
        if (rootI != rootJ) {
            parent[rootI] = rootJ;
        }
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
        if (factor <= 0) return;
        int totalCells = cols * rows;
        int numExtraPaths = (int) (totalCells * factor);
        int[][] directions = {{0, -1}, {0, 1}, {-1, 0}, {1, 0}};

        for (int i = 0; i < numExtraPaths; i++) {
            int x = random.nextInt(cols);
            int y = random.nextInt(rows);
            Node nodeA = grid[x][y];
            for (int attempt = 0; attempt < 10; attempt++) {
                int[] dir = directions[random.nextInt(directions.length)];
                int nx = x + dir[0];
                int ny = y + dir[1];
                if (nx >= 0 && nx < cols && ny >= 0 && ny < rows) {
                    Node nodeB = grid[nx][ny];
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