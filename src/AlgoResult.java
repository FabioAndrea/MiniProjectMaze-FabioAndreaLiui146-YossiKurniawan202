public class AlgoResult {
    String algorithmName;
    long durationNano;
    int pathLength;
    int visitedCount;
    int totalCost;

    public AlgoResult(String name, long time, int len, int visited, int cost) {
        this.algorithmName = name;
        this.durationNano = time;
        this.pathLength = len;
        this.visitedCount = visited;
        this.totalCost = cost;
    }

    public double getDurationMs() {
        return durationNano / 1_000_000.0;
    }
}