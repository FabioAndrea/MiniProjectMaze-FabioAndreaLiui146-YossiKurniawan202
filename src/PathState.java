// Menyimpan state node dan cost akumulatif untuk Dijkstra
public class PathState implements Comparable<PathState> {
    Node node;
    int cost; // Total biaya dari start

    public PathState(Node node, int cost) {
        this.node = node;
        this.cost = cost;
    }

    // Urutkan dari cost terendah ke tertinggi
    @Override
    public int compareTo(PathState other) {
        return Integer.compare(this.cost, other.cost);
    }
}