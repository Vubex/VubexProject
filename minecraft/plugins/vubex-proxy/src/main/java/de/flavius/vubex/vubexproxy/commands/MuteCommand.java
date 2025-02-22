package de.flavius.vubex.vubexproxy.commands;

import de.flavius.vubex.vubexproxy.Vubex_proxy;
import de.flavius.vubex.vubexproxy.api.VubexDiscordAPI;
import de.flavius.vubex.vubexproxy.mysql.MySQLManager;
import de.flavius.vubex.vubexproxy.utils.ChatUtils;
import de.flavius.vubex.vubexproxy.utils.MuteManager;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.logging.Level;

/**
 * @author : flavius
 * project : VubexProject
 **/
public class MuteCommand extends Command implements TabExecutor {

    private final MySQLManager mysqlManager;
    private final MuteManager muteManager;
    private final VubexDiscordAPI vubexDiscordAPI;
    private final Random random = new Random();

    public MuteCommand(MySQLManager mysqlManager, VubexDiscordAPI vubexDiscordAPI) {
        super("mute", null);
        this.mysqlManager = mysqlManager;
        this.muteManager = new MuteManager(mysqlManager);
        this.vubexDiscordAPI = vubexDiscordAPI;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(!sender.hasPermission("vubex.mute")){
            sender.sendMessage(new TextComponent(Vubex_proxy.serverPrefix + ChatUtils.getColoredText("&7Diesen &7Befehl &7darfst &7du &7nicht &7ausführen.")));
            return;
        }

        String muteDescription = null;
        if (args.length < 2) {
            sender.sendMessage(new TextComponent(Vubex_proxy.serverPrefix + ChatUtils.getColoredText("&7Verwendung: &e/mute &e[Spieler] &e[Grund-Nummer] &e<Beschreibung>")));
            return;
        }
        if (args.length > 2) {
            muteDescription = concatenateArgs(args);
        }

        String targetName = args[0];
        ProxiedPlayer targetPlayer = ProxyServer.getInstance().getPlayer(targetName);
        String reasonArg = args[1];
        int reasonNumber;
        try {
            reasonNumber = Integer.parseInt(reasonArg);

            // Überprüfe, ob die Zahl zwischen 1 und 99 liegt und maximal 2 Zeichen lang ist
            if(reasonNumber != 99 && !(reasonNumber >= 0 && reasonNumber <= 9 && reasonArg.length() <= 2)){
                sender.sendMessage(new TextComponent(Vubex_proxy.serverPrefix + "Ungültige Grund-Nummer."));
                return;
            }

        } catch (NumberFormatException e) {
            // Das Argument ist keine gültige Zahl
            sender.sendMessage(new TextComponent(Vubex_proxy.serverPrefix + "Ungültige Grund-Nummer."));
            return;
        }

        if (reasonNumber == 99 && !sender.hasPermission("vubex.mute.adminmute")) {
            sender.sendMessage(new TextComponent(Vubex_proxy.serverPrefix + "Du darfst diesen Grund nicht nutzen."));
            return;
        }

        int durationDays = calculateMuteDurationDays(reasonNumber);
        String duration = durationDays + "d";

        UUID targetUUID = ChatUtils.getUUIDFromPlayerName(targetName);
        UUID mutedBy;
        if (targetUUID != null) {
            if(sender instanceof ProxiedPlayer){
                mutedBy = ((ProxiedPlayer)sender).getUniqueId();
            }else{
                mutedBy = UUID.fromString("1b6b1876-df7f-4ac5-b352-84aa28a5749c");
            }
            if(targetUUID.toString().equals("ba671a5b-5c79-448e-b40b-8bbd64445472") || targetUUID.toString().equals("d9c2256d-e13b-4c8c-b3c9-3461033af680")) {
                if (sender != targetPlayer){
                    sender.sendMessage(new TextComponent(Vubex_proxy.serverPrefix + ChatUtils.getColoredText("&7Bei dieser Person darfst du den Befehl nicht benutzen.")));
                    if (targetPlayer != null && sender instanceof ProxiedPlayer) {
                        targetPlayer.sendMessage(new TextComponent(Vubex_proxy.serverPrefix + ChatUtils.getColoredText("&e" + ((ProxiedPlayer) sender).getDisplayName() + "&7 hat versucht dich zu muten!.")));
                    }
                }
                return;
            }
            try {
                try (Connection connection = mysqlManager.getConnection()) {
                    if (muteManager.isPlayerAlreadyMuted(connection, targetUUID.toString())) {
                        sender.sendMessage(new TextComponent(Vubex_proxy.serverPrefix + "Spieler ist bereits Stummgeschaltet."));
                        return;
                    }
                } catch (SQLException e) {
                    Vubex_proxy.getInstance().getLogger().log(Level.SEVERE, "Ein Fehler ist aufgetreten beim Checken ob der Spieler schon Stummgeschaltet ist.", e);
                }
                if (reasonNumber == 8 || reasonNumber == 9 || reasonNumber == 99) {
                    // Set duration to permanent for reasons 8, 9, and 99
                    duration = "Permanent";
                }

                if (muteManager.mutePlayer(targetUUID, mutedBy, reasonNumber, duration, generateUniqueMuteId(mysqlManager.getConnection()))) {
                    sender.sendMessage(new TextComponent(Vubex_proxy.serverPrefix + "Spieler erfolgreich Stummgeschaltet."));
                    String muteId = null;
                    try (Connection connection = mysqlManager.getConnection();
                         PreparedStatement selectStatement = connection.prepareStatement(
                                 "SELECT mute_id FROM mutes WHERE uuid = ? AND mute_active = true"
                         )) {
                        selectStatement.setString(1, targetUUID.toString());

                        try (ResultSet resultSet = selectStatement.executeQuery()) {
                            if (resultSet.next()) {
                                muteId = resultSet.getString("mute_id");
                            }
                        }
                    } catch (SQLException e) {
                        Vubex_proxy.getInstance().getLogger().log(Level.SEVERE, "Ein Fehler ist beim Muten des Spielers aufgetreten. 1", e);
                    }

                    String title = "Team-Information";
                    String[] lines;
                    String iconUrl = "https://mc-heads.net/head/" + targetUUID + "/600";
                    Color color = Color.ORANGE;
                    String beschreibung = "";
                    if(duration.equals("Permanent")){
                        String broadcastMessage = Vubex_proxy.serverPrefix + "\n"
                                + Vubex_proxy.serverPrefix + ChatUtils.getColoredText("&e" + ChatUtils.getOfflinePlayerName(targetUUID) + "&7 wurde von &e" + sender.getName() + "&7 Stummgeschaltet.") + "\n"
                                + Vubex_proxy.serverPrefix + "\n"
                                + Vubex_proxy.serverPrefix + ChatUtils.getColoredText(Vubex_proxy.serverPrefix + "&7Grund: &e" + getMuteReason(reasonNumber) + "&7,") + "\n"
                                + Vubex_proxy.serverPrefix + ChatUtils.getColoredText(Vubex_proxy.serverPrefix + "&7Dauer: &ePermanent&7, Mute-ID: &e" + muteId) + "\n"
                                + Vubex_proxy.serverPrefix;

                        sendBroadcastMessage(sender, broadcastMessage);

                        if(muteDescription != null){
                            beschreibung = "\nBeschreibung: " + muteDescription;
                        }
                        lines = new String[]{"Der Spieler **" + ChatUtils.getOfflinePlayerName(targetUUID) + "** wurde von **" + sender.getName() + "** Stummgeschaltet.\n" +
                                "Grund: **" + getMuteReason(reasonNumber) + "**\n" +
                                "Dauer: **Permanent**\n" +
                                "Mute-ID: **" + muteId + "**\n" +
                                beschreibung};
                    }else{
                        String broadcastMessage = Vubex_proxy.serverPrefix + "\n"
                                + Vubex_proxy.serverPrefix + ChatUtils.getColoredText("&e" + ChatUtils.getOfflinePlayerName(targetUUID) + "&7 wurde von &e" + sender.getName() + "&7 Stummgeschaltet.") + "\n"
                                + Vubex_proxy.serverPrefix + "\n"
                                + Vubex_proxy.serverPrefix + ChatUtils.getColoredText(Vubex_proxy.serverPrefix + "&7Grund: &e" + getMuteReason(reasonNumber) + "&7,") + "\n"
                                + Vubex_proxy.serverPrefix + ChatUtils.getColoredText(Vubex_proxy.serverPrefix + "&7Dauer: &e" + durationDays + "&e Tage&7, Mute-ID: &e" + muteId) + "\n"
                                + Vubex_proxy.serverPrefix;

                        sendBroadcastMessage(sender, broadcastMessage);

                        if(muteDescription != null){
                            beschreibung = "\nBeschreibung: " + muteDescription;
                        }
                        lines = new String[]{"Der Spieler **" + ChatUtils.getOfflinePlayerName(targetUUID) + "** wurde von **" + sender.getName() + "** Stummgeschaltet.\n" +
                                "Grund: **" + getMuteReason(reasonNumber) + "**\n" +
                                "Dauer: **" + durationDays + " Tage**\n" +
                                "Mute-ID: **" + muteId + "**" +
                                beschreibung};
                    }
                    vubexDiscordAPI.sendEmbedMessage("1222293450284863498", title, lines, iconUrl, color);
                } else {
                    sender.sendMessage(new TextComponent(Vubex_proxy.serverPrefix + "Fehler beim Muten des Spielers."));
                }
            } catch (SQLException e) {
                Vubex_proxy.getInstance().getLogger().log(Level.SEVERE, "Ein Fehler ist beim Muten des Spielers aufgetreten. 2", e);
            }
        } else {
            sender.sendMessage(new TextComponent(Vubex_proxy.serverPrefix + "Spieler nicht gefunden."));
        }
    }

