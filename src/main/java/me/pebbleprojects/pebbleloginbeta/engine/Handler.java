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
    public static Handler INSTANCE;
    private MessageDigest hashData;
    private final boolean highEndAPI;
    private FileConfiguration data, config;
    private final ConsoleCommandSender console;

    public Handler() {
        INSTANCE = this;
        highEndAPI = Integer.parseInt(PebbleLogin.INSTANCE.getServer().getBukkitVersion().split("\\.")[1].split("\\.")[0]) >= 13;
        console = PebbleLogin.INSTANCE.getServer().getConsoleSender();
        PebbleLogin.INSTANCE.getConfig().options().copyDefaults(true);
        PebbleLogin.INSTANCE.saveDefaultConfig();
        updateConfig();
        dataFile = new File(PebbleLogin.INSTANCE.getDataFolder().getPath(), "data.yml");
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

        new SessionsHandler();
        final String hashType = Boolean.parseBoolean(getConfig("protection.hashData.enabled", false).toString()) ? getConfig("protection.hashData.type", false).toString() : null;
        hashData = null;
        if (hashType != null) {
            try {
                hashData = MessageDigest.getInstance(hashType);
            } catch (final NoSuchAlgorithmException ignored) {
                console.sendMessage("§cCouldn't find hash type §e" + hashType);
            }
        }


        final PluginManager pm = PebbleLogin.INSTANCE.getServer().getPluginManager();
        pm.registerEvents(new PlayerJoin(), PebbleLogin.INSTANCE);
        pm.registerEvents(new PlayerQuit(), PebbleLogin.INSTANCE);
        pm.registerEvents(new BlockBreak(), PebbleLogin.INSTANCE);
        pm.registerEvents(new BlockPlace(), PebbleLogin.INSTANCE);
        pm.registerEvents(new EntityDamage(), PebbleLogin.INSTANCE);
        pm.registerEvents(new AsyncPlayerChat(), PebbleLogin.INSTANCE);
        pm.registerEvents(new FoodLevelChange(), PebbleLogin.INSTANCE);
        pm.registerEvents(new PlayerCommandPreprocess(), PebbleLogin.INSTANCE);

        final Commands commands = new Commands();
        Objects.requireNonNull(PebbleLogin.INSTANCE.getCommand("login")).setExecutor(commands);
        Objects.requireNonNull(PebbleLogin.INSTANCE.getCommand("register")).setExecutor(commands);
        Objects.requireNonNull(PebbleLogin.INSTANCE.getCommand("pebblelogin")).setExecutor(commands);

        new ConsoleFilter();
    }

    public void updateConfig() {
        PebbleLogin.INSTANCE.reloadConfig();
        config = PebbleLogin.INSTANCE.getConfig();
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
            }.runTask(PebbleLogin.INSTANCE);
            return;
        }
        runnable.run();
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

    public boolean isHighEndAPI() {
        return highEndAPI;
    }
}
