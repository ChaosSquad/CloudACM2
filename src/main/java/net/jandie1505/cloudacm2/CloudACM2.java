package net.jandie1505.cloudacm2;

import de.myzelyam.api.vanish.VanishAPI;
import net.jandie1505.cloudacm2.commands.ACM2Command;
import net.jandie1505.cloudacm2.config.DefaultConfigValues;
import net.jandie1505.cloudacm2.game.Game;
import net.jandie1505.cloudacm2.lobby.Lobby;
import net.jandie1505.configmanager.ConfigManager;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;
import org.bukkit.OfflinePlayer;
import org.bukkit.craftbukkit.v1_20_R1.CraftServer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

public class CloudACM2 extends JavaPlugin {
    private ConfigManager configManager;
    private int gameTimer;
    private List<UUID> bypassingPlayers;
    private GamePart game;
    private boolean nextStatus;
    private boolean datapackStatus;
    private String datapackName;
    private boolean cloudSystemMode;
    private boolean svLoaded;

    @Override
    public void onEnable() {
        this.configManager = new ConfigManager(DefaultConfigValues.getConfig(), false, this.getDataFolder(), "config.json");
        this.configManager.reloadConfig();
        this.gameTimer = 0;
        this.bypassingPlayers = new ArrayList<>();
        this.game = null;
        this.nextStatus = false;
        this.datapackStatus = false;
        this.datapackName = "file/" + this.configManager.getConfig().optString("datapack", "data_pack");
        this.cloudSystemMode = this.configManager.getConfig().optJSONObject("cloudSystemMode", new JSONObject()).optBoolean("enable", false);

        // supervanish/premiumvanish integration

        if (this.configManager.getConfig().optJSONObject("integrations", new JSONObject()).optBoolean("supervanish-premiumvanish")) {
            try {
                Class.forName("de.myzelyam.api.vanish.VanishAPI");
                this.svLoaded = true;
                this.getLogger().info("SuperVanish/PremiumVanish integration enabled (auto-bypass when vanished)");
            } catch (ClassNotFoundException ignored) {
                this.svLoaded = false;
            }
        } else {
            this.svLoaded = false;
        }

        // commands

        this.getCommand("cloudacm2").setExecutor(new ACM2Command(this));
        this.getCommand("cloudacm2").setTabCompleter(new ACM2Command(this));

        // event listener

        this.getServer().getPluginManager().registerEvents(new EventListener(this), this);

        // NMS datapack

        PackRepository packRepository = this.getNMS().getPackRepository();

        if (!packRepository.getAvailableIds().contains(this.datapackName)) {
            this.getLogger().log(Level.SEVERE, "Data Pack not found");
            this.getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // datapack repeating task

        this.getServer().getScheduler().scheduleSyncRepeatingTask(this, () -> {

            Pack pack = packRepository.getPack(this.datapackName);

            if (pack == null) {
                this.getLogger().log(Level.SEVERE, "Data Pack not found");
                this.getServer().getPluginManager().disablePlugin(this);
                return;
            }

            if (this.getDatapackStatus()) {

                if (!packRepository.getSelectedIds().contains(this.datapackName)) {
                    packRepository.addPack(pack.getId());
                    this.getNMS().reloadResources(packRepository.getSelectedPacks().stream().map(Pack::getId).toList());
                    this.getLogger().info("Data Pack enabled");
                }

            } else {

                if (packRepository.getSelectedIds().contains(this.datapackName)) {
                    packRepository.removePack(pack.getId());
                    this.getNMS().reloadResources(packRepository.getSelectedPacks().stream().map(Pack::getId).toList());
                    this.getLogger().info("Data Pack disabled");
                }

            }

        }, 0, 1);

        // game task

        this.getServer().getScheduler().scheduleSyncRepeatingTask(this, () -> {

            try {

                if (this.game != null) {

                    if (this.game.tick()) {

                        if (this.nextStatus) {

                            this.nextStatus = false;
                            this.game = this.game.getNextStatus();
                            this.getLogger().info("Updated game part");

                        }

                    } else {

                        this.stopGame();
                        this.getLogger().warning("Game stopped because it was aborted by tick");

                    }

                } else {

                    if (this.nextStatus) {
                        this.nextStatus = false;
                    }

                }

            } catch (Exception e) {
                this.stopGame();
                this.getLogger().warning("Exception in game: " + e + "\nMessage: " + e.getMessage() + "\nStacktrace: " + Arrays.toString(e.getStackTrace()) + "--- END ---");
            }

            if (this.game != null) {

                if (this.gameTimer >= 7200) {
                    this.gameTimer = 0;
                    this.stopGame();
                    this.getLogger().info("Game stopped because game timer expired");
                } else {
                    this.gameTimer++;
                }

            }

            if (!(this.game instanceof Game)) {
                this.datapackStatus = false;
            }

        }, 0, 10);

        if (this.isCloudSystemMode()) {
            this.getLogger().info("Cloud System Mode enabled (autostart game + switch to ingame + shutdown on end)");
            this.startGame();
        }

    }

    public void onDisable() {
        this.getLogger().info("Disabling plugin CloudACM");

        this.stopGame();
        this.getNMS().getPackRepository().removePack(this.datapackName);

        this.getLogger().info("CloudACM was successfully disabled");
    }

    public void stopGame() {
        this.game = null;
        this.getLogger().info("Stopped game");
    }

    public void startGame() {
        if (this.game == null) {
            this.game = new Lobby(this);
            this.getLogger().info("Started game");
        }
    }

    public void nextStatus() {
        this.nextStatus = true;
    }

    public GamePart getGame() {
        return this.game;
    }

    public List<UUID> getBypassingPlayers() {
        return List.copyOf(this.bypassingPlayers);
    }

    public boolean isPlayerBypassing(UUID playerId) {

        if (this.getBypassingPlayers().contains(playerId)) {
            return true;
        }

        if (this.getConfigManager().getConfig().optJSONObject("integrations", new JSONObject()).optBoolean("supervanish-premiumvanish", false) && this.svLoaded) {

            Player player = this.getServer().getPlayer(playerId);

            if (player == null) {
                return false;
            }

            return VanishAPI.isInvisible(player);

        }

        return false;
    }

    public void addBypassingPlayer(UUID playerId) {
        this.bypassingPlayers.add(playerId);
    }

    public void removeBypassingPlayer(UUID playerId) {
        this.bypassingPlayers.remove(playerId);
    }

    public void clearBypassingPlayers() {
        this.bypassingPlayers.clear();
    }

    public ConfigManager getConfigManager() {
        return this.configManager;
    }

    public Player getPlayerFromString(String playerString) {
        try {
            UUID uuid = UUID.fromString(playerString);
            return this.getServer().getPlayer(uuid);
        } catch (IllegalArgumentException e) {
            return this.getServer().getPlayer(playerString);
        }
    }

    public OfflinePlayer getOfflinePlayerFromString(String playerString) {
        try {
            UUID uuid = UUID.fromString(playerString);
            return this.getServer().getOfflinePlayer(uuid);
        } catch (IllegalArgumentException e) {
            return this.getServer().getOfflinePlayer(playerString);
        }
    }

    public UUID getPlayerUUIDFromString(String playerString) {
        try {
            return UUID.fromString(playerString);
        } catch (IllegalArgumentException e) {
            Player player = this.getServer().getPlayer(playerString);

            if (player != null) {
                return player.getUniqueId();
            } else {
                return null;
            }
        }
    }

    public boolean isCloudSystemMode() {
        return this.cloudSystemMode;
    }

    public void setCloudSystemMode(boolean cloudSystemMode) {
        this.cloudSystemMode = cloudSystemMode;
    }

    public DedicatedServer getNMS() {
        return ((CraftServer) this.getServer()).getServer();
    }

    public boolean getDatapackStatus() {
        return this.game instanceof Game && this.datapackStatus;
    }

    public void setDatapackStatus(boolean status) {
        this.datapackStatus = status;
    }

    public static int getItemId(ItemStack item) {

        if (item == null) {
            return -1;
        }

        if (item.getItemMeta() == null) {
            return -1;
        }

        if (item.getItemMeta().getLore() == null) {
            return -1;
        }

        if (item.getItemMeta().getLore().isEmpty()) {
            return -1;
        }

        try {
            return Integer.parseInt(item.getItemMeta().getLore().get(0));
        } catch (IllegalArgumentException e) {
            return -1;
        }
    }
}