    private String getMuteReason(int reasonNumber) {
        return switch (reasonNumber) {
            case 1 -> "Unerlaubte Clientmodification/Hackclient";
            case 2 -> "Reportmissbrauch";
            case 3 -> "Unangebrachter Skin/Name";
            case 4 -> "Bugusing";
            case 5 -> "Chat Verhalten";
            case 6 -> "Werbung";
            case 7 -> "Alt-Account";
            case 8 -> "Bannumgehung";
            case 9 -> "Sicherheitsbann";
            case 99 -> "Admin Mute";
            default -> "Unbekannter Grund (Ban Bis der Grund geklärt ist.)";
        };
    }
    private int calculateMuteDurationDays(int reasonNumber) {
        return switch (reasonNumber) {
            case 1 -> 30; // 1 Monat
            case 2, 4, 6 -> 7; // 1 Woche
            case 3 -> 2; // 2 Tage
            case 5 -> 14; // 2 Wochen
            case 7 -> 5; // 5 Tage
            case 8, 9, 99 -> 1; // Permanent - mit if abfrage oben
            default -> 3; // 3 Tage bis der Ban geklärt ist
        };
    }

    private String concatenateArgs(String[] args) {
        StringBuilder description = new StringBuilder();
        for (int i = 2; i < args.length; i++) {
            description.append(args[i]);
            if (i < args.length - 1) {
                description.append(" ");
            }
        }
        return description.toString();
    }

