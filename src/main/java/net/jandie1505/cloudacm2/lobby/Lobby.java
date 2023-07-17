package net.jandie1505.cloudacm2.lobby;

import net.jandie1505.cloudacm2.CloudACM2;
import net.jandie1505.cloudacm2.GamePart;
import net.jandie1505.cloudacm2.game.Game;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
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
    private final int requiredPlayers;
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
        this.requiredPlayers = this.plugin.getConfigManager().getConfig().optInt("requiredPlayers", 2);
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

            if (!this.plugin.isPlayerBypassing(player.getUniqueId())) {

                for (ItemStack item : Arrays.copyOf(player.getInventory().getContents(), player.getInventory().getContents().length)) {

                    if (item == null || item.getType() == Material.AIR) {
                        continue;
                    }

                    if (!item.isSimilar(this.getLobbyVoteHotbarButton()) && !item.isSimilar(this.getLobbyTeamSelectionHotbarButton())) {
                        player.getInventory().clear();
                    }

                }

                if (!player.getInventory().contains(this.getLobbyVoteHotbarButton())) {
                    player.getInventory().setItem(3, this.getLobbyVoteHotbarButton());
                }

                if (!player.getInventory().contains(this.getLobbyTeamSelectionHotbarButton())) {
                    player.getInventory().setItem(5, this.getLobbyTeamSelectionHotbarButton());
                }

            }

        }

        // Add players

        for (Player player : List.copyOf(this.plugin.getServer().getOnlinePlayers())) {

            if (this.plugin.isPlayerBypassing(player.getUniqueId())) {
                continue;
            }

            if (this.players.containsKey(player.getUniqueId())) {
                continue;
            }

            this.players.put(player.getUniqueId(), new LobbyPlayerData());

        }

        // TIME

        if (this.timeStep >= 1 && this.players.size() >= this.requiredPlayers) {

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

        for (UUID playerId : this.getPlayers().keySet()) {
            LobbyPlayerData lobbyPlayerData = this.players.get(playerId);

            if (lobbyPlayerData.getTeam() > 0) {
                continue;
            }

            int countOne = this.countTeamPlayers(1);
            int countTwo = this.countTeamPlayers(2);

            if (countOne < countTwo) {
                lobbyPlayerData.setTeam(countOne);
            } else if (countTwo < countOne) {
                lobbyPlayerData.setTeam(countTwo);
            } else {
                lobbyPlayerData.setTeam(new Random().nextInt(2) + 1);
            }

        }

        return new Game(
                this.plugin,
                this.gamemode,
                this.selectedMap.getId(),
                this.getPlayers()
        );
    }

    public CloudACM2 getPlugin() {
        return this.plugin;
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

    public boolean isMapVoting() {
        return this.mapVoting;
    }

    public boolean addPlayer(UUID playerId) {
        if (!this.players.containsKey(playerId)) {
            this.players.put(playerId, new LobbyPlayerData());
            return true;
        } else {
            return false;
        }
    }

    public boolean removePlayer(UUID playerId) {
        return this.players.remove(playerId) != null;
    }

    public ItemStack getLobbyVoteHotbarButton() {
        ItemStack item = new ItemStack(Material.MAP);
        ItemMeta meta = this.plugin.getServer().getItemFactory().getItemMeta(item.getType());

        meta.setDisplayName("§r§6Map Voting §r§7(right click)");

        List<String> lore = new ArrayList<>();
        lore.add(0, "1");
        meta.setLore(lore);

        item.setItemMeta(meta);

        return item;
    }

    public ItemStack getLobbyTeamSelectionHotbarButton() {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta meta = this.plugin.getServer().getItemFactory().getItemMeta(item.getType());

        meta.setDisplayName("§r§6Team Selection §r§7(right click)");

        List<String> lore = new ArrayList<>();
        lore.add(0, "2");
        meta.setLore(lore);

        item.setItemMeta(meta);

        return item;
    }

    public Location getLobbySpawn() {
        return this.lobbySpawn.clone();
    }

    public int countTeamPlayers(int teamScore) {

        int count = 0;

        for (UUID playerId : this.getPlayers().keySet()) {
            LobbyPlayerData lobbyPlayerData = this.players.get(playerId);

            if (lobbyPlayerData.getTeam() == teamScore) {
                count++;
            }

        }

        return count;
    }
}
