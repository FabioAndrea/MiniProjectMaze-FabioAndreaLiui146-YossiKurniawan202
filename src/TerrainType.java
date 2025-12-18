import java.awt.Color;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public enum TerrainType {
    // 1. TERRACE: Hijau Terang (Jalan Utama/Lantai) - Cost 0
    TERRACE(0, new Color(144, 238, 144)),

    // 2. RINTANGAN (Obstacles)
    GRASS(1, new Color(34, 139, 34)),      // Hijau Hutan Tua (Cost 1)
    MUD(5, new Color(101, 67, 33)),        // Coklat Lumpur (Cost 5)
    WATER(10, new Color(0, 119, 190));     // Biru Laut (Cost 10 - Mahal)

    private final int cost;
    private final Color color;

    TerrainType(int cost, Color color) {
        this.cost = cost;
        this.color = color;
    }

    public int getCost() { return cost; }
    public Color getColor() { return color; }

    private static final List<TerrainType> VALUES = List.of(values());
    private static final List<TerrainType> OBSTACLES = List.of(GRASS, MUD, WATER);

    public static TerrainType getRandomTerrain() {
        return VALUES.get(ThreadLocalRandom.current().nextInt(VALUES.size()));
    }

    // Helper untuk mengambil rintangan saja (dipakai saat generate map)
    public static TerrainType getRandomObstacle() {
        return OBSTACLES.get(ThreadLocalRandom.current().nextInt(OBSTACLES.size()));
    }
}