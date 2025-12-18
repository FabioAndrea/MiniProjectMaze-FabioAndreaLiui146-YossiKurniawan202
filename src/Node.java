import java.util.ArrayList;
import java.util.List;

public class Node {
    int x, y;
    boolean visited;
    Node parent;
    List<Node> neighbors;
    TerrainType terrain;

    public Node(int x, int y) {
        this.x = x;
        this.y = y;
        this.visited = false;
        this.parent = null;
        this.neighbors = new ArrayList<>();
        this.terrain = TerrainType.TERRACE;
    }

    public int getCost() {
        return terrain.getCost();
    }
}