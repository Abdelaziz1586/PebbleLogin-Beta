package me.pebbleprojects.pebbleloginbeta.listeners;

import me.pebbleprojects.pebbleloginbeta.engine.Handler;
import me.pebbleprojects.pebbleloginbeta.engine.sessions.SessionsHandler;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoin implements Listener {

    private final Handler handler;
    private final SessionsHandler sessionsHandler;

    public PlayerJoin(final Handler handler) {
        this.handler = handler;
        sessionsHandler = handler.getSessionsHandler();
    }

    @EventHandler
    public void onPlayerJoin(final PlayerJoinEvent event) {
        handler.runTask(() -> {
            final Player player = event.getPlayer();
            if (sessionsHandler.isSessionSaved(player)) {
                teleportTo(player, handler.getConfig("protection.autologin.teleportation", false).toString());
                player.sendMessage(handler.getConfig("protection.autologin.message", true).toString());
                return;
            }
            sessionsHandler.createSession(event.getPlayer());
        });
    }

    private void teleportTo(final Player player, final String teleportation) {
        Object o;
        if (teleportation.equals("lobby")) {
            o = handler.getData("locations.lobby");
            if (o instanceof Location) player.teleport((Location) o);
            return;
        }

        if (teleportation.equals("login")) {
            o = handler.getData("locations.login");
            if (o instanceof Location) player.teleport((Location) o);
            return;
        }

        if (teleportation.equals("saved")) {
            o = handler.getData("playerData." + player.getUniqueId() + ".savedLocation");
            if (o instanceof Location) player.teleport((Location) o);
        }
    }

}
