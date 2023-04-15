package me.pebbleprojects.pebblelogin.engine.sessions;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;

import me.pebbleprojects.pebblelogin.PebbleLogin;
import me.pebbleprojects.pebblelogin.engine.Handler;

public class Session implements Runnable {

    private Random random;
    private final Player player;
    private boolean canSeeOthers, canOthersSee;
    private ScheduledFuture<?> messageRunnable, timeoutRunnable, moveRunnable;
    private String sessionType, WC, kickWC, IA, kickIA, NMP, kickNMP, IP, kickIP, C;
    private int captcha, captchaMaxNumber, WCMaxTries, IAMaxTries, NMPMaxTries, IPMaxTries, WCTries, IATries, NMPTries, IPTries;

    public Session(final Player player) {
        this.player = player;
        random = new Random();
        new Thread(this).start();
    }

    @Override
    public void run() {
        try {
            sessionType = Handler.INSTANCE.getData("playerData." + player.getUniqueId() + ".password") == null ? "register" : "login";

            teleportTo(Handler.INSTANCE.getConfig(sessionType + ".teleportation", false).toString());

            setupCaptcha();
            setupTimeout();
            setupMovement();
            setupCompleted();
            setupVisibility();
            setupRepetitiveMessage();
            setupIncorrectPassword();
            setupIncorrectArguments();
            setupNonMatchingPasswords();
            setupMovement();
            setupVisibility();
        } catch (final Exception exception) {
            destroySession();
            exception.printStackTrace();
        }
    }

    public void completeSession(final String[] args) {
        try {
            if (sessionType.equals("register")) {
                if (args.length <= 1) {
                    if (kickIA != null) {
                        IATries++;
                        if (IATries >= IAMaxTries) {
                            Handler.INSTANCE.runTask(() -> player.kickPlayer(kickIA));
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
                            Handler.INSTANCE.runTask(() -> player.kickPlayer(kickNMP));
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
                                    Handler.INSTANCE.runTask(() -> player.kickPlayer(kickIA));
                                    return;
                                }
                            }
                            player.sendMessage(IA);
                            return;
                        }
                        if (Integer.parseInt(args[2]) != captcha) {
                            if (kickWC != null) {
                                WCTries++;
                                if (WCTries >= WCMaxTries) {
                                    Handler.INSTANCE.runTask(() -> player.kickPlayer(kickWC));
                                    return;
                                }
                            }
                            regenerateCaptcha();
                            player.sendMessage(WC.replace("%captcha%", String.valueOf(captcha)));
                            return;
                        }
                    } catch (final NumberFormatException ignored) {
                        if (kickWC != null) {
                            WCTries++;
                            if (WCTries >= WCMaxTries) {
                                Handler.INSTANCE.runTask(() -> player.kickPlayer(kickWC));
                                return;
                            }
                        }
                        regenerateCaptcha();
                        player.sendMessage(WC.replace("%captcha%", String.valueOf(captcha)));
                        return;
                    }
                }

                Handler.INSTANCE.writeData("playerData." + player.getUniqueId() + ".password", Handler.INSTANCE.getHashString(args[1]));

            } else if (sessionType.equals("login")) {
                if (args.length == 0) {
                    if (kickIA != null) {
                        IATries++;
                        if (IATries >= IAMaxTries) {
                            Handler.INSTANCE.runTask(() -> player.kickPlayer(kickIA));
                            return;
                        }
                    }
                    player.sendMessage(IA);
                    return;
                }

                if (!Handler.INSTANCE.getHashString(args[0]).equals(Handler.INSTANCE.getData("playerData." + player.getUniqueId() + ".password"))) {
                    if (kickIP != null) {
                        IPTries++;
                        if (IPTries >= IPMaxTries) {
                            Handler.INSTANCE.runTask(() -> player.kickPlayer(kickIP));
                            return;
                        }
                    }
                    player.sendMessage(IP);
                    return;
                }
            }

