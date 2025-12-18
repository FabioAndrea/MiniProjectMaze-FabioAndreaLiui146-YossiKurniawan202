import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MazeGraphModel {
    private final int cols;
    private final int rows;
    private Node[][] grid;
    private final Random random;

    // Berapa persen dinding tambahan yang dihapus untuk membuat multiple path
    private static final double EXTRA_PATHS_PERCENTAGE = 0.1; // 10% dinding tambahan dihapus

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
            }
        }
    }

    public void generateMaze() {
        // 1. Reset Grid
        initializeGrid();

        // 2. Prim's Algorithm (Menghasilkan Perfect Maze - 1 solusi)
        List<Edge> walls = new ArrayList<>();

        // Mulai dari pojok kiri atas
        Node startNode = grid[0][0];
        startNode.visited = true;

        // Masukkan dinding tetangga awal ke daftar
        addNeighborsToWalls(startNode, walls);

        while (!walls.isEmpty()) {
            // Ambil dinding secara acak
            int randomIndex = random.nextInt(walls.size());
            Edge currentEdge = walls.remove(randomIndex);

            Node u = currentEdge.source;
            Node v = currentEdge.target;

            if (!v.visited) {
                // Jebol dinding (buat koneksi graph)
                u.neighbors.add(v);
                v.neighbors.add(u);

                v.visited = true;
                addNeighborsToWalls(v, walls);
            }
        }

        // 3. Tambahkan Multiple Paths (Sesuai requirement Step 1)
        // Kita akan menghapus beberapa dinding tambahan secara acak untuk membuat loops
        addExtraPaths();
    }

    private void addNeighborsToWalls(Node node, List<Edge> walls) {
        int[][] directions = {{0, -1}, {0, 1}, {-1, 0}, {1, 0}}; // Atas, Bawah, Kiri, Kanan

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
            // Pilih node acak
            int x = random.nextInt(cols);
            int y = random.nextInt(rows);
            Node nodeA = grid[x][y];

            // Cari tetangga yang valid tapi BELUM terhubung (ada dinding)
            // Lakukan beberapa kali percobaan agar tidak infinite loop
            for (int attempt = 0; attempt < 10; attempt++) {
                int[] dir = directions[random.nextInt(directions.length)];
                int nx = x + dir[0];
                int ny = y + dir[1];

                if (nx >= 0 && nx < cols && ny >= 0 && ny < rows) {
                    Node nodeB = grid[nx][ny];
                    // Jika belum terhubung, hubungkan (hapus dinding)
                    if (!nodeA.neighbors.contains(nodeB)) {
                        nodeA.neighbors.add(nodeB);
                        nodeB.neighbors.add(nodeA);
                        break; // Pindah ke path ekstra berikutnya
                    }
                }
            }
        }
    }
}