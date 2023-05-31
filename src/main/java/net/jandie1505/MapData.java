package net.jandie1505;

import java.util.List;

public final class MapData {

    public List<Integer> getRushMaps() {
        return List.of(400);
    }

    public List<Integer> getTDMMaps() {
        return List.of(
                100,
                201,
                301,
                401
        );
    }

    public List<Integer> getCTFMaps() {
        return List.of(
                100,
                200,
                300,
                400
        );
    }

    private MapData() {}
}
