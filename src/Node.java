import java.util.ArrayList;
import java.util.List;

public class Node {
    int x, y;
    boolean visited;
    Node parent; // Field baru: Untuk menyimpan jejak (backtracking) jalur solusi
    List<Node> neighbors;

    public Node(int x, int y) {
        this.x = x;
        this.y = y;
        this.visited = false;
        this.parent = null;
        this.neighbors = new ArrayList<>();
    }
}