import java.util.ArrayList;
import java.util.List;

public class Node {
    int x, y;
    boolean visited; // Helper untuk algoritma generasi
    List<Node> neighbors; // Daftar node yang terhubung (tidak ada dinding diantaranya)

    public Node(int x, int y) {
        this.x = x;
        this.y = y;
        this.visited = false;
        this.neighbors = new ArrayList<>();
    }
}