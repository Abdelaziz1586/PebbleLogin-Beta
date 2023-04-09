package me.pebbleprojects.pebbleloginbeta.engine.sessions;

import me.pebbleprojects.pebbleloginbeta.engine.Handler;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Random;
import java.util.UUID;

public class Session {

    private UUID uuid;
    private Player player;
    private Random random;
    private Handler handler;
    private SessionsHandler sessionsHandler;
    private int captcha, captchaMaxNumber, WCMaxTries, IAMaxTries, NMPMaxTries, IPMaxTries, WCTries, IATries, NMPTries, IPTries;
    private BukkitRunnable messageRunnable, timeoutRunnable, moveRunnable;
    private String sessionType, WC, kickWC, IA, kickIA, NMP, kickNMP, IP, kickIP, C;

    public Session(final Player player, final Handler handler) {
        try {
            this.player = player;
            random = new Random();
            sessionType = handler.getData("playerData." + player.getUniqueId() + ".password") == null ? "register" : "login";
            this.handler = handler;
            uuid = player.getUniqueId();
            sessionsHandler = handler.getSessionsHandler();

            setupCaptcha();
            setupTimeout();
            setupMovement();
            setupCompleted();
            setupRepetitiveMessage();
            setupIncorrectPassword();
            setupIncorrectArguments();
            setupNonMatchingPasswords();

            teleportTo(handler.getConfig(sessionType + ".teleportation", false).toString());
        } catch (final Exception e) {
            player.kickPlayer(handler.getConfig("otherKickMessages.error", true).toString());
            e.printStackTrace();
            destroySession();
        }
    }

    public void completeSession(final String[] args) {
        try {
            if (sessionType.equals("register")) {

                if (args.length <= 1) {
                    if (kickIA != null) {
                        IATries++;
                        if (IATries >= IAMaxTries) {
                            player.kickPlayer(kickIA);
                            return;
                        }
                    }
                    player.sendMessage(IA);
                    return;
                }

                if (!args[0].equals(args[1])) {
                    if (kickNMP != null) {
                        NMPTries++;
                        if (NMPTries >= NMPMaxTries) {
                            player.kickPlayer(kickNMP);
                            return;
                        }
                    }
                    player.sendMessage(NMP);
                    return;
                }

                if (WC != null) {
                    try {
                        if (args.length == 2) {
                            if (kickIA != null) {
                                IATries++;
                                if (IATries >= IAMaxTries) {
                                    player.kickPlayer(kickIA);
                                    return;
                                }
                            }
                            player.sendMessage(IA);
                            return;
                        }
                        if (Integer.parseInt(args[2]) != this.captcha) {
                            if (kickWC != null) {
                                WCTries++;
                                if (WCTries >= WCMaxTries) {
                                    player.kickPlayer(kickWC);
                                    return;
                                }
                            }
                            player.sendMessage(WC);
                            return;
                        }
                    } catch (final NumberFormatException ignored) {
                        if (kickWC != null) {
                            WCTries++;
                            if (WCTries >= WCMaxTries) {
                                player.kickPlayer(kickWC);
                                return;
                            }
                        }
                        player.sendMessage(WC);
                        return;
                    }
                }

                handler.writeData("playerData." + player.getUniqueId() + ".password", handler.getHashString(args[1]));

            } else if (sessionType.equals("login")) {


                if (args.length == 0) {
                    if (kickIA != null) {
                        IATries++;
                        if (IATries >= IAMaxTries) {
                            player.kickPlayer(kickIA);
                            return;
                        }
                    }
                    player.sendMessage(IA);
                    return;
                }

                if (!handler.getHashString(args[0]).equals(handler.getData("playerData." + player.getUniqueId() + ".password"))) {
                    if (kickIP != null) {
                        IPTries++;
                        if (IPTries >= IPMaxTries) {
                            player.kickPlayer(kickIP);
                            return;
                        }
                    }
                    player.sendMessage(IP);
                    return;
                }
            }

            player.sendMessage(C);
            teleportTo(handler.getConfig(sessionType + ".completed.teleportation", false).toString());
            if (Boolean.parseBoolean(handler.getConfig("protection.autologin.enabled", false).toString())) handler.getSessionsHandler().saveSession(player);
            destroySession();
        } catch (final Exception ex) {
            player.kickPlayer(handler.getConfig("otherKickMessages.error", true).toString());
            ex.printStackTrace();
            destroySession();
        }
    }

