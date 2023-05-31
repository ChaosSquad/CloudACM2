package net.jandie1505.game;

import net.jandie1505.CloudACM2;
import net.jandie1505.GamePart;
import org.bukkit.World;

public class Game implements GamePart {
    private final CloudACM2 plugin;
    private boolean killswitch;
    private final World world;
    private final int gamemode;
    private final int selectedMap;

    public Game(CloudACM2 plugin, World world, int gamemode, int selectedMap) {
        this.plugin = plugin;
        this.killswitch = false;
        this.world = world;
        this.gamemode = gamemode;
        this.selectedMap = selectedMap;
    }

    @Override
    public boolean tick() {

        // Stop game

        if (this.killswitch || this.world == null) {
            return false;
        }

        return true;
    }

    @Override
    public GamePart getNextStatus() {
        return null;
    }

    public World getWorld() {
        return this.world;
    }
}
