package net.jandie1505.lobby;

import java.util.List;

public class MapData {
    private final int id;
    private final int gamemode;
    private String name;

    public MapData(int id, int gamemode, String name) {
        this.id = id;
        this.gamemode = gamemode;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public int getGamemode() {
        return gamemode;
    }

    public String getName() {
        return name;
    }

    public static List<MapData> getRushMaps() {
        return List.of(
                new MapData(400, 2, "Old village")
        );
    }

    public static List<MapData> getTDMMaps() {
        return List.of(
                new MapData(100, 4, "Central Island"),
                new MapData(201, 4, "School"),
                new MapData(301, 4, "Modern Village"),
                new MapData(401, 4, "Old village")
        );
    }

    public static List<MapData> getCTFMaps() {
        return List.of(
                new MapData(100, 5, "Central Island"),
                new MapData(200, 5, "School"),
                new MapData(300, 5, "Modern Village"),
                new MapData(400, 5, "Old village")
        );
    }
}