            teleportTo(Handler.INSTANCE.getConfig(sessionType + ".completed.teleportation", false).toString());
            player.sendMessage(C);
            if (Boolean.parseBoolean(Handler.INSTANCE.getConfig("protection.autologin.enabled", false).toString()))
                SessionsHandler.INSTANCE.saveSession(player);
            destroySession();
        } catch (final Exception ex) {
            Handler.INSTANCE.runTask(() -> player.kickPlayer(Handler.INSTANCE.getConfig("otherKickMessages.error", true).toString()));
            destroySession();
            ex.printStackTrace();
        }
    }

    public final String getSessionType() {
        return sessionType;
    }

    @SuppressWarnings("deprecation")
    public void destroySession() {
        if (timeoutRunnable != null) {
            timeoutRunnable.cancel(false);
            timeoutRunnable = null;
        }
        if (messageRunnable != null) {
            messageRunnable.cancel(false);
            messageRunnable = null;
        }
        if (moveRunnable != null) {
            moveRunnable.cancel(false);
            moveRunnable = null;
        }

        SessionsHandler.INSTANCE.deleteSession(player.getUniqueId());
        if (player.isOnline() && (!canSeeOthers || !canOthersSee)) {
            for (final Player p : PebbleLogin.INSTANCE.getServer().getOnlinePlayers()) {
                if (!canOthersSee) {
                    if (Handler.INSTANCE.isHighEndAPI())
                        p.showPlayer(PebbleLogin.INSTANCE, player);
                    else
                        p.showPlayer(player);
                }
                if (!canSeeOthers){
                    if (Handler.INSTANCE.isHighEndAPI())
                        player.showPlayer(PebbleLogin.INSTANCE, p);
                    else
                        player.showPlayer(p);
                }
            }
        }
        captcha = captchaMaxNumber = WCMaxTries = IAMaxTries = NMPMaxTries = IPMaxTries = WCTries = IATries = NMPTries = IPTries = 0;
        sessionType = WC = kickWC = IA = kickIA = NMP = kickNMP = IP = kickIP = C = null;

        random = null;

        System.gc();
    }

    private void regenerateCaptcha() {
        captcha = random.nextInt(captchaMaxNumber);
    }

    private void setupRepetitiveMessage() {
        messageRunnable = null;
        if (Boolean.parseBoolean(Handler.INSTANCE.getConfig(sessionType + ".repetitiveMessage.enabled", false).toString())) {
            final String message = Handler.INSTANCE.getConfig(sessionType + ".repetitiveMessage.message", true).toString().replace("%captcha%", String.valueOf(captcha));
            messageRunnable = Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> player.sendMessage(message), 0, Integer.parseInt(Handler.INSTANCE.getConfig(sessionType + ".repetitiveMessage.every", false).toString()), TimeUnit.SECONDS);
        }
    }

    private void setupMovement() {
        if (!Boolean.parseBoolean(Handler.INSTANCE.getConfig("sessionRules.canMove", false).toString())) {
            final Location location = player.getLocation();

            moveRunnable = Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
                if (!player.getLocation().equals(location)) Handler.INSTANCE.runTask(() -> player.teleport(location));
            }, 0, 500, TimeUnit.MICROSECONDS);
        }
    }

    private void setupCaptcha() {
        WC = null;
        kickWC = null;
        WCMaxTries = 0;
        captchaMaxNumber = 0;
        captcha = 0;
        if (sessionType.equals("login")) return;
        if (Boolean.parseBoolean(Handler.INSTANCE.getConfig(sessionType + ".captcha.enabled", false).toString())) {
            captchaMaxNumber = Integer.parseInt(Handler.INSTANCE.getConfig(sessionType + ".captcha.maxNumber", false).toString());
            if (captchaMaxNumber <= 1) {
                captchaMaxNumber = 0;
                Handler.INSTANCE.sendSessionConsoleMessage("§cMax captcha number must be greater than 1, therefor captcha has been disabled for this session.", player.getName(), true);
                return;
            }

            if (Boolean.parseBoolean(Handler.INSTANCE.getConfig(sessionType + ".captcha.tries.enabled", false).toString())) {
                WCMaxTries = Integer.parseInt(Handler.INSTANCE.getConfig(sessionType + ".captcha.tries.tries", false).toString());
                if (WCMaxTries <= 0) {
                    WCMaxTries = 0;
                    Handler.INSTANCE.sendSessionConsoleMessage("§cCaptcha tries must be greater than 0, therefor captcha has been disabled for this session.", player.getName(), true);
                    return;
                }
                kickWC = Handler.INSTANCE.getConfig(sessionType + ".captcha.tries.kick-message", true).toString();
            }

            WC = Handler.INSTANCE.getConfig(sessionType + ".captcha.incorrectMessage", true).toString();
            regenerateCaptcha();
            return;
        }
        captcha = -1;
    }

    private void setupTimeout() {
        timeoutRunnable = null;
        if (Boolean.parseBoolean(Handler.INSTANCE.getConfig(sessionType + ".timeout.enabled", false).toString())) {
            final String timeout = Handler.INSTANCE.getConfig(sessionType + ".timeout.kick-message", true).toString();
            timeoutRunnable = Executors.newSingleThreadScheduledExecutor().schedule(() -> {
                Handler.INSTANCE.runTask(() -> player.kickPlayer(timeout));
                destroySession();
            }, Integer.parseInt(Handler.INSTANCE.getConfig(sessionType + ".timeout.time", false).toString()), TimeUnit.SECONDS);
        }
    }

    private void setupIncorrectArguments() {
        IA = Handler.INSTANCE.getConfig(sessionType + ".incorrectArguments.message", true).toString();
        kickIA = null;
        IAMaxTries = 0;
        if (Boolean.parseBoolean(Handler.INSTANCE.getConfig(sessionType + ".incorrectArguments.tries.enabled", false).toString())) {
            IAMaxTries = Integer.parseInt(Handler.INSTANCE.getConfig(sessionType + ".incorrectArguments.tries.tries", false).toString());
            if (IAMaxTries <= 0) {
                Handler.INSTANCE.sendSessionConsoleMessage("§cArgument tries must be greater than 0, therefor argument tries has been disabled for this session.", player.getName(), true);
                IAMaxTries = 0;
                return;
            }

            kickIA = Handler.INSTANCE.getConfig(sessionType + ".incorrectArguments.tries.kick-message", true).toString();
        }
    }

    private void setupNonMatchingPasswords() {
        NMP = null;
        kickNMP = null;
        NMPMaxTries = 0;
        if (sessionType.equals("login")) return;

        NMP = Handler.INSTANCE.getConfig(sessionType + ".nonMatchingPasswords.message", true).toString();
        if (Boolean.parseBoolean(Handler.INSTANCE.getConfig(sessionType + ".nonMatchingPasswords.tries.enabled", false).toString())) {
            NMPMaxTries = Integer.parseInt(Handler.INSTANCE.getConfig(sessionType + ".nonMatchingPasswords.tries.tries", false).toString());
            if (NMPMaxTries <= 0) {
                Handler.INSTANCE.sendSessionConsoleMessage("§cNon Matching Password tries must be greater than 0, therefor non matching password tries has been disabled for this session.", player.getName(), true);
                NMPMaxTries = 0;
                return;
            }

            kickNMP = Handler.INSTANCE.getConfig(sessionType + ".nonMatchingPasswords.tries.kick-message", true).toString();
        }
    }

    private void setupCompleted() {
        C = Handler.INSTANCE.getConfig(sessionType + ".completed.message", true).toString();
    }

    private void setupIncorrectPassword() {
        IP = null;
        kickIP = null;
        IPMaxTries = 0;
        if (sessionType.equals("register")) return;

        IP = Handler.INSTANCE.getConfig(sessionType + ".incorrectPassword.message", true).toString();
        if (Boolean.parseBoolean(Handler.INSTANCE.getConfig(sessionType + ".incorrectPassword.tries.enabled", false).toString())) {
            IPMaxTries = Integer.parseInt(Handler.INSTANCE.getConfig(sessionType + ".incorrectPassword.tries.tries", false).toString());
            if (IPMaxTries <= 0) {
                Handler.INSTANCE.sendSessionConsoleMessage("§cIncorrect Password tries must be greater than 0, therefor incorrect password tries has been disabled for this session.", player.getName(), true);
                IPMaxTries = 0;
                return;
            }

            kickIP = Handler.INSTANCE.getConfig(sessionType + ".incorrectPassword.tries.kick-message", true).toString();
        }
    }

    @SuppressWarnings("deprecation")
    private void setupVisibility() {
        final PebbleLogin main = PebbleLogin.INSTANCE;
        canOthersSee = Boolean.parseBoolean(Handler.INSTANCE.getConfig("sessionRules.canOthersSee", false).toString());
        canSeeOthers = Boolean.parseBoolean(Handler.INSTANCE.getConfig("sessionRules.canSeeOthers", false).toString());
        if (!canSeeOthers || !canOthersSee) {
            for (final Player p : main.getServer().getOnlinePlayers()) {
                if (!canOthersSee) {
                    if (Handler.INSTANCE.isHighEndAPI())
                        p.hidePlayer(main, player);
                    else
                        p.hidePlayer(player);
                }
                if (!canSeeOthers){
                    if (Handler.INSTANCE.isHighEndAPI())
                        player.hidePlayer(main, p);
                    else
                        player.hidePlayer(p);
                }
            }
        }
    }

    private void teleportTo(final String teleportation) {
        new Thread(() -> {
            Object o;
            if (teleportation.equals("lobby")) {
                o = Handler.INSTANCE.getData("locations.lobby");
                if (o instanceof Location) Handler.INSTANCE.runTask(() -> player.teleport((Location) o));
                return;
            }

            if (teleportation.equals("login")) {
                o = Handler.INSTANCE.getData("locations.login");
                if (o instanceof Location) Handler.INSTANCE.runTask(() -> player.teleport((Location) o));
                return;
            }

            if (teleportation.equals("saved")) {
                o = Handler.INSTANCE.getData("playerData." + player.getUniqueId() + ".savedLocation");
                if (o instanceof Location) Handler.INSTANCE.runTask(() -> player.teleport((Location) o));
            }
        }).start();
    }
}

