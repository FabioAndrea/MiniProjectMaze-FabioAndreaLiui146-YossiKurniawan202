import java.awt.Color;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public enum TerrainType {
    // 1. TERRACE: Hijau Terang (Solid) - Menjadi lantai maze
    TERRACE(0, new Color(144, 238, 144)),

    // 2. RINTANGAN (Obstacles)
    GRASS(1, new Color(34, 139, 34)),      // Hijau Hutan Tua
    MUD(5, new Color(101, 67, 33)),        // Coklat Lumpur
    WATER(10, new Color(0, 119, 190));     // Biru Laut

    private final int cost;
    private final Color color;

    TerrainType(int cost, Color color) {
        this.cost = cost;
        this.color = color;
    }

    public int getCost() { return cost; }
    public Color getColor() { return color; }

    // Daftar semua
    private static final List<TerrainType> VALUES = List.of(values());

    // Daftar KHUSUS rintangan (dipakai oleh MazeGraphModel)
    private static final List<TerrainType> OBSTACLES = List.of(GRASS, MUD, WATER);

    public static TerrainType getRandomTerrain() {
        return VALUES.get(ThreadLocalRandom.current().nextInt(VALUES.size()));
    }

    // Method Helper PENTING agar tidak error "cannot find symbol"
    public static TerrainType getRandomObstacle() {
        return OBSTACLES.get(ThreadLocalRandom.current().nextInt(OBSTACLES.size()));
    }
}