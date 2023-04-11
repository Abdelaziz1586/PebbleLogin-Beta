package me.pebbleprojects.pebbleloginbeta.engine;

import me.pebbleprojects.pebbleloginbeta.engine.sessions.Session;
import me.pebbleprojects.pebbleloginbeta.engine.sessions.SessionsHandler;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class Commands implements CommandExecutor {

    private final Handler handler;
    private final SessionsHandler sessionsHandler;

    public Commands(final Handler handler) {
        this.handler = handler;
        sessionsHandler = handler.getSessionsHandler();
    }

    @Override
    public boolean onCommand(final @NotNull CommandSender sender, final @NotNull Command command, final @NotNull String label, final String[] args) {
        new Thread(() -> {
            if (sender instanceof Player player) {
                final String s = command.getName().toLowerCase();
                if (s.equals("pebblelogin")) {
                    if (player.hasPermission(handler.getConfig("mainCommand.permission", false).toString())) {
                        if (args.length > 0) {
                            if (args[0].equals("setLobby")) {
                                handler.writeData("locations.lobby", player.getLocation());
                                player.sendMessage(handler.getConfig("mainCommand.messages.lobbySet", true).toString());
                                return;
                            }
                            if (args[0].equals("setLogin")) {
                                handler.writeData("locations.login", player.getLocation());
                                player.sendMessage(handler.getConfig("mainCommand.messages.loginSet", true).toString());
                                return;
                            }
                            if (args[0].equals("reload")) {
                                handler.updateData();
                                handler.updateConfig();
                                player.sendMessage(handler.getConfig("mainCommand.messages.reload", true).toString());
                                return;
                            }
                        }

                        player.sendMessage(handler.getConfig("mainCommand.messages.invalidArgument", true).toString());
                    }
                    return;
                }

                final Session session = sessionsHandler.getSession(player.getUniqueId());
                if (session == null) {
                    player.sendMessage(handler.getConfig("commandMessages.not_in_session", true).toString());
                    return;
                }


                if (!s.equals(session.getSessionType())) {
                    player.sendMessage(handler.getConfig("commandMessages.not_in_" + s + "_session", true).toString());
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
