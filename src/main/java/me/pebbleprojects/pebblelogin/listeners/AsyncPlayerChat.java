package me.pebbleprojects.pebblelogin.listeners;

import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import me.pebbleprojects.pebblelogin.engine.Handler;
import me.pebbleprojects.pebblelogin.engine.sessions.SessionsHandler;

public class AsyncPlayerChat implements Listener {

    @EventHandler
    public void onAsyncPlayerChat(final AsyncPlayerChatEvent event) {
        if (SessionsHandler.INSTANCE.getSession(event.getPlayer().getUniqueId()) != null && !Boolean.parseBoolean(Handler.INSTANCE.getConfig("sessionRules.canChat", false).toString())) event.setCancelled(true);
    }

}
