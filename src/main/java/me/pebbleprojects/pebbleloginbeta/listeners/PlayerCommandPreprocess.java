package me.pebbleprojects.pebbleloginbeta.listeners;

import me.pebbleprojects.pebbleloginbeta.engine.Handler;
import me.pebbleprojects.pebbleloginbeta.engine.sessions.SessionsHandler;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class PlayerCommandPreprocess implements Listener {

    private final Handler handler;
    private final SessionsHandler sessionsHandler;

    public PlayerCommandPreprocess(final Handler handler) {
        this.handler = handler;
        sessionsHandler = handler.getSessionsHandler();
    }

    @EventHandler
    public void onPlayerCommandPreprocess(final PlayerCommandPreprocessEvent event) {
        handler.runTask(() -> {
            if (sessionsHandler.getSession(event.getPlayer().getUniqueId()) != null && !Boolean.parseBoolean(handler.getConfig("sessionRules.canExecuteOtherCommands", false).toString()) && !event.getMessage().startsWith("/login") && !event.getMessage().startsWith("/register")) event.setCancelled(true);
        });
    }

}