    private String generateUniqueMuteId(Connection connection) throws SQLException {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder muteId = new StringBuilder();

        while (true) {
            for (int i = 0; i < 6; i++) {
                muteId.append(characters.charAt(random.nextInt(characters.length())));
            }

            try (PreparedStatement checkStatement = connection.prepareStatement("SELECT * FROM mutes WHERE mute_id = ?")) {
                checkStatement.setString(1, muteId.toString());
                ResultSet resultSet = checkStatement.executeQuery();

                if (!resultSet.next()) {
                    return muteId.toString();
                }
            } catch (SQLException e) {
                Vubex_proxy.getInstance().getLogger().log(Level.SEVERE, "Ein Fehler ist beim Generieren einer MuteID aufgetreten.", e);
            }

            muteId.setLength(0);
        }
    }

    private void sendBroadcastMessage(CommandSender sender, String message) {
        Vubex_proxy.getInstance().getProxy().getPlayers().stream()
                .filter(player -> player.hasPermission("vubex.mute"))
                .forEach(player -> player.sendMessage(new TextComponent(message)));

        Vubex_proxy.getInstance().getLogger().info(sender.getName() + " sent a broadcast message: " + message);
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        List<String> completions = new ArrayList<>();
        if(sender.hasPermission("vubex.mute") && args.length == 1){
            try (Connection connection = mysqlManager.getConnection();
                 PreparedStatement selectStatement = connection.prepareStatement(
                         "SELECT uuid FROM users"
                 );
                 ResultSet resultSet = selectStatement.executeQuery()) {

                while (resultSet.next()) {
                    String uuidString = resultSet.getString("uuid");
                    UUID uuid = UUID.fromString(uuidString);
                    String playerName = ChatUtils.getOfflinePlayerName(uuid);

                    while (resultSet.next()) {
                        String partialName = args[0].toLowerCase();
                        if(playerName.toLowerCase().startsWith(partialName)){
                            completions.add(playerName);
                        }
                    }
                }
            } catch (SQLException e) {
                Vubex_proxy.getInstance().getLogger().log(Level.SEVERE, "Ein Fehler ist aufgetreten, während Spieler UUIDs von der Datenbank Users abgerufen wurden.", e);
            }
        }

        return completions;
    }
}