package net.jandie1505.cloudacm2.game;

import net.jandie1505.cloudacm2.CloudACM2;
import net.jandie1505.cloudacm2.GamePart;
import net.jandie1505.cloudacm2.lobby.LobbyPlayerData;
import net.jandie1505.cloudacm2.map.ACM2GameMode;
import net.jandie1505.cloudacm2.map.ACM2GameState;
import net.jandie1505.cloudacm2.map.ACM2PlayerState;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Score;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Game implements GamePart {
    private final CloudACM2 plugin;
    private boolean killswitch;
    private final int gamemode;
    private final int selectedMap;
    private final Map<UUID, PlayerData> players;
    private int mapState;
    private int mapTimer;

    public Game(CloudACM2 plugin, int gamemode, int selectedMap, Map<UUID, LobbyPlayerData> players) {
        this.plugin = plugin;
        this.killswitch = false;
        this.gamemode = gamemode;
        this.selectedMap = selectedMap;
        this.players = new HashMap<>();
        this.mapState = 0;
        this.mapTimer = 0;

        for (UUID playerId : players.keySet()) {
            LobbyPlayerData lobbyPlayerData = players.get(playerId);
            Player player = this.plugin.getServer().getPlayer(playerId);

            if (player == null || lobbyPlayerData == null) {
                continue;
            }

            PlayerData playerData = new PlayerData();

            if (lobbyPlayerData.getTeam() >= 1 && lobbyPlayerData.getTeam() <= 2) {
                playerData.setTeam(lobbyPlayerData.getTeam());
            }

            this.players.put(playerId, playerData);
        }

        this.plugin.setDatapackStatus(true);
    }

    @Override
    public boolean tick() {

        // Stop game

        if (this.killswitch) {
            this.plugin.getLogger().warning("killswitch triggered");
            return false;
        }

        // Scoreboard

        ServerScoreboard scoreboard = this.plugin.getNMS().getScoreboard();

        Objective systemObjective = scoreboard.getOrCreateObjective("system");
        Score gameMode = scoreboard.getOrCreatePlayerScore("#mode", systemObjective);
        Score gameState = scoreboard.getOrCreatePlayerScore("#state", systemObjective);

        Objective playerStateObjective = scoreboard.getOrCreateObjective("pms.playerstate");

        Objective playerBypassObjective = scoreboard.getOrCreateObjective("pms.bypass");

        // Check players

        for (UUID playerId : this.getPlayers().keySet()) {
            Player player = this.plugin.getServer().getPlayer(playerId);

            if (player == null) {
                this.players.remove(playerId);
                continue;
            }

            Score playerBypass = scoreboard.getOrCreatePlayerScore(player.getName(), playerBypassObjective);

            if (this.plugin.isPlayerBypassing(playerId)) {

                if (playerBypass.getScore() != 1) {
                    playerBypass.setScore(1);
                }

            } else {

                if (playerBypass.getScore() != 0) {
                    playerBypass.setScore(0);
                }

            }

            switch (this.mapState) {
                case 0, 1, 2, 3 -> {

                    player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 300*20, 255, false, false));
                    player.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, 300*20, 255, false, false));
                    player.sendTitle("§6§lPREPARING GAME...", "§7§lThis will take about 10 seconds. Please wait...", 0, 20, 0);

                }
                case 4 -> {

                    player.removePotionEffect(PotionEffectType.BLINDNESS);
                    player.removePotionEffect(PotionEffectType.DARKNESS);

                    player.setHealth(20);
                    player.setFoodLevel(20);
                    player.setSaturation(20);

                    if (this.mapTimer >= 4) {
                        for (int i = 0; i < 100; i++) {
                            player.sendMessage("");
                        }
                        player.sendMessage("§6From this point on, the map runs completely without plugins");
                    }

                }
                case 5 -> {

                    if (!player.getInventory().contains(Material.BREAD)) {
                        player.getInventory().addItem(new ItemStack(Material.BREAD, 15));
                    }

                }
            }

        }

        // Map State Management

        if (this.mapTimer >= 4) {

            switch (this.mapState) {
                case 0 -> {
                    gameMode.setScore(ACM2GameMode.NONE);
                    gameState.setScore(ACM2GameState.RESET);
                    this.mapState++;
                }
                case 1 -> {
                    if (gameState.getScore() != ACM2GameState.NONE) {
                        this.plugin.getLogger().warning("Wrong game state [1]: state has to be 0 but is " + gameState.getScore());
                        return false;
                    }

                    gameMode.setScore(this.gamemode);
                    gameState.setScore(ACM2GameState.START_LOBBY);
                    this.setMapScore(this.selectedMap);
                    this.mapState++;
                }
                case 2 -> {
                    if (gameState.getScore() != ACM2GameState.LOBBY) {
                        this.plugin.getLogger().warning("Wrong game state [2]: state has to be 2 but is " + gameState.getScore());
                        return false;
                    }

                    this.setMapScore(this.selectedMap);

                    for (UUID playerId : this.getPlayers().keySet()) {
                        Player player = this.plugin.getServer().getPlayer(playerId);
                        PlayerData playerData = this.players.get(playerId);

                        if (player == null || playerData == null) {
                            continue;
                        }

                        Score playerState = scoreboard.getOrCreatePlayerScore(player.getName(), playerStateObjective);
                        playerState.setScore(ACM2PlayerState.getPlayerScore(ACM2GameState.LOBBY, this.gamemode, playerData.getTeam()));
                    }

                    this.mapState++;
                }
                case 3 -> {
                    if (gameState.getScore() != ACM2GameState.LOBBY) {
                        this.plugin.getLogger().warning("Wrong game state [3]: state has to be 2 but is " + gameState.getScore());
                        return false;
                    }

                    gameState.setScore(ACM2GameState.START_GAME);

                    this.mapState++;
                }
                case 4 -> this.mapState++;
                case 5 -> {
                    if (gameState.getScore() < ACM2GameState.GAME || gameState.getScore() > ACM2GameState.RESET) {
                        this.plugin.getLogger().warning("Wrong game state [4]: state has to be 4, 5 or 6 but is " + gameState.getScore());
                        return false;
                    }

                    if (gameState.getScore() == ACM2GameState.NONE) {
                        this.plugin.getLogger().info("Game end. Proceeding to endlobby...");
                        this.plugin.setDatapackStatus(false);
                        this.plugin.nextStatus();
                        this.mapState++;
                    }
                }
                default -> {
                    return false;
                }
            }

            this.mapTimer = 0;
        } else {
            this.mapTimer++;
        }

        return true;
    }

    @Override
    public GamePart getNextStatus() {
        return null;
    }

    public Map<UUID, PlayerData> getPlayers() {
        return Map.copyOf(this.players);
    }

    public void addPlayer(UUID playerId) {
        if (!this.players.containsKey(playerId)) {
            this.players.put(playerId, new PlayerData());
        }
    }

    public void removePlayer(UUID playerId) {
        this.players.remove(playerId);
    }

    private void setMapScore(int map) {
        ServerScoreboard scoreboard = this.plugin.getNMS().getScoreboard();

        switch (this.gamemode) {
            case ACM2GameMode.RUSH -> {
                Objective settingsObjective = scoreboard.getOrCreateObjective("rush.settings");
                Score gamezoneScore = scoreboard.getOrCreatePlayerScore("#gamezone", settingsObjective);
                gamezoneScore.setScore(this.selectedMap);
            }
            case ACM2GameMode.TDM -> {
                Objective settingsObjective = scoreboard.getOrCreateObjective("tdm.settings");
                Score gamezoneScore = scoreboard.getOrCreatePlayerScore("#zone", settingsObjective);
                gamezoneScore.setScore(this.selectedMap);
            }
            case ACM2GameMode.CTF -> {
                Objective settingsObjective = scoreboard.getOrCreateObjective("ctf.settings");
                Score gamezoneScore = scoreboard.getOrCreatePlayerScore("#zone", settingsObjective);
                gamezoneScore.setScore(this.selectedMap);
            }
        }

    }
}
