package me.pebbleprojects.pebbleloginbeta.listeners;

import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.FoodLevelChangeEvent;

import me.pebbleprojects.pebbleloginbeta.engine.Handler;
import me.pebbleprojects.pebbleloginbeta.engine.sessions.SessionsHandler;

public class FoodLevelChange implements Listener {

    @EventHandler
    public void onFoodLevelChange(final FoodLevelChangeEvent event) {
        Handler.INSTANCE.runTask(() -> {
            if (SessionsHandler.INSTANCE.getSession(event.getEntity().getUniqueId()) != null && !Boolean.parseBoolean(Handler.INSTANCE.getConfig("sessionRules.canGetHungry", false).toString())) event.setCancelled(true);
        });
    }

}
