package me.pebbleprojects.pebbleloginbeta.listeners;

import me.pebbleprojects.pebbleloginbeta.engine.Handler;
import me.pebbleprojects.pebbleloginbeta.engine.sessions.SessionsHandler;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

public class EntityDamage implements Listener {

    private final Handler handler;
    private final SessionsHandler sessionsHandler;

    public EntityDamage(final Handler handler) {
        this.handler = handler;
        sessionsHandler = handler.getSessionsHandler();
    }

    @EventHandler
    public void onEntityDamage(final EntityDamageEvent event) {
        handler.runTask(() -> {
            if (event instanceof EntityDamageByEntityEvent entityEvent) {
                if (sessionsHandler.getSession(entityEvent.getDamager().getUniqueId()) != null && !Boolean.parseBoolean(handler.getConfig("sessionRules.canDamage", false).toString())) event.setCancelled(true);
                return;
            }
            if (sessionsHandler.getSession(event.getEntity().getUniqueId()) != null && !Boolean.parseBoolean(handler.getConfig("sessionRules.canGetDamaged", false).toString())) event.setCancelled(true);
        });
    }

}
