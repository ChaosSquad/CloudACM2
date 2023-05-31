package net.jandie1505.cloudacm2;

import net.jandie1505.cloudacm2.commands.ACM2Command;
import net.jandie1505.cloudacm2.config.DefaultConfigValues;
import net.jandie1505.configmanager.ConfigManager;
import net.jandie1505.cloudacm2.game.Game;
import net.jandie1505.cloudacm2.lobby.Lobby;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class CloudACM2 extends JavaPlugin {
    private ConfigManager configManager;
    private List<World> managedWorlds;
    private int gameTimer;
    private List<UUID> bypassingPlayers;
    private GamePart game;
    private boolean nextStatus;

    @Override
    public void onEnable() {
        this.configManager = new ConfigManager(DefaultConfigValues.getConfig(), false, this.getDataFolder(), "config.json");
        this.configManager.reloadConfig();
        this.managedWorlds = new ArrayList<>();
        this.gameTimer = 0;
        this.bypassingPlayers = new ArrayList<>();
        this.game = null;
        this.nextStatus = false;

        this.getCommand("cloudacm2").setExecutor(new ACM2Command(this));
        this.getCommand("cloudacm2").setTabCompleter(new ACM2Command(this));

        this.getServer().getPluginManager().registerEvents(new EventListener(this), this);

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

            for (World world : this.getManagedWorlds()) {

                if (!(this.game instanceof Game) || ((Game) this.game).getWorld() != world) {
                    this.unloadWorld(world);
                }

            }

        }, 0, 10);

    }

    public void onDisable() {
        this.getLogger().info("Disabling plugin CloudACM");

        this.stopGame();

        for (World world : this.getManagedWorlds()) {
            this.unloadWorld(world);
        }

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

    public List<World> getManagedWorlds() {
        return List.copyOf(this.managedWorlds);
    }

    public boolean unloadWorld(World world) {

        if (world == null || this.getServer().getWorlds().get(0) == world || !this.managedWorlds.contains(world) || !this.getServer().getWorlds().contains(world)) {
            return false;
        }

        UUID uid = world.getUID();
        int index = this.getServer().getWorlds().indexOf(world);
        String name = world.getName();

        for (Player player : world.getPlayers()) {
            player.teleport(new Location(this.getServer().getWorlds().get(0), 0, 0, 0));
        }

        boolean success = this.getServer().unloadWorld(world, false);

        if (success) {
            this.managedWorlds.remove(world);
            this.getLogger().info("Unloaded world [" + index + "] " + uid + " (" + name + ")");
        } else {
            this.getLogger().warning("Error white unloading world [" + index + "] " + uid + " (" + name + ")");
        }

        return success;

    }

    public World loadWorld(String name) {

        World world = this.getServer().getWorld(name);

        if (world != null) {
            this.managedWorlds.add(world);
            world.setAutoSave(false);
            this.getLogger().info("World [" + this.getServer().getWorlds().indexOf(world) + "] " + world.getUID() + " (" + world.getName() + ") is already loaded and was added to managed worlds");
            return world;
        }

        world = this.getServer().createWorld(new WorldCreator(name));

        if (world != null) {
            this.managedWorlds.add(world);
            world.setAutoSave(false);
            this.getLogger().info("Loaded world [" + this.getServer().getWorlds().indexOf(world) + "] " + world.getUID() + " (" + world.getName() + ")");
        } else {
            this.getLogger().warning("Error while loading world " + name);
        }

        return world;

    }

    public List<UUID> getBypassingPlayers() {
        return List.copyOf(this.bypassingPlayers);
    }

    public boolean isPlayerBypassing(UUID playerId) {
        return this.bypassingPlayers.contains(playerId);
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
}