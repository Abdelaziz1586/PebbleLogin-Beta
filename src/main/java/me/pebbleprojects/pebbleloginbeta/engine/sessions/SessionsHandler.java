package me.pebbleprojects.pebbleloginbeta.engine.sessions;

import me.pebbleprojects.pebbleloginbeta.engine.Handler;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;

public class SessionsHandler {

    private final Handler handler;
    private final HashMap<UUID, String> savedSessions;
    private final HashMap<UUID, Session> sessions;

    public SessionsHandler(final Handler handler) {
        this.handler = handler;
        sessions = new HashMap<>();
        savedSessions = new HashMap<>();
    }

    public void createSession(final Player player) {
        sessions.put(player.getUniqueId(), new Session(player, handler));
    }

    public void saveSession(final Player player) {
        try {
            final UUID uuid = player.getUniqueId();
            final String IP = handler.getHashString(Objects.requireNonNull(player.getAddress()).getAddress().toString().replace("/", ""));
            final int i = Integer.parseInt(handler.getConfig("protection.autologin.sessionDuration", false).toString());
            if (i <= 0) {
                handler.writeData("playerData." + uuid + ".savedSession", IP);
                return;
            }
            savedSessions.put(uuid, IP);
            handler.runTaskLater(new BukkitRunnable() {
                @Override
                public void run() {
                    savedSessions.remove(uuid);
                }
            }, i);
        } catch (final Exception exception) {
            handler.sendSessionConsoleMessage("§cCouldn't save this session\n\n" + exception.getMessage(), player.getName(), true);
        }
    }

    public final boolean isSessionSaved(final Player player) {
        final UUID uuid = player.getUniqueId();
        final String IP = handler.getHashString(Objects.requireNonNull(player.getAddress()).getAddress().toString().replace("/", ""));
        final Object o = handler.getData("playerData." + uuid + ".savedSession");
        if (o != null && o.toString().equals(IP)) return true;
        if (savedSessions.containsKey(uuid)) {
            return Objects.equals(savedSessions.get(uuid), IP);
        }
        return false;
    }

    public final Session getSession(final UUID uuid) {
        return sessions.getOrDefault(uuid, null);
    }
}
