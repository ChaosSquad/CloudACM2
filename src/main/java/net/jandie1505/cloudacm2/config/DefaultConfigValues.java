package net.jandie1505.cloudacm2.config;

import org.json.JSONObject;

public class DefaultConfigValues {

    public static JSONObject getConfig() {
        JSONObject config = new JSONObject();

        // reload

        config.put("lobbyTime", 90);
        config.put("datapack", "data_pack");
        config.put("gamemode", 0);
        config.put("mapVoting", false);

        // border

        JSONObject borderConfig = new JSONObject();

        borderConfig.put("enable", true);
        borderConfig.put("x1", -179);
        borderConfig.put("y1", 63);
        borderConfig.put("z1", -539);
        borderConfig.put("x2", -119);
        borderConfig.put("y2", 92);
        borderConfig.put("z2", -490);

        config.put("border", borderConfig);

        JSONObject lobbySpawn = new JSONObject();

        lobbySpawn.put("x", -149);
        lobbySpawn.put("y", 65);
        lobbySpawn.put("z", -492);
        lobbySpawn.put("yaw", -180);
        lobbySpawn.put("pitch", 0);

        config.put("spawn", lobbySpawn);

        // Cloudsystem

        JSONObject cloudSystemConfig = new JSONObject();

        cloudSystemConfig.put("enable", false);
        cloudSystemConfig.put("switchToIngameCommand", "");

        config.put("cloudSystemMode", cloudSystemConfig);

        // integrations

        JSONObject integrationsConfig = new JSONObject();

        integrationsConfig.put("cloudnet", true);
        integrationsConfig.put("supervanish-premiumvanish", true);

        config.put("integrations", integrationsConfig);

        // return

        return config;
    }

}
