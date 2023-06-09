package me.pebbleprojects.pebblelogin.listeners;

import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

import me.pebbleprojects.pebblelogin.PebbleLogin;

public class AsyncPlayerPreLogin implements Listener {

    @EventHandler
    public void onAsyncPlayerPreLogin(final AsyncPlayerPreLoginEvent event) {
        if (!PebbleLogin.INSTANCE.isLoaded()) event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, "§cPlease wait a few seconds.");
    }

}
