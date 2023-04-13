package me.pebbleprojects.pebbleloginbeta;

import me.pebbleprojects.pebbleloginbeta.engine.Handler;
import me.pebbleprojects.pebbleloginbeta.listeners.AsyncPlayerPreLogin;
import org.bukkit.Server;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public final class PebbleLogin extends JavaPlugin {

    private boolean loaded;
    public static PebbleLogin INSTANCE;

    @Override
    public void onEnable() {
        INSTANCE = this;
        loaded = false;
        final Server server = getServer();
        server.getPluginManager().registerEvents(new AsyncPlayerPreLogin(), this);
        Executors.newSingleThreadScheduledExecutor().schedule(() -> {
            final ConsoleCommandSender console = server.getConsoleSender();
            console.sendMessage("§eLoading §bPebbleLogin§f-§bBETA§e...");
            new Handler();
            console.sendMessage("§aLoaded §bPebbleLogin§f-§bBETA");
            loaded = true;
        }, 1, TimeUnit.SECONDS);

    }

    @Override
    public void onDisable() {
        getServer().getConsoleSender().sendMessage("§cUnloaded §bPebbleLogin§f-§bBETA");
    }

    public boolean isLoaded() {
        return loaded;
    }
}
