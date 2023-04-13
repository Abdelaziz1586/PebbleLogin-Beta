package me.pebbleprojects.pebbleloginbeta.listeners;

import me.pebbleprojects.pebbleloginbeta.engine.Handler;
import me.pebbleprojects.pebbleloginbeta.engine.sessions.SessionsHandler;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

public class EntityDamage implements Listener {

    @EventHandler
    public void onEntityDamage(final EntityDamageEvent event) {
        Handler.INSTANCE.runTask(() -> {
            if (event instanceof EntityDamageByEntityEvent entityEvent) {
                if (SessionsHandler.INSTANCE.getSession(entityEvent.getDamager().getUniqueId()) != null && !Boolean.parseBoolean(Handler.INSTANCE.getConfig("sessionRules.canDamage", false).toString())) event.setCancelled(true);
                return;
            }
            if (SessionsHandler.INSTANCE.getSession(event.getEntity().getUniqueId()) != null && !Boolean.parseBoolean(Handler.INSTANCE.getConfig("sessionRules.canGetDamaged", false).toString())) event.setCancelled(true);
        });
    }

}
