package me.pebbleprojects.pebblelogin.listeners;

import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockPlaceEvent;

import me.pebbleprojects.pebblelogin.engine.Handler;
import me.pebbleprojects.pebblelogin.engine.sessions.SessionsHandler;

public class BlockPlace implements Listener {

    @EventHandler
    public void onBlockPlace(final BlockPlaceEvent event) {
        Handler.INSTANCE.runTask(() -> {
            if (SessionsHandler.INSTANCE.getSession(event.getPlayer().getUniqueId()) != null && !Boolean.parseBoolean(Handler.INSTANCE.getConfig("sessionRules.canPlace", false).toString())) event.setCancelled(true);
        });
    }
}