    public final String getSessionType() {
        return sessionType;
    }

    public void destroySession() {
        if (timeoutRunnable != null) {
            timeoutRunnable.cancel();
            timeoutRunnable = null;
        }
        if (messageRunnable != null) {
            messageRunnable.cancel();
            messageRunnable = null;
        }
        if (moveRunnable != null) {
            moveRunnable.cancel();
            moveRunnable = null;
        }

        sessionsHandler.deleteSession(uuid);
        captcha = captchaMaxNumber = WCMaxTries = IAMaxTries = NMPMaxTries = IPMaxTries = WCTries = IATries = NMPTries = IPTries = 0;
        sessionType = WC = kickWC = IA = kickIA = NMP = kickNMP = IP = kickIP = C = null;

        player = null;
        handler = null;

        System.gc();
        System.runFinalization();
    }

    private void regenerateCaptcha() {
        captcha = random.nextInt(captchaMaxNumber);
    }

    private void setupRepetitiveMessage() {
        messageRunnable = null;
        if (Boolean.parseBoolean(handler.getConfig(sessionType + ".repetitiveMessage.enabled", false).toString())) {
            messageRunnable = handler.runTaskTimer(new BukkitRunnable() {
                @Override
                public void run() {
                    player.sendMessage(handler.getConfig(sessionType + ".repetitiveMessage.message", true).toString().replace("%captcha%", String.valueOf(captcha)));
                }
            }, Integer.parseInt(handler.getConfig(sessionType + ".repetitiveMessage.every", false).toString()));
        }
    }

    private void setupMovement() {
        if (!Boolean.parseBoolean(handler.getConfig("sessionRules.canMove", false).toString())) {
            final Location location = player.getLocation();
            moveRunnable = handler.runTaskTimer(new BukkitRunnable() {
                @Override
                public void run() {
                    if (!player.getLocation().equals(location)) player.teleport(location);
                }
            }, 0.05);
        }
    }

    private void setupCaptcha() {
        WC = null;
        kickWC = null;
        WCMaxTries = 0;
        captchaMaxNumber = 0;
        captcha = 0;
        if (sessionType.equals("login")) return;
        if (Boolean.parseBoolean(handler.getConfig(sessionType + ".captcha.enabled", false).toString())) {
            captchaMaxNumber = Integer.parseInt(handler.getConfig(sessionType + ".captcha.maxNumber", false).toString());
            if (captchaMaxNumber <= 1) {
                captchaMaxNumber = 0;
                handler.sendSessionConsoleMessage("§cMax captcha number must be greater than 1, therefor captcha has been disabled for this session.", player.getName(), true);
                return;
            }

            if (Boolean.parseBoolean(handler.getConfig(sessionType + ".captcha.tries.enabled", false).toString())) {
                WCMaxTries = Integer.parseInt(handler.getConfig(sessionType + ".captcha.tries.tries", false).toString());
                if (WCMaxTries <= 0) {
                    WCMaxTries = 0;
                    handler.sendSessionConsoleMessage("§cCaptcha tries must be greater than 0, therefor captcha has been disabled for this session.", player.getName(), true);
                    return;
                }
                kickWC = handler.getConfig(sessionType + ".captcha.tries.kick-message", true).toString();
            }

            WC = handler.getConfig(sessionType + ".captcha.incorrectMessage", true).toString();
            regenerateCaptcha();
            return;
        }
        captcha = -1;
    }

