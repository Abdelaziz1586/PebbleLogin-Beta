package me.pebbleprojects.pebbleloginbeta.engine;

import me.pebbleprojects.pebbleloginbeta.PebbleLogin;
import me.pebbleprojects.pebbleloginbeta.engine.sessions.SessionsHandler;
import me.pebbleprojects.pebbleloginbeta.listeners.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

public class Handler {

    private final File dataFile;
    private final PebbleLogin main;
    private MessageDigest hashData;
    private FileConfiguration data, config;
    private final ConsoleCommandSender console;
    private final SessionsHandler sessionsHandler;

    public Handler(final PebbleLogin main) {
        this.main = main;
        console = main.getServer().getConsoleSender();
        main.getConfig().options().copyDefaults(true);
        main.saveDefaultConfig();
        updateConfig();
        dataFile = new File(main.getDataFolder().getPath(), "data.yml");
        if (!dataFile.exists()) {
            try {
                if (dataFile.createNewFile()) {
                    console.sendMessage("§aCreated §edata.yml");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        updateData();

        final String hashType = Boolean.parseBoolean(getConfig("protection.hashData.enabled", false).toString()) ? getConfig("protection.hashData.type", false).toString() : null;
        hashData = null;
        if (hashType != null) {
            try {
                hashData = MessageDigest.getInstance(hashType);
            } catch (final NoSuchAlgorithmException ignored) {
                console.sendMessage("§cCouldn't find hash type §e" + hashType);
            }
        }

        sessionsHandler = new SessionsHandler(this);

        final PluginManager pm = main.getServer().getPluginManager();
        pm.registerEvents(new PlayerJoin(this), main);
        pm.registerEvents(new PlayerQuit(this), main);
        pm.registerEvents(new BlockBreak(this), main);
        pm.registerEvents(new BlockPlace(this), main);
        pm.registerEvents(new PlayerMove(this), main);
        pm.registerEvents(new EntityDamage(this), main);
        pm.registerEvents(new AsyncPlayerChat(this), main);
        pm.registerEvents(new FoodLevelChange(this), main);

        final Commands commands = new Commands(this);
        Objects.requireNonNull(main.getCommand("login")).setExecutor(commands);
        Objects.requireNonNull(main.getCommand("register")).setExecutor(commands);
        Objects.requireNonNull(main.getCommand("pebblelogin")).setExecutor(commands);
    }

    public void updateConfig() {
        main.reloadConfig();
        config = main.getConfig();
    }

    public void updateData() {
        data = YamlConfiguration.loadConfiguration(dataFile);
    }

    public void writeData(final String key, final Object value) {
        data.set(key, value);
        try {
            data.save(dataFile);
        } catch (final IOException ex) {
            ex.printStackTrace();
        }
    }

    public void sendSessionConsoleMessage(final String message, final String sessionPlayer, final boolean isError) {
        if (isError) {
            console.sendMessage("§f[PebbleLogin Session] §cSession of §e" + sessionPlayer + " §chas ran into an error, details:\n\n" + message);
            return;
        }
        console.sendMessage("§f[PebbleLogin Session] §bMessage from session of §e" + sessionPlayer + "§b: §r" + message);
    }

    public void runTask(final Runnable runnable) {
        if (!Bukkit.isPrimaryThread()) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    runnable.run();
                }
            }.runTask(main);
            return;
        }
        runnable.run();
    }

    public final BukkitRunnable runTaskLater(final BukkitRunnable runnable, final int delay) {
        runnable.runTaskLater(main, delay*20L);
        return runnable;
    }

    public final BukkitRunnable runTaskTimer(final BukkitRunnable runnable, final int delay) {
        runnable.runTaskTimer(main, 0, delay*20L);
        return runnable;
    }

    public final String getHashString(final String input) {
        if (hashData != null) return bytesToHex(hashData.digest(input.getBytes(StandardCharsets.UTF_8)));
        return input;
    }

    private String bytesToHex(final byte[] hash) {
        final StringBuilder hexString = new StringBuilder(2 * hash.length);
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    public Object getConfig(final String key, final boolean translate) {
        if (config.isSet(key)) {
            if (translate) {
                return ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(config.getString(key)).replace("%nl%", "\n"));
            }
            return config.get(key);
        }
        return null;
    }

    public Object getData(final String key) {
        if (data.isSet(key))
            return data.get(key);
        return null;
    }

    public final SessionsHandler getSessionsHandler() {
        return sessionsHandler;
    }
}
