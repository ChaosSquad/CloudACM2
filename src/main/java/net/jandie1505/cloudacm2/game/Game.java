package net.jandie1505.cloudacm2.game;

import eu.cloudnetservice.driver.inject.InjectionLayer;
import eu.cloudnetservice.modules.bridge.BridgeServiceHelper;
import net.jandie1505.cloudacm2.CloudACM2;
import net.jandie1505.cloudacm2.GamePart;
import net.jandie1505.cloudacm2.lobby.LobbyPlayerData;
import net.jandie1505.cloudacm2.map.ACM2GameMode;
import net.jandie1505.cloudacm2.map.ACM2GameState;
import net.jandie1505.cloudacm2.map.ACM2PlayerState;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.ScoreAccess;
import net.minecraft.world.scores.ScoreHolder;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Game implements GamePart {
    private final CloudACM2 plugin;
    private boolean killswitch;
    private final int gamemode;
    private final int selectedMap;
    private final Map<UUID, Integer> playerTeams;
    private int mapState;
    private int mapTimer;

    public Game(CloudACM2 plugin, int gamemode, int selectedMap, Map<UUID, LobbyPlayerData> players) {
        this.plugin = plugin;
        this.killswitch = false;
        this.gamemode = gamemode;
        this.selectedMap = selectedMap;
        this.playerTeams = new HashMap<>();
        this.mapState = 0;
        this.mapTimer = 0;

        // add players

        for (UUID playerId : players.keySet()) {
            LobbyPlayerData lobbyPlayerData = players.get(playerId);
            Player player = this.plugin.getServer().getPlayer(playerId);

            if (player == null || lobbyPlayerData == null) {
                continue;
            }

            if (!(lobbyPlayerData.getTeam() >= 1 && lobbyPlayerData.getTeam() <= 2)) {
                continue;
            }

            this.playerTeams.put(playerId, lobbyPlayerData.getTeam());
        }

        // CLOUDSYSTEM MODE

        if (this.plugin.isCloudSystemMode()) {

            // Custom command

            String customCommand = this.plugin.getConfigManager().getConfig().optJSONObject("cloudSystemMode", new JSONObject()).optString("switchToIngameCommand", "");

            if (!customCommand.equalsIgnoreCase("")) {
                this.plugin.getServer().dispatchCommand(this.plugin.getServer().getConsoleSender(), customCommand);
            }

            // CloudNet ingame state

            if (this.plugin.getConfigManager().getConfig().optJSONObject("integrations", new JSONObject()).optBoolean("cloudnet", false)) {

                try {

                    try {
                        Class.forName("eu.cloudnetservice.driver.inject.InjectionLayer");
                        Class.forName("eu.cloudnetservice.modules.bridge.BridgeServiceHelper");

                        BridgeServiceHelper bridgeServiceHelper = InjectionLayer.ext().instance(BridgeServiceHelper.class);

                        if (bridgeServiceHelper != null) {
                            bridgeServiceHelper.changeToIngame();
                            this.plugin.getLogger().info("Changed server to ingame state (CloudNet)");
                        }
                    } catch (ClassNotFoundException ignored) {
                        // ignored (cloudnet not installed)
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

        }

        // enable datapack

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

        Objective systemObjective = scoreboard.getObjective("system");
        ScoreAccess gameMode = scoreboard.getOrCreatePlayerScore(ScoreHolder.forNameOnly("#mode"), systemObjective);
        ScoreAccess gameState = scoreboard.getOrCreatePlayerScore(ScoreHolder.forNameOnly("#state"), systemObjective);

        Objective playerStateObjective = scoreboard.getObjective("pms.playerstate");

        Objective playerBypassObjective = scoreboard.getObjective("pms.bypass");

        // Check players

        for (Player player : this.plugin.getServer().getOnlinePlayers()) {
            ScoreAccess playerBypass = scoreboard.getOrCreatePlayerScore(ScoreHolder.forNameOnly(player.getName()), playerBypassObjective);

            if (this.plugin.isPlayerBypassing(player.getUniqueId())) {

                if (playerBypass.get() != 1) {
                    playerBypass.set(1);
                }

            } else {

                if (playerBypass.get() != 0) {
                    playerBypass.set(0);
                }

                ScoreAccess playerStateScore = scoreboard.getOrCreatePlayerScore(ScoreHolder.forNameOnly(player.getName()), playerStateObjective);
                int playerState = playerStateScore.get();

                boolean isIngame = false;
                for (int i = 0; i < 4; i++) {

                    if (playerState != ACM2PlayerState.getPlayerScore(ACM2GameState.GAME, this.gamemode, i)) {
                        isIngame = true;
                        break;
                    }

                }

                if (isIngame && player.getGameMode() != GameMode.ADVENTURE) {
                    player.setGameMode(GameMode.ADVENTURE);
                } else if (!isIngame && player.getGameMode() != GameMode.SPECTATOR) {
                    player.setGameMode(GameMode.SPECTATOR);
                }

            }

            switch (this.mapState) {
                case 0, 1, 2, 3 -> {

                    player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 300*20, 255, false, false));
                    player.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, 300*20, 255, false, false));
                    player.sendTitle("§6§lPREPARING GAME...", "§7§lThis will take about 10 seconds. Please wait...", 0, 20, 0);

                    if (!this.plugin.isPlayerBypassing(player.getUniqueId()) && !player.getInventory().isEmpty()) {
                        player.getInventory().clear();
                    }

                }
                case 4 -> {

                    player.removePotionEffect(PotionEffectType.BLINDNESS);
                    player.removePotionEffect(PotionEffectType.DARKNESS);

                    player.setHealth(20);
                    player.setFoodLevel(20);
                    player.setSaturation(20);

                    if (!this.plugin.isPlayerBypassing(player.getUniqueId()) && !player.getInventory().isEmpty()) {
                        player.getInventory().clear();
                    }

                    if (this.mapTimer >= 4) {
                        for (int i = 0; i < 100; i++) {
                            player.sendMessage(" ");
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
                    gameMode.set(ACM2GameMode.NONE);
                    gameState.set(ACM2GameState.RESET);
                    this.mapState++;
                }
                case 1 -> {
                    if (gameState.get() != ACM2GameState.NONE) {
                        this.plugin.getLogger().warning("Wrong game state [1]: state has to be 0 but is " + gameState.get());
                        return false;
                    }

                    gameMode.set(this.gamemode);
                    gameState.set(ACM2GameState.START_LOBBY);
                    this.mapState++;
                }
                case 2 -> {
                    if (gameState.get() != ACM2GameState.LOBBY) {
                        this.plugin.getLogger().warning("Wrong game state [2]: state has to be 2 but is " + gameState.get());
                        return false;
                    }

                    this.setMapScore(this.selectedMap);

                    for (Player player : this.plugin.getServer().getOnlinePlayers()) {
                        Integer team = this.playerTeams.get(player.getUniqueId());

                        if (team == null) {
                            team = 0;
                        }

                        ScoreAccess playerState = scoreboard.getOrCreatePlayerScore(ScoreHolder.forNameOnly(player.getName()), playerStateObjective);
                        playerState.set(ACM2PlayerState.getPlayerScore(ACM2GameState.LOBBY, this.gamemode, team));
                    }

                    this.mapState++;
                }
                case 3 -> {
                    if (gameState.get() != ACM2GameState.LOBBY) {
                        this.plugin.getLogger().warning("Wrong game state [3]: state has to be 2 but is " + gameState.get());
                        return false;
                    }

                    gameState.set(ACM2GameState.START_GAME);

                    this.mapState++;
                }
                case 4 -> this.mapState++;
                case 5 -> {
                    if (gameState.get() != ACM2GameState.NONE && (gameState.get() < ACM2GameState.GAME || gameState.get() > ACM2GameState.RESET)) {
                        this.plugin.getLogger().warning("Wrong game state [5]: state has to be 4, 5 or 6 but is " + gameState.get());
                        return false;
                    }

                    if (gameState.get() == ACM2GameState.NONE) {
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

        if (this.plugin.isCloudSystemMode()) {
            this.plugin.getLogger().info("Cloudsystem mode enabled: Shutting down server");
            this.plugin.getServer().shutdown();
        }

        return null;
    }

    private void setMapScore(int map) {
        ServerScoreboard scoreboard = this.plugin.getNMS().getScoreboard();

        switch (this.gamemode) {
            case ACM2GameMode.RUSH -> {
                Objective settingsObjective = scoreboard.getObjective("rush.settings");
                ScoreAccess gamezoneScore = scoreboard.getOrCreatePlayerScore(ScoreHolder.forNameOnly("#gamezone"), settingsObjective);
                gamezoneScore.set(this.selectedMap);
            }
            case ACM2GameMode.TDM -> {
                Objective settingsObjective = scoreboard.getObjective("tdm.settings");
                ScoreAccess gamezoneScore = scoreboard.getOrCreatePlayerScore(ScoreHolder.forNameOnly("#zone"), settingsObjective);
                gamezoneScore.set(this.selectedMap);
            }
            case ACM2GameMode.CTF -> {
                Objective settingsObjective = scoreboard.getObjective("ctf.settings");
                ScoreAccess gamezoneScore = scoreboard.getOrCreatePlayerScore(ScoreHolder.forNameOnly("#zone"), settingsObjective);
                gamezoneScore.set(this.selectedMap);
            }
        }

    }
}
