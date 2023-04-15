package me.pebbleprojects.pebblelogin.listeners;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;

import me.pebbleprojects.pebblelogin.engine.Handler;
import me.pebbleprojects.pebblelogin.engine.sessions.SessionsHandler;

public class PlayerJoin implements Listener {

    @EventHandler
    public void onPlayerJoin(final PlayerJoinEvent event) {
        new Thread(() -> {
            final Player player = event.getPlayer();
            if (SessionsHandler.INSTANCE.isSessionSaved(player)) {
                teleportTo(player, Handler.INSTANCE.getConfig("protection.autologin.teleportation", false).toString());
                player.sendMessage(Handler.INSTANCE.getConfig("protection.autologin.message", true).toString());
                return;
            }
            SessionsHandler.INSTANCE.createSession(event.getPlayer());
        }).start();
    }

    private void teleportTo(final Player player, final String teleportation) {
        new Thread(() -> {
            Object o;
            if (teleportation.equals("lobby")) {
                o = Handler.INSTANCE.getData("locations.lobby");
                if (o instanceof Location) Handler.INSTANCE.runTask(() -> player.teleport((Location) o));
                return;
            }

            if (teleportation.equals("login")) {
                o = Handler.INSTANCE.getData("locations.login");
                if (o instanceof Location) Handler.INSTANCE.runTask(() -> player.teleport((Location) o));
                return;
            }

            if (teleportation.equals("saved")) {
                o = Handler.INSTANCE.getData("playerData." + player.getUniqueId() + ".savedLocation");
                if (o instanceof Location) Handler.INSTANCE.runTask(() -> player.teleport((Location) o));
            }
        }).start();
    }

}
