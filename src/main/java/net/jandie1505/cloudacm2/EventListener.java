package net.jandie1505.cloudacm2;

import net.jandie1505.cloudacm2.game.Game;
import net.jandie1505.cloudacm2.lobby.Lobby;
import net.jandie1505.cloudacm2.lobby.LobbyPlayerData;
import net.jandie1505.cloudacm2.lobby.MapData;
import net.jandie1505.cloudacm2.lobby.PlayerMenu;
import org.bukkit.entity.Fox;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;

public class EventListener implements Listener {
    private final CloudACM2 plugin;

    public EventListener(CloudACM2 plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {

        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        if (this.plugin.getGame() instanceof Game) {
            return;
        }

        if (this.plugin.isPlayerBypassing(event.getEntity().getUniqueId())) {
            return;
        }

        if (event instanceof EntityDamageByEntityEvent && this.plugin.isPlayerBypassing(((EntityDamageByEntityEvent) event).getDamager().getUniqueId())) {
            return;
        }

        event.setCancelled(true);

    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {

        if (!(this.plugin.getGame() instanceof Lobby)) {
            return;
        }

        if (event.getItem() == null) {
            return;
        }

        int itemId = CloudACM2.getItemId(event.getItem());

        if (itemId < 0) {
            return;
        }

        event.setCancelled(true);

        if (itemId == 1) {
            event.getPlayer().openInventory(new PlayerMenu((Lobby) this.plugin.getGame()).getVotingMenu(event.getPlayer().getUniqueId()));
            return;
        }

    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {

        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        if (event.getInventory() == null) {
            return;
        }

        if (event.getCurrentItem() == null) {
            return;
        }

        if (event.getInventory().getHolder() instanceof PlayerMenu) {

            event.setCancelled(true);

            if (!(this.plugin.getGame() instanceof Lobby)) {
                return;
            }

            LobbyPlayerData playerData = ((Lobby) this.plugin.getGame()).getPlayers().get(event.getWhoClicked().getUniqueId());

            if (playerData == null) {
                return;
            }

            int itemId = CloudACM2.getItemId(event.getCurrentItem());

            if (itemId == 3) {

                if (!((Lobby) this.plugin.getGame()).isMapVoting() || ((Lobby) this.plugin.getGame()).getSelectedMap() != null) {
                    event.getWhoClicked().closeInventory();
                    event.getWhoClicked().sendMessage("§cMap voting is already over or disabled");
                    return;
                }

                event.getWhoClicked().closeInventory();
                event.getWhoClicked().sendMessage("§aVote successfully cleared");
                playerData.setVote(null);
                return;
            }

            if (itemId == 4) {

                if (!((Lobby) this.plugin.getGame()).isMapVoting() || ((Lobby) this.plugin.getGame()).getSelectedMap() != null) {
                    event.getWhoClicked().closeInventory();
                    event.getWhoClicked().sendMessage("§cMap voting is already over or disabled");
                    return;
                }

                int id = ((PlayerMenu) event.getInventory().getHolder()).getMapId(event.getCurrentItem());

                for (MapData map : ((Lobby) this.plugin.getGame()).getMaps()) {

                    if (map.getId() == id) {

                        if (playerData.getVote() == map) {

                            event.getWhoClicked().closeInventory();
                            playerData.setVote(null);
                            event.getWhoClicked().sendMessage("§aYou removed your vote");

                        } else {

                            event.getWhoClicked().closeInventory();
                            playerData.setVote(map);
                            event.getWhoClicked().sendMessage("§aYou changed your vote to " + map.getName());

                        }

                        return;
                    }

                }

                return;
            }

            return;
        }

        if (this.plugin.getGame() instanceof Game) {
            return;
        }

        if (this.plugin.isPlayerBypassing(event.getWhoClicked().getUniqueId())) {
            return;
        }

        event.setCancelled(true);

    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {

        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        if (event.getInventory() == null) {
            return;
        }

        if (event.getInventory() instanceof PlayerMenu) {
            event.setCancelled(true);
            return;
        }

        if (this.plugin.getGame() instanceof Game) {
            return;
        }

        if (this.plugin.isPlayerBypassing(event.getWhoClicked().getUniqueId())) {
            return;
        }

        event.setCancelled(true);

    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {

        if (this.plugin.getGame() instanceof Lobby) {

            event.getPlayer().teleport(((Lobby) this.plugin.getGame()).getLobbySpawn());

        }

    }
}
