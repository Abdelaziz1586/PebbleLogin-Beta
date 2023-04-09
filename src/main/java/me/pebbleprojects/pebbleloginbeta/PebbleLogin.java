package me.pebbleprojects.pebbleloginbeta;

import me.pebbleprojects.pebbleloginbeta.engine.Handler;
import me.pebbleprojects.pebbleloginbeta.listeners.AsyncPlayerPreLogin;
import org.bukkit.Server;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public final class PebbleLogin extends JavaPlugin {

    private boolean loaded;

    @Override
    public void onEnable() {
        loaded = false;
        final Server server = getServer();
        server.getPluginManager().registerEvents(new AsyncPlayerPreLogin(this), this);
        final ConsoleCommandSender console = server.getConsoleSender();
        console.sendMessage("§eLoading §bPebbleLogin§f-§bBETA§e...");
        new Handler(this);
        console.sendMessage("§aLoaded §bPebbleLogin§f-§bBETA");
        loaded = true;
    }

    @Override
    public void onDisable() {
        getServer().getConsoleSender().sendMessage("§cUnloaded §bPebbleLogin§f-§bBETA");
    }

    public boolean isLoaded() {
        return loaded;
    }
}
