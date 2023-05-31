package net.jandie1505.lobby;

import net.jandie1505.CloudACM2;
import net.jandie1505.GamePart;
import net.jandie1505.game.Game;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.json.JSONObject;

import java.util.*;

public class Lobby implements GamePart {
    private final CloudACM2 plugin;
    private int timeStep;
    private int time;
    private final Map<UUID, LobbyPlayerData> players;
    private final boolean enableBorder;
    private final int[] border;
    private final Location lobbySpawn;
    private boolean mapVoting;
    private int gamemode;
    private MapData selectedMap;

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
        this.selectedMap = null;
        this.mapVoting = this.plugin.getConfigManager().getConfig().optBoolean("mapVoting", false);
    }

    @Override
    public boolean tick() {

        // SELECT MAP

        if (this.selectedMap == null && this.time <= 10) {
            this.autoSelectMap();
            this.displayMap();
        }

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
                this.plugin.nextStatus();
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

    public List<MapData> getMaps() {

        switch (this.gamemode) {
            case 2:
                return MapData.getRushMaps();
            case 4:
                return MapData.getTDMMaps();
            case 5:
                return MapData.getCTFMaps();
            default:
                return List.of();
        }
    }

    private List<MapData> getHighestVotedMaps() {

        // Get map votes

        Map<MapData, Integer> mapVotes = new HashMap<>();

        for (UUID playerId : this.getPlayers().keySet()) {
            LobbyPlayerData playerData = this.players.get(playerId);

            if (playerData.getVote() == null) {
                continue;
            }

            if (mapVotes.containsKey(playerData.getVote())) {
                mapVotes.put(playerData.getVote(), mapVotes.get(playerData.getVote()) + 1);
            } else {
                mapVotes.put(playerData.getVote(), 1);
            }

        }

        // Get list of maps with the highest vote count

        List<MapData> highestVotedMaps = new ArrayList<>();
        int maxVotes = Integer.MIN_VALUE;

        for (Map.Entry<MapData, Integer> entry : mapVotes.entrySet()) {
            int votes = entry.getValue();
            if (votes > maxVotes) {
                maxVotes = votes;
                highestVotedMaps.clear();
                highestVotedMaps.add(entry.getKey());
            } else if (votes == maxVotes) {
                highestVotedMaps.add(entry.getKey());
            }
        }

        return highestVotedMaps;

    }

    private void autoSelectMap() {

        MapData selectedMap = null;

        if (this.mapVoting) {

            List<MapData> highestVotedMaps = this.getHighestVotedMaps();

            if (!highestVotedMaps.isEmpty()) {

                selectedMap = highestVotedMaps.get(new Random().nextInt(highestVotedMaps.size()));

            }

        }

        if (selectedMap == null) {

            if (!this.getMaps().isEmpty()) {

                selectedMap = this.getMaps().get(new Random().nextInt(this.getMaps().size()));

            }

        }

        this.selectedMap = selectedMap;

    }

    private void displayMap() {

        for (UUID playerId : this.getPlayers().keySet()) {
            Player player = this.plugin.getServer().getPlayer(playerId);

            if (player == null) {
                continue;
            }

            if (this.selectedMap == null) {
                return;
            }

            player.sendMessage("§bThe map has been set to " + this.selectedMap.getName());

        }

    }

    @Override
    public GamePart getNextStatus() {

        if (this.selectedMap == null) {
            this.autoSelectMap();
            this.displayMap();
        }

        if (selectedMap == null) {
            this.plugin.getLogger().warning("Game stopped because no world was selected");
            this.plugin.stopGame();
            return null;
        }

        World world = this.plugin.loadWorld(this.plugin.getConfigManager().getConfig().optString("world", "acm2"));

        if (world == null || !this.plugin.getServer().getWorlds().contains(world)) {
            this.plugin.getLogger().warning("Game stopped because world does not exist");
            return null;
        }

        if (world == this.plugin.getServer().getWorlds().get(0)) {
            this.plugin.getLogger().warning("Game stopped because selected world is default world on server");
            return null;
        }

        world.setAutoSave(false);

        return new Game(this.plugin, world);
    }

    public Map<UUID, LobbyPlayerData> getPlayers() {
        return Map.copyOf(this.players);
    }

    public MapData getSelectedMap() {
        return this.selectedMap;
    }

    public void selectMap(MapData mapData) {
        this.selectedMap = mapData;
    }
}
