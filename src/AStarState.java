// Menyimpan state untuk A* (G Cost + H Cost)
public class AStarState implements Comparable<AStarState> {
    Node node;
    int gCost; // Cost riil dari start
    int hCost; // Heuristik: Estimasi jarak ke finish
    int fCost; // Total: g + h

    public AStarState(Node node, int gCost, int hCost) {
        this.node = node;
        this.gCost = gCost;
        this.hCost = hCost;
        this.fCost = gCost + hCost;
    }

    @Override
    public int compareTo(AStarState other) {
        // Prioritas utama: fCost terendah
        int compare = Integer.compare(this.fCost, other.fCost);
        // Tie-breaker: Jika fCost sama, pilih hCost terkecil (paling dekat target)
        if (compare == 0) {
            return Integer.compare(this.hCost, other.hCost);
        }
        return compare;
    }
}