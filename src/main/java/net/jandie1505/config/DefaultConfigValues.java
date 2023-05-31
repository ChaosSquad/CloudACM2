package net.jandie1505.config;

import org.json.JSONObject;

public class DefaultConfigValues {

    public static JSONObject getConfig() {
        JSONObject config = new JSONObject();

        // reload

        config.put("lobbyTime", 90);
        config.put("world", "acm2");
        config.put("gamemode", 0);
        config.put("mapVoting", false);

        // border

        JSONObject borderConfig = new JSONObject();

        borderConfig.put("enable", true);
        borderConfig.put("x1", -286);
        borderConfig.put("y1", 65);
        borderConfig.put("z1", -478);
        borderConfig.put("x2", -280);
        borderConfig.put("y2", -69);
        borderConfig.put("z2", -472);

        config.put("border", borderConfig);

        JSONObject lobbySpawn = new JSONObject();

        lobbySpawn.put("x", -285);
        lobbySpawn.put("y", 64);
        lobbySpawn.put("z", -477);
        lobbySpawn.put("yaw", -90);
        lobbySpawn.put("pitch", 0);

        config.put("spawn", lobbySpawn);

        // return

        return config;
    }

}
