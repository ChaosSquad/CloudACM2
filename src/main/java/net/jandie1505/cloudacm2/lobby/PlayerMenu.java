package net.jandie1505.cloudacm2.lobby;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PlayerMenu implements InventoryHolder {
    private final Lobby lobby;

    public PlayerMenu(Lobby lobby) {
        this.lobby = lobby;
    }

    @Override
    public Inventory getInventory() {
        return this.lobby.getPlugin().getServer().createInventory(this, 9, "§c§mLobby Menu");
    }

    public Inventory getVotingMenu(UUID playerId) {
        int inventorySize = ((this.lobby.getMaps().size() / 9) + 1) * 9;

        if (inventorySize < 9) {
            inventorySize = 9;
        }

        if (inventorySize > 54) {
            inventorySize = 54;
        }

        Inventory inventory = this.lobby.getPlugin().getServer().createInventory(this, inventorySize, "§6§lZone Voting");
        LobbyPlayerData playerData = this.lobby.getPlayers().get(playerId);

        if (playerData == null) {
            return inventory;
        }

        inventory.setItem(0, this.getClearVoteButton());

        int slot = 1;
        for (MapData map : this.lobby.getMaps()) {

            if (slot >= inventory.getSize()) {
                break;
            }

            inventory.setItem(slot, this.getVotingButton(map, map == playerData.getVote()));

            slot++;
        }

        return inventory;
    }

    public ItemStack buildInventoryButton(String name, int id, Material material) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = this.lobby.getPlugin().getServer().getItemFactory().getItemMeta(item.getType());

        meta.setDisplayName("§r" + name);

        List<String> lore = new ArrayList<>();
        lore.add(String.valueOf(id));
        meta.setLore(lore);

        item.setItemMeta(meta);

        return item;
    }

    public ItemStack getClearVoteButton() {
        return this.buildInventoryButton("§cClear vote", 3, Material.STRUCTURE_VOID);
    }

    public ItemStack getVotingButton(MapData map, boolean selected) {
        ItemStack item = this.buildInventoryButton("§a" + map.getName(), 4, Material.GREEN_TERRACOTTA);
        ItemMeta meta = item.getItemMeta();

        List<String> lore = meta.getLore();
        lore.add(String.valueOf(map.getId()));

        if (selected) {
            lore.add("§r§7Click to clear vote");
        } else {
            lore.add("§r§7Click to vote");
        }

        meta.setLore(lore);

        if (selected) {
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            meta.addEnchant(Enchantment.LUCK, 1, true);
        }

        item.setItemMeta(meta);

        return item;
    }

    public int getMapId(ItemStack item) {

        if (item == null) {
            return -1;
        }

        if (item.getItemMeta() == null) {
            return -1;
        }

        if (item.getItemMeta().getLore() == null) {
            return -1;
        }

        if (item.getItemMeta().getLore().size() < 2) {
            return -1;
        }

        try {
            return Integer.parseInt(item.getItemMeta().getLore().get(1));
        } catch (IllegalArgumentException e) {
            return -1;
        }
    }
}
