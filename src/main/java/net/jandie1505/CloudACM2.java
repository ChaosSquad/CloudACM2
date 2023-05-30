package net.jandie1505;

import net.jandie1505.config.DefaultConfigValues;
import net.jandie1505.configmanager.ConfigManager;
import net.jandie1505.game.Game;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

public class CloudACM2 extends JavaPlugin {
    private ConfigManager configManager;
    private GamePart game;
    private List<World> managedWorlds;
    private int gameTimer;

    @Override
    public void onEnable() {

        this.configManager = new ConfigManager(DefaultConfigValues.getConfig(), false, this.getDataFolder(), "config.json");
        this.configManager.reloadConfig();

        this.managedWorlds = new ArrayList<>();
        this.gameTimer = 0;
        this.game = null;

        // this.dataPack = this.getServer().getDataPackManager().getDataPack(); this.configManager.getConfig().optString("datapackName", "data_pack");

        /*
        this.border = new int[]{
                this.configManager.getConfig().optJSONObject("border", new JSONObject()).optInt("x1", -286),
                this.configManager.getConfig().optJSONObject("border", new JSONObject()).optInt("y1", 65),
                this.configManager.getConfig().optJSONObject("border", new JSONObject()).optInt("z1", -478),
                this.configManager.getConfig().optJSONObject("border", new JSONObject()).optInt("x2", -280),
                this.configManager.getConfig().optJSONObject("border", new JSONObject()).optInt("y2", -69),
                this.configManager.getConfig().optJSONObject("border", new JSONObject()).optInt("z2", -472)
        };

         */

        this.getServer().getScheduler().scheduleSyncRepeatingTask(this, () -> {

            try {

                if (this.game != null) {

                    if (!this.game.tick()) {
                        this.stopGame();
                        this.getLogger().warning("Game stopped because it was aborted by tick");
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

    public void stopGame() {
        this.game = null;
    }

    public void startGame() {

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
}
