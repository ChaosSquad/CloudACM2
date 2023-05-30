package net.jandie1505.config;

import org.json.JSONObject;

public class DefaultConfigValues {

    public static JSONObject getConfig() {
        JSONObject config = new JSONObject();

        // reload


        config.put("world", "acm2");

        // border

        JSONObject borderConfig = new JSONObject();

        borderConfig.put("x1", -286);
        borderConfig.put("y1", 65);
        borderConfig.put("z1", -478);
        borderConfig.put("x2", -280);
        borderConfig.put("y2", -69);
        borderConfig.put("z2", -472);

        config.put("border", borderConfig);

        // return

        return config;
    }

}
