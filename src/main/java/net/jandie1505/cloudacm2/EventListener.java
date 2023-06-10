package net.jandie1505.cloudacm2;

import net.jandie1505.cloudacm2.game.Game;
import net.jandie1505.cloudacm2.lobby.Lobby;
import net.jandie1505.cloudacm2.lobby.PlayerMenu;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;

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
}
