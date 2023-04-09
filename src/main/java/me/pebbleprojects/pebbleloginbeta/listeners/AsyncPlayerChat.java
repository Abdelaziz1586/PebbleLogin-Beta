package me.pebbleprojects.pebbleloginbeta.listeners;

import me.pebbleprojects.pebbleloginbeta.engine.Handler;
import me.pebbleprojects.pebbleloginbeta.engine.sessions.SessionsHandler;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class AsyncPlayerChat implements Listener {

    private final Handler handler;
    private final SessionsHandler sessionsHandler;

    public AsyncPlayerChat(final Handler handler) {
        this.handler = handler;
        sessionsHandler = handler.getSessionsHandler();
    }

    @EventHandler
    public void onAsyncPlayerChat(final AsyncPlayerChatEvent event) {
        if (sessionsHandler.getSession(event.getPlayer().getUniqueId()) != null && !Boolean.parseBoolean(handler.getConfig("sessionRules.canChat", false).toString()))
            event.setCancelled(true);
    }

}