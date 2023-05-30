package net.jandie1505.game;

import net.jandie1505.GamePart;
import org.bukkit.World;

public class Game implements GamePart {
    private final World world;

    public Game() {

    }

    @Override
    public boolean tick() {
        return false;
    }

    public World getWorld() {
        return this.world;
    }
}
