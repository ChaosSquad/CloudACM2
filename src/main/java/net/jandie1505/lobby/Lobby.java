package net.jandie1505.lobby;

import net.jandie1505.CloudACM2;
import net.jandie1505.GamePart;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Lobby implements GamePart {
    private final CloudACM2 plugin;
    private int timeStep;
    private int time;
    private final Map<UUID, LobbyPlayerData> players;
    private final boolean enableBorder;
    private final int[] border;
    private final Location lobbySpawn;
    private final int gamemode;
    private final int mapPart;

    public Lobby(CloudACM2 plugin) {
        this.plugin = plugin;
        this.time = this.plugin.getConfigManager().getConfig().optInt("lobbyTime", 90);
        this.players = new HashMap<>();
        this.enableBorder = this.plugin.getConfigManager().getConfig().optJSONObject("border", new JSONObject()).optBoolean("enable", false);
        this.border = new int[]{
                this.plugin.getConfigManager().getConfig().optJSONObject("border", new JSONObject()).optInt("x1", -286),
                this.plugin.getConfigManager().getConfig().optJSONObject("border", new JSONObject()).optInt("y1", 65),
                this.plugin.getConfigManager().getConfig().optJSONObject("border", new JSONObject()).optInt("z1", -478),
                this.plugin.getConfigManager().getConfig().optJSONObject("border", new JSONObject()).optInt("x2", -280),
                this.plugin.getConfigManager().getConfig().optJSONObject("border", new JSONObject()).optInt("y2", -69),
                this.plugin.getConfigManager().getConfig().optJSONObject("border", new JSONObject()).optInt("z2", -472)
        };
        this.lobbySpawn = new Location(
                this.plugin.getServer().getWorlds().get(0),
                this.plugin.getConfigManager().getConfig().optJSONObject("spawn", new JSONObject()).optInt("x"),
                this.plugin.getConfigManager().getConfig().optJSONObject("spawn", new JSONObject()).optInt("y"),
                this.plugin.getConfigManager().getConfig().optJSONObject("spawn", new JSONObject()).optInt("z"),
                this.plugin.getConfigManager().getConfig().optJSONObject("spawn", new JSONObject()).optFloat("yaw"),
                this.plugin.getConfigManager().getConfig().optJSONObject("spawn", new JSONObject()).optFloat("pitch")
        );
        this.gamemode = this.plugin.getConfigManager().getConfig().optInt("gamemode", 0);
        this.mapPart = 0;
    }

    @Override
    public boolean tick() {

        // PLAYER MANAGEMENT

        for (UUID playerId : this.getPlayers().keySet()) {

            LobbyPlayerData playerData = this.players.get(playerId);
            Player player = this.plugin.getServer().getPlayer(playerId);

            // Check if player is online

            if (player == null || playerData == null) {
                this.players.remove(playerId);
                continue;
            }

            // Force adventure mode

            if ((player.getGameMode() != GameMode.ADVENTURE) && !this.plugin.isPlayerBypassing(playerId)) {
                player.setGameMode(GameMode.ADVENTURE);
            }

            // Health

            if (player.getHealth() < 20) {
                player.setHealth(20);
            }

            // Saturation

            if (player.getFoodLevel() < 20) {
                player.setFoodLevel(20);
            }

            // Actionbar

            if (this.players.size() >= 2) {

                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText("§aStarting in " + this.time + "s §8§l|§r§a Players: " + this.players.size() + " / 2"));

            } else {

                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText("§cNot enough players (" + this.players.size() + " / 2)"));

            }

            // Messages

            if ((this.time <= 5 || (this.time % 10 == 0)) && players.size() >= 2 && this.timeStep >= 1) {
                player.sendMessage("§7The game starts in " + this.time + " seconds");
            }

            // Lobby location

            if (!this.plugin.isPlayerBypassing(playerId) && this.enableBorder) {

                Location location = player.getLocation();

                if (!(location.getBlockX() >= this.border[0] && location.getBlockY() >= this.border[1] && location.getBlockZ() >= this.border[2] && location.getBlockX() <= this.border[3] && location.getBlockY() <= this.border[4] && location.getBlockZ() <= this.border[5])) {
                    player.teleport(this.lobbySpawn);
                }

            }

        }

        // TIME

        if (this.timeStep >= 1) {

            if (this.time < 0) {
                this.nextStatus();
            } else {
                this.time--;
            }

        }

        // TIME STEP

        if (this.timeStep >= 1) {
            this.timeStep = 0;
        } else {
            this.timeStep++;
        }

        return true;
    }

    private void nextStatus() {



    }

    public Map<UUID, LobbyPlayerData> getPlayers() {
        return Map.copyOf(this.players);
    }
}
