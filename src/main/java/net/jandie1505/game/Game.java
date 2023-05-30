package net.jandie1505.game;

import net.jandie1505.CloudACM2;
import net.jandie1505.GamePart;
import org.bukkit.World;

public class Game implements GamePart {
    private final CloudACM2 plugin;
    private boolean killswitch;
    private final World world;

    public Game(CloudACM2 plugin, World world) {
        this.plugin = plugin;
        this.killswitch = false;
        this.world = world;
    }

    @Override
    public boolean tick() {

        // Stop game

        if (this.killswitch || this.world == null) {
            return false;
        }

        return true;
    }

    public World getWorld() {
        return this.world;
    }
}
