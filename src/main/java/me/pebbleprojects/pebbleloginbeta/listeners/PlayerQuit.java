package me.pebbleprojects.pebbleloginbeta.listeners;

import me.pebbleprojects.pebbleloginbeta.engine.Handler;
import me.pebbleprojects.pebbleloginbeta.engine.sessions.Session;
import me.pebbleprojects.pebbleloginbeta.engine.sessions.SessionsHandler;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

public class PlayerQuit implements Listener {

    private final Handler handler;
    private final SessionsHandler sessionsHandler;

    public PlayerQuit(final Handler handler) {
        this.handler = handler;
        sessionsHandler = handler.getSessionsHandler();
    }

    @EventHandler
    public void onPlayerQuit(final PlayerQuitEvent event) {
        handler.runTask(() -> {
            final Player player = event.getPlayer();
            final UUID uuid = player.getUniqueId();
            final Session session = sessionsHandler.getSession(uuid);
            if (session != null) session.destroySession();
            handler.writeData("playerData." + uuid + ".savedLocation", player.getLocation());
        });
    }

}
