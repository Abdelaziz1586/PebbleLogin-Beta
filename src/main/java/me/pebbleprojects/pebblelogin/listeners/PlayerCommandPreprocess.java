package me.pebbleprojects.pebblelogin.listeners;

import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import me.pebbleprojects.pebblelogin.engine.Handler;
import me.pebbleprojects.pebblelogin.engine.sessions.SessionsHandler;

public class PlayerCommandPreprocess implements Listener {

    @EventHandler
    public void onPlayerCommandPreprocess(final PlayerCommandPreprocessEvent event) {
        Handler.INSTANCE.runTask(() -> {
            if (SessionsHandler.INSTANCE.getSession(event.getPlayer().getUniqueId()) != null && !Boolean.parseBoolean(Handler.INSTANCE.getConfig("sessionRules.canExecuteOtherCommands", false).toString()) && !event.getMessage().startsWith("/login") && !event.getMessage().startsWith("/register")) event.setCancelled(true);
        });
    }

}
