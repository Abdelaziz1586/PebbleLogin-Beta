package me.pebbleprojects.pebbleloginbeta;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.Executors;

import me.pebbleprojects.pebbleloginbeta.engine.Handler;
import me.pebbleprojects.pebbleloginbeta.listeners.AsyncPlayerPreLogin;

public final class PebbleLogin extends JavaPlugin {

    private boolean loaded;
    public static PebbleLogin INSTANCE;

    @Override
    public void onEnable() {
        INSTANCE = this;
        loaded = false;
        getServer().getPluginManager().registerEvents(new AsyncPlayerPreLogin(), this);
        Executors.newSingleThreadScheduledExecutor().schedule(() -> {
            final Logger console = getLogger();
            console.info("§eLoading §bPebbleLogin§e...");
            new Handler();
            console.info("§aLoaded §bPebbleLogin");
            loaded = true;
        }, 1, TimeUnit.SECONDS);
    }

    @Override
    public void onDisable() {
        getLogger().info("§cUnloaded §bPebbleLogin");
    }

    public boolean isLoaded() {
        return loaded;
    }
}