    private void setupTimeout() {
        timeoutRunnable = null;
        if (Boolean.parseBoolean(handler.getConfig(sessionType + ".timeout.enabled", false).toString())) {
            final String timeout = handler.getConfig(sessionType + ".timeout.kick-message", true).toString();
            timeoutRunnable = handler.runTaskLater(new BukkitRunnable() {
                @Override
                public void run() {
                    player.kickPlayer(timeout);
                    destroySession();
                }
            }, Integer.parseInt(handler.getConfig(sessionType + ".timeout.time", false).toString()));
        }
    }

    private void setupIncorrectArguments() {
        IA = handler.getConfig(sessionType + ".incorrectArguments.message", true).toString();
        kickIA = null;
        IAMaxTries = 0;
        if (Boolean.parseBoolean(handler.getConfig(sessionType + ".incorrectArguments.tries.enabled", false).toString())) {
            IAMaxTries = Integer.parseInt(handler.getConfig(sessionType + ".incorrectArguments.tries.tries", false).toString());
            if (IAMaxTries <= 0) {
                handler.sendSessionConsoleMessage("§cArgument tries must be greater than 0, therefor argument tries has been disabled for this session.", player.getName(), true);
                IAMaxTries = 0;
                return;
            }

            kickIA = handler.getConfig(sessionType + ".incorrectArguments.tries.kick-message", true).toString();
        }
    }

    private void setupNonMatchingPasswords() {
        NMP = null;
        kickNMP = null;
        NMPMaxTries = 0;
        if (sessionType.equals("login")) return;

        NMP = handler.getConfig(sessionType + ".nonMatchingPasswords.message", true).toString();
        if (Boolean.parseBoolean(handler.getConfig(sessionType + ".nonMatchingPasswords.tries.enabled", false).toString())) {
            NMPMaxTries = Integer.parseInt(handler.getConfig(sessionType + ".nonMatchingPasswords.tries.tries", false).toString());
            if (NMPMaxTries <= 0) {
                handler.sendSessionConsoleMessage("§cNon Matching Password tries must be greater than 0, therefor non matching password tries has been disabled for this session.", player.getName(), true);
                NMPMaxTries = 0;
                return;
            }

            kickNMP = handler.getConfig(sessionType + ".nonMatchingPasswords.tries.kick-message", true).toString();
        }
    }

    private void setupCompleted() {
        C = handler.getConfig(sessionType + ".completed.message", true).toString();
    }

    private void setupIncorrectPassword() {
        IP = null;
        kickIP = null;
        IPMaxTries = 0;
        if (sessionType.equals("register")) return;

        IP = handler.getConfig(sessionType + ".incorrectPassword.message", true).toString();
        if (Boolean.parseBoolean(handler.getConfig(sessionType + ".incorrectPassword.tries.enabled", false).toString())) {
            IPMaxTries = Integer.parseInt(handler.getConfig(sessionType + ".incorrectPassword.tries.tries", false).toString());
            if (IPMaxTries <= 0) {
                handler.sendSessionConsoleMessage("§cIncorrect Password tries must be greater than 0, therefor incorrect password tries has been disabled for this session.", player.getName(), true);
                IPMaxTries = 0;
                return;
            }

            kickIP = handler.getConfig(sessionType + ".incorrectPassword.tries.kick-message", true).toString();
        }
    }

    private void teleportTo(final String teleportation) {
        Object o;
        if (teleportation.equals("lobby")) {
            o = handler.getData("locations.lobby");
            if (o instanceof Location) player.teleport((Location) o);
            return;
        }

        if (teleportation.equals("login")) {
            o = handler.getData("locations.login");
            if (o instanceof Location) player.teleport((Location) o);
            return;
        }

        if (teleportation.equals("saved")) {
            o = handler.getData("playerData." + player.getUniqueId() + ".savedLocation");
            if (o instanceof Location) player.teleport((Location) o);
        }
    }

}

