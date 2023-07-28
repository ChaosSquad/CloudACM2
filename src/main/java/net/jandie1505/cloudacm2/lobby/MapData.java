package net.jandie1505.cloudacm2.lobby;

import java.util.ArrayList;
import java.util.List;

public class MapData {
    private static final List<MapData> rushMaps;
    private static final List<MapData> TDMMaps;
    private static final List<MapData> CTFMaps;
    private final int id;
    private final int gamemode;
    private String name;

    static {

        rushMaps = List.of(
                new MapData(400, 2, "Old village")
        );

        TDMMaps = List.of(
                new MapData(100, 4, "Central Island"),
                //new MapData(201, 4, "School"),
                new MapData(301, 4, "Modern Village"),
                new MapData(401, 4, "Old village")
        );

        CTFMaps = List.of(
                //new MapData(100, 5, "Central Island"),
                //new MapData(200, 5, "School"),
                new MapData(300, 5, "Modern Village"),
                new MapData(400, 5, "Old village")
        );

    }

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
        return List.copyOf(rushMaps);
    }

    public static List<MapData> getTDMMaps() {
        return List.copyOf(TDMMaps);
    }

    public static List<MapData> getCTFMaps() {
        return List.copyOf(CTFMaps);
    }
}
