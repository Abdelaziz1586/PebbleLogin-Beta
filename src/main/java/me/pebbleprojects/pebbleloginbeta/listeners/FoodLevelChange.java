package me.pebbleprojects.pebbleloginbeta.listeners;

import me.pebbleprojects.pebbleloginbeta.engine.Handler;
import me.pebbleprojects.pebbleloginbeta.engine.sessions.SessionsHandler;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.FoodLevelChangeEvent;

public class FoodLevelChange implements Listener {

    private final Handler handler;
    private final SessionsHandler sessionsHandler;

    public FoodLevelChange(final Handler handler) {
        this.handler = handler;
        sessionsHandler = handler.getSessionsHandler();
    }

    @EventHandler
    public void onFoodLevelChange(final FoodLevelChangeEvent event) {
        handler.runTask(() -> {
            if (sessionsHandler.getSession(event.getEntity().getUniqueId()) != null && !Boolean.parseBoolean(handler.getConfig("sessionRules.canGetHungry", false).toString())) event.setCancelled(true);
        });
    }

}
