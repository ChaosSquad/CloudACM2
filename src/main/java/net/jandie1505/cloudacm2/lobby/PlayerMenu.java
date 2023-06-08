package net.jandie1505.cloudacm2.lobby;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.List;

public class PlayerMenu implements InventoryHolder {
    private final Lobby lobby;

    public PlayerMenu(Lobby lobby) {
        this.lobby = lobby;
    }

    @Override
    public Inventory getInventory() {
        return this.lobby.getPlugin().getServer().createInventory(this, 9, "§c§mLobby Menu");
    }
}
