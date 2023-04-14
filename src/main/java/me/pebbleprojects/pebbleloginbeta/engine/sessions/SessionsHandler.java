package me.pebbleprojects.pebbleloginbeta.engine.sessions;

import org.bukkit.entity.Player;

import java.util.*;

import me.pebbleprojects.pebbleloginbeta.engine.Handler;

public class SessionsHandler {

    public static SessionsHandler INSTANCE;
    private final HashMap<UUID, Session> sessions;
    private final HashMap<UUID, String> savedSessions;

    public SessionsHandler() {
        INSTANCE = this;
        sessions = new HashMap<>();
        savedSessions = new HashMap<>();
    }

    public void createSession(final Player player) {
        sessions.put(player.getUniqueId(), new Session(player));
    }

    public void saveSession(final Player player) {
        try {
            final UUID uuid = player.getUniqueId();
            final String IP = Handler.INSTANCE.getHashString(Objects.requireNonNull(player.getAddress()).getAddress().toString().replace("/", ""));
            final int i = Integer.parseInt(Handler.INSTANCE.getConfig("protection.autologin.sessionDuration", false).toString());
            if (i <= 0) {
                Handler.INSTANCE.writeData("playerData." + uuid + ".savedSession", IP);
                return;
            }
            savedSessions.put(uuid, IP);
            new Thread(() -> new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    savedSessions.remove(uuid);
                }
            }, i * 1000L)).start();
        } catch (final Exception exception) {
            Handler.INSTANCE.sendSessionConsoleMessage("§cCouldn't save this session\n\n" + exception.getMessage(), player.getName(), true);
        }
    }

    public void deleteSession(final UUID uuid) {
        sessions.remove(uuid);
    }

    public final boolean isSessionSaved(final Player player) {
        final UUID uuid = player.getUniqueId();
        final String IP = Handler.INSTANCE.getHashString(Objects.requireNonNull(player.getAddress()).getAddress().toString().replace("/", ""));
        final Object o = Handler.INSTANCE.getData("playerData." + uuid + ".savedSession");
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
