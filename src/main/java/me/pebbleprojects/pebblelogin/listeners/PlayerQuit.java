package me.pebbleprojects.pebblelogin.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

import me.pebbleprojects.pebblelogin.engine.Handler;
import me.pebbleprojects.pebblelogin.engine.sessions.Session;
import me.pebbleprojects.pebblelogin.engine.sessions.SessionsHandler;

public class PlayerQuit implements Listener {

    @EventHandler
    public void onPlayerQuit(final PlayerQuitEvent event) {
        new Thread(() -> {
            final Player player = event.getPlayer();
            final UUID uuid = player.getUniqueId();
            final Session session = SessionsHandler.INSTANCE.getSession(uuid);
            if (session != null) session.destroySession();
            Handler.INSTANCE.writeData("playerData." + uuid + ".savedLocation", player.getLocation());
        }).start();
    }

}
