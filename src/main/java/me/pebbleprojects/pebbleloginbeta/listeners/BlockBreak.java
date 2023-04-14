package me.pebbleprojects.pebbleloginbeta.listeners;

import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;

import me.pebbleprojects.pebbleloginbeta.engine.Handler;
import me.pebbleprojects.pebbleloginbeta.engine.sessions.SessionsHandler;

public class BlockBreak implements Listener {

    @EventHandler
    public void onBlockBreak(final BlockBreakEvent event) {
        Handler.INSTANCE.runTask(() -> {
            if (SessionsHandler.INSTANCE.getSession(event.getPlayer().getUniqueId()) != null && !Boolean.parseBoolean(Handler.INSTANCE.getConfig("sessionRules.canBreak", false).toString())) event.setCancelled(true);
        });
    }

}
