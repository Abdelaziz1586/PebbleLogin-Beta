package me.pebbleprojects.pebbleloginbeta.engine;

import org.bukkit.entity.Player;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.CommandExecutor;

import org.jetbrains.annotations.NotNull;

import me.pebbleprojects.pebbleloginbeta.engine.sessions.Session;
import me.pebbleprojects.pebbleloginbeta.engine.sessions.SessionsHandler;

public class Commands implements CommandExecutor {

    @Override
    public boolean onCommand(final @NotNull CommandSender sender, final @NotNull Command command, final @NotNull String label, final String[] args) {
        new Thread(() -> {
            if (sender instanceof Player player) {
                final String s = command.getName().toLowerCase();
                if (s.equals("pebblelogin")) {
                    if (player.hasPermission(Handler.INSTANCE.getConfig("mainCommand.permission", false).toString())) {
                        if (args.length > 0) {
                            if (args[0].equals("setLobby")) {
                                Handler.INSTANCE.writeData("locations.lobby", player.getLocation());
                                player.sendMessage(Handler.INSTANCE.getConfig("mainCommand.messages.lobbySet", true).toString());
                                return;
                            }
                            if (args[0].equals("setLogin")) {
                                Handler.INSTANCE.writeData("locations.login", player.getLocation());
                                player.sendMessage(Handler.INSTANCE.getConfig("mainCommand.messages.loginSet", true).toString());
                                return;
                            }
                            if (args[0].equals("reload")) {
                                Handler.INSTANCE.updateData();
                                Handler.INSTANCE.updateConfig();
                                player.sendMessage(Handler.INSTANCE.getConfig("mainCommand.messages.reload", true).toString());
                                return;
                            }
                        }

                        player.sendMessage(Handler.INSTANCE.getConfig("mainCommand.messages.invalidArgument", true).toString());
                    }
                    return;
                }

                final Session session = SessionsHandler.INSTANCE.getSession(player.getUniqueId());
                if (session == null) {
                    player.sendMessage(Handler.INSTANCE.getConfig("commandMessages.not_in_session", true).toString());
                    return;
                }

                if (!s.equals(session.getSessionType())) {
                    player.sendMessage(Handler.INSTANCE.getConfig("commandMessages.not_in_" + s + "_session", true).toString());
                    return;
                }

                session.completeSession(args);
                return;
            }
            sender.sendMessage("§cAll of this plugin's commands can't be executed by console.");
        }).start();
        return false;
    }
}
