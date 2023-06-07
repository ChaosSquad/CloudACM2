package net.jandie1505.cloudacm2;

import net.jandie1505.cloudacm2.game.Game;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

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
}
