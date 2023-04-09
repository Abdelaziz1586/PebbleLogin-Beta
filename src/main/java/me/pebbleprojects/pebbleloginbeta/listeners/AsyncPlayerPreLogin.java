package me.pebbleprojects.pebbleloginbeta.listeners;

import me.pebbleprojects.pebbleloginbeta.PebbleLogin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

public class AsyncPlayerPreLogin implements Listener {

    private final PebbleLogin main;

    public AsyncPlayerPreLogin(final PebbleLogin main) {
        this.main = main;
    }

    @EventHandler
    public void onAsyncPlayerPreLogin(final AsyncPlayerPreLoginEvent event) {
        if (!main.isLoaded()) event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, "§cPlease wait a few seconds.");
    }

}
