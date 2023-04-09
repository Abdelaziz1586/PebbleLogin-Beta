package me.pebbleprojects.pebbleloginbeta.listeners;

import me.pebbleprojects.pebbleloginbeta.engine.Handler;
import me.pebbleprojects.pebbleloginbeta.engine.sessions.SessionsHandler;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class PlayerMove implements Listener {

    private final Handler handler;
    private final SessionsHandler sessionsHandler;

    public PlayerMove(final Handler handler) {
        this.handler = handler;
        sessionsHandler = handler.getSessionsHandler();
    }

    @EventHandler
    public void onPlayerMove(final PlayerMoveEvent event) {
        handler.runTask(() -> {
            if (sessionsHandler.getSession(event.getPlayer().getUniqueId()) != null && !Boolean.parseBoolean(handler.getConfig("sessionRules.canMove", false).toString())) event.setCancelled(true);
        });
    }

}
