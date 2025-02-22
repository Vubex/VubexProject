package de.flavius.vubex.vubexproxy.commands;

import de.flavius.vubex.vubexproxy.Vubex_proxy;
import de.flavius.vubex.vubexproxy.api.VubexDiscordAPI;
import de.flavius.vubex.vubexproxy.mysql.MySQLManager;
import de.flavius.vubex.vubexproxy.utils.BanManager;
import de.flavius.vubex.vubexproxy.utils.ChatUtils;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;
import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.URL;
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
public class BanCommand extends Command implements TabExecutor {

    private final MySQLManager mysqlManager;
    private final BanManager banManager;
    private final VubexDiscordAPI vubexDiscordAPI;
    private final Random random = new Random();

    public BanCommand(MySQLManager mysqlManager, VubexDiscordAPI vubexDiscordAPI) {
        super("ban", null);
        this.mysqlManager = mysqlManager;
        this.banManager = new BanManager(mysqlManager);
        this.vubexDiscordAPI = vubexDiscordAPI;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(!sender.hasPermission("vubex.ban")){
            sender.sendMessage(new TextComponent(Vubex_proxy.serverPrefix + ChatUtils.getColoredText("&7Diesen &7Befehl &7darfst &7du &7nicht &7ausführen.")));
            return;
        }

        String banDescription = null;
        if (args.length < 2) {
            sender.sendMessage(new TextComponent(Vubex_proxy.serverPrefix + ChatUtils.getColoredText("&7Verwendung: &e/ban &e[Spieler] &e[Grund-Nummer] &e<Beschreibung>")));
            return;
        }
        if (args.length > 2) {
            banDescription = concatenateArgs(args);
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

        if (reasonNumber == 99 && !sender.hasPermission("vubex.ban.adminban")) {
            sender.sendMessage(new TextComponent(Vubex_proxy.serverPrefix + "Du darfst diesen Grund nicht nutzen."));
            return;
        }

        int durationDays = calculateBanDurationDays(reasonNumber);
        String duration = durationDays + "d";

        if (targetPlayer != null && targetPlayer.isConnected()) {
            if(targetPlayer.getUniqueId().toString().equals("ba671a5b-5c79-448e-b40b-8bbd64445472") || targetPlayer.getUniqueId().toString().equals("d9c2256d-e13b-4c8c-b3c9-3461033af680")) {
                if (sender != targetPlayer){
                    sender.sendMessage(new TextComponent(Vubex_proxy.serverPrefix + ChatUtils.getColoredText("&7Bei dieser Person darfst du den Befehl nicht benutzen.")));
                    targetPlayer.sendMessage(new TextComponent(Vubex_proxy.serverPrefix + ChatUtils.getColoredText("&e" + ((ProxiedPlayer) sender).getDisplayName() + "&7 hat versucht dich zu Bannen.")));
                }
                return;
            }

            try (Connection connection = mysqlManager.getConnection()) {
                if (isPlayerAlreadyBanned(connection, targetPlayer.getUniqueId().toString())) {
                    UUID UUIDhere = ChatUtils.getUUIDFromPlayerName(args[0]);
                    if (UUIDhere == null){
                        sender.sendMessage(new TextComponent(Vubex_proxy.serverPrefix + "Spieler ist bereits gebannt. " + ChatColor.YELLOW + "/baninfo " + ChatColor.YELLOW + args[0]));
                    }else{
                        sender.sendMessage(new TextComponent(Vubex_proxy.serverPrefix + "Spieler ist bereits gebannt. " + ChatColor.YELLOW + "/baninfo " + ChatColor.YELLOW + ChatUtils.getOfflinePlayerName(UUIDhere)));
                    }
                    return;
                }

                try (PreparedStatement statement = connection.prepareStatement(
                        "INSERT INTO bans (uuid, reason, duration, banned_by, ip, ban_id, country, region, city, proxy, isp, ban_active, ban_time, ban_description) " +
                                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")) {

                    String banId = generateUniqueBanId(connection);

                    try {
                        // Fetch IP location information
                        String apiUrl = "http://ip-api.com/xml/" + getTargetIP(targetPlayer) + "?fields=country,regionName,city,isp,proxy&lang=de";
                        String locationResponse = fetchUrlContent(apiUrl);

                        String isp = parseValueFromXmlTag(locationResponse, "isp");
                        String proxy = parseValueFromXmlTag(locationResponse, "proxy");
                        String country = parseValueFromXmlTag(locationResponse, "country");
                        String region = parseValueFromXmlTag(locationResponse, "regionName");
                        String city = parseValueFromXmlTag(locationResponse, "city");

                        statement.setString(7, country);
                        statement.setString(8, region);
                        statement.setString(9, city);
                        statement.setString(10, proxy);
                        statement.setString(11, isp);

                    } catch (IOException e) {
                        Vubex_proxy.getInstance().getLogger().log(Level.SEVERE, "Fehler beim Abrufen der IP-Informationen.", e);
                    }

                    statement.setString(1, targetPlayer.getUniqueId().toString());
                    statement.setInt(2, reasonNumber);

                    if (reasonNumber == 8 || reasonNumber == 9 || reasonNumber == 99) {
                        // Set duration to permanent for reasons 8, 9, and 99
                        duration = "Permanent";
                    }
                    statement.setString(3, duration);

                    if(sender instanceof ProxiedPlayer){
                        statement.setString(4, ((ProxiedPlayer) sender).getUniqueId().toString());
                    }else {
                        statement.setString(4, ("1b6b1876-df7f-4ac5-b352-84aa28a5749c"));
                    }
                    statement.setString(5, getTargetIP(targetPlayer));
                    statement.setString(6, banId);
                    statement.setInt(12, 1);
                    statement.setTimestamp(13, new java.sql.Timestamp(System.currentTimeMillis()));
                    statement.setString(14, banDescription);
                    statement.executeUpdate();

                    String banMessage;
                    String title = "Team-Information";
                    String[] lines;
                    String iconUrl = "https://mc-heads.net/head/" + targetPlayer.getUniqueId() + "/600";
                    Color color = Color.ORANGE;
                    String beschreibung = "";
                    if(duration.equals("Permanent")){
                        banMessage = "&8« &e&lVubex.DE &r&8»\n\n" +
                                "&cDu bist nun Permanent von unserem Netzwerk ausgeschlossen.\n\n" +
                                "&7Grund: &e" + getBanReason(reasonNumber) + "\n\n\n" +
                                "&7Ban-ID: &e" + banId + "\n\n" +
                                "&7Unrecht gebannt? Wir helfen dir im Forum gerne weiter.\n" +
                                "&ehttps://vubex.de/forum";
                        targetPlayer.disconnect(new TextComponent(ChatUtils.getColoredText(banMessage)));

                        String broadcastMessage = Vubex_proxy.serverPrefix + "\n"
                                + Vubex_proxy.serverPrefix + ChatUtils.getColoredText("&e" + targetPlayer.getName() + "&7 wurde von &e" + sender.getName() + "&7 gebannt.") + "\n"
                                + Vubex_proxy.serverPrefix + "\n"
                                + Vubex_proxy.serverPrefix + ChatUtils.getColoredText(Vubex_proxy.serverPrefix + "&7Grund: &e" + getBanReason(reasonNumber) + "&7,") + "\n"
                                + Vubex_proxy.serverPrefix + ChatUtils.getColoredText(Vubex_proxy.serverPrefix + "&7Dauer: &ePermanent&7, Ban-ID: &e" + banId) + "\n"
                                + Vubex_proxy.serverPrefix;
                        sendBroadcastMessage(sender, broadcastMessage);

                        if(banDescription != null){
                            beschreibung = "\nBeschreibung: " + banDescription;
                        }
                        lines = new String[]{"Der Spieler **" + targetPlayer.getName() + "** wurde von **" + sender.getName() + "** gebannt.\n" +
                                "Grund: **" + getBanReason(reasonNumber) + "**\n" +
                                "Dauer: **Permanent**\n" +
                                "Ban-ID: **" + banId + "**\n" +
                                beschreibung};
                    }else{
                        banMessage = "&8« &e&lVubex.DE &r&8»\n\n" +
                                "&cDu bist nun vorübergehend von unserem Netzwerk ausgeschlossen.\n\n" +
                                "&7Grund: &e" + getBanReason(reasonNumber) + "\n\n" +
                                "&7Dauer &e" + duration.replace("d", " Tage") + "\n\n\n" +
                                "&7Ban-ID: &e" + banId + "\n\n" +
                                "&7Unrecht gebannt? Wir helfen dir im Forum gerne weiter.\n" +
                                "&ehttps://vubex.de/forum";
                        targetPlayer.disconnect(new TextComponent(ChatUtils.getColoredText(banMessage)));

                        String broadcastMessage = Vubex_proxy.serverPrefix + "\n"
                                + Vubex_proxy.serverPrefix + ChatUtils.getColoredText("&e" + targetPlayer.getName() + "&7 wurde von &e" + sender.getName() + "&7 gebannt.") + "\n"
                                + Vubex_proxy.serverPrefix + "\n"
                                + Vubex_proxy.serverPrefix + ChatUtils.getColoredText(Vubex_proxy.serverPrefix + "&7Grund: &e" + getBanReason(reasonNumber) + "&7,") + "\n"
                                + Vubex_proxy.serverPrefix + ChatUtils.getColoredText(Vubex_proxy.serverPrefix + "&7Dauer: &e" + duration + "&7, Ban-ID: &e" + banId) + "\n"
                                + Vubex_proxy.serverPrefix;
                        sendBroadcastMessage(sender, broadcastMessage);

                        if(banDescription != null){
                            beschreibung = "\nBeschreibung: " + banDescription;
                        }
                        lines = new String[]{"Der Spieler **" + targetPlayer.getName() + "** wurde von **" + sender.getName() + "** gebannt.\n" +
                                "Grund: **" + getBanReason(reasonNumber) + "**\n" +
                                "Dauer: **" + durationDays + " Tage**\n" +
                                "Ban-ID: **" + banId + "**" +
                                beschreibung};
                    }
                    vubexDiscordAPI.sendEmbedMessage("1222293450284863498", title, lines, iconUrl, color);
                } catch (SQLException e) {
                    Vubex_proxy.getInstance().getLogger().log(Level.SEVERE, "Ein Fehler ist während dem Spieler Bannen aufgetreten", e);
                }
            } catch (SQLException e) {
                Vubex_proxy.getInstance().getLogger().log(Level.SEVERE, "Ein Fehler ist beim Kontaktieren der Datenbank aufgetreten.", e);
            }
        } else {
            UUID targetUUID = getUUIDFromDatabase(targetName);
            UUID bannedBy;
            if (targetUUID != null) {
                if(sender instanceof ProxiedPlayer){
                    bannedBy = ((ProxiedPlayer)sender).getUniqueId();
                }else{
                    bannedBy = UUID.fromString("1b6b1876-df7f-4ac5-b352-84aa28a5749c");
                }
                if(targetUUID.toString().equals("ba671a5b-5c79-448e-b40b-8bbd64445472") || targetUUID.toString().equals("d9c2256d-e13b-4c8c-b3c9-3461033af680")) {
                    if (sender != targetPlayer && targetPlayer != null && sender instanceof ProxiedPlayer){
                        sender.sendMessage(new TextComponent(Vubex_proxy.serverPrefix + ChatUtils.getColoredText("&7Bei dieser Person darfst du den Befehl nicht benutzen.")));
                        targetPlayer.sendMessage(new TextComponent(Vubex_proxy.serverPrefix + ChatUtils.getColoredText("&e" + ((ProxiedPlayer) sender).getDisplayName() + "&7 hat versucht dich zu Bannen!.")));
                    }
                    return;
                }
                try {
                    try (Connection connection = mysqlManager.getConnection()) {
                        if (banManager.isPlayerAlreadyBanned(connection, targetUUID.toString())) {
                            sender.sendMessage(new TextComponent(Vubex_proxy.serverPrefix + "Spieler ist bereits gebannt."));
                            return;
                        }
                    } catch (SQLException e) {
                        Vubex_proxy.getInstance().getLogger().log(Level.SEVERE, "Ein Fehler ist aufgetreten beim Checken ob der Spieler schon gebannt ist.", e);
                    }
                    if (reasonNumber == 8 || reasonNumber == 9 || reasonNumber == 99) {
                        // Set duration to permanent for reasons 8, 9, and 99
                        duration = "Permanent";
                    }

                    if (banManager.banOfflinePlayer(targetUUID, bannedBy, reasonNumber, duration, generateUniqueBanId(mysqlManager.getConnection()))) {
                        sender.sendMessage(new TextComponent(Vubex_proxy.serverPrefix + "Offline-Spieler erfolgreich gebannt."));
                        String banId = null;
                        try (Connection connection = mysqlManager.getConnection();
                             PreparedStatement selectStatement = connection.prepareStatement(
                                     "SELECT ban_id FROM bans WHERE uuid = ? AND ban_active = true"
                             )) {
                            selectStatement.setString(1, targetUUID.toString());

                            try (ResultSet resultSet = selectStatement.executeQuery()) {
                                if (resultSet.next()) {
                                    banId = resultSet.getString("ban_id");
                                }
                            }
                        } catch (SQLException e) {
                            Vubex_proxy.getInstance().getLogger().log(Level.SEVERE, "Ein Fehler ist beim Bannen des Offline-Spielers aufgetreten. 1", e);
                        }

                        String title = "Team-Information";
                        String[] lines;
                        String iconUrl = "https://mc-heads.net/head/" + targetUUID + "/600";
                        Color color = Color.ORANGE;
                        String beschreibung = "";
                        if(duration.equals("Permanent")){
                            String broadcastMessage = Vubex_proxy.serverPrefix + "\n"
                                    + Vubex_proxy.serverPrefix + ChatUtils.getColoredText("&e" + ChatUtils.getOfflinePlayerName(targetUUID) + "&7 wurde von &e" + sender.getName() + "&7 gebannt.") + "\n"
                                    + Vubex_proxy.serverPrefix + "\n"
                                    + Vubex_proxy.serverPrefix + ChatUtils.getColoredText(Vubex_proxy.serverPrefix + "&7Grund: &e" + getBanReason(reasonNumber) + "&7,") + "\n"
                                    + Vubex_proxy.serverPrefix + ChatUtils.getColoredText(Vubex_proxy.serverPrefix + "&7Dauer: &ePermanent&7, Ban-ID: &e" + banId) + "\n"
                                    + Vubex_proxy.serverPrefix;

                            sendBroadcastMessage(sender, broadcastMessage);

                            if(banDescription != null){
                                beschreibung = "\nBeschreibung: " + banDescription;
                            }
                            lines = new String[]{"Der Spieler **" + ChatUtils.getOfflinePlayerName(targetUUID) + "** wurde von **" + sender.getName() + "** gebannt.\n" +
                                    "Grund: **" + getBanReason(reasonNumber) + "**\n" +
                                    "Dauer: **Permanent**\n" +
                                    "Ban-ID: **" + banId + "**\n" +
                                    beschreibung};
                        }else{
                            String broadcastMessage = Vubex_proxy.serverPrefix + "\n"
                                    + Vubex_proxy.serverPrefix + ChatUtils.getColoredText("&e" + ChatUtils.getOfflinePlayerName(targetUUID) + "&7 wurde von &e" + sender.getName() + "&7 gebannt.") + "\n"
                                    + Vubex_proxy.serverPrefix + "\n"
                                    + Vubex_proxy.serverPrefix + ChatUtils.getColoredText(Vubex_proxy.serverPrefix + "&7Grund: &e" + getBanReason(reasonNumber) + "&7,") + "\n"
                                    + Vubex_proxy.serverPrefix + ChatUtils.getColoredText(Vubex_proxy.serverPrefix + "&7Dauer: &e" + durationDays + "&e Tage&7, Ban-ID: &e" + banId) + "\n"
                                    + Vubex_proxy.serverPrefix;

                            sendBroadcastMessage(sender, broadcastMessage);

                            if(banDescription != null){
                                beschreibung = "\nBeschreibung: " + banDescription;
                            }
                            lines = new String[]{"Der Spieler **" + ChatUtils.getOfflinePlayerName(targetUUID) + "** wurde von **" + sender.getName() + "** gebannt.\n" +
                                    "Grund: **" + getBanReason(reasonNumber) + "**\n" +
                                    "Dauer: **" + durationDays + " Tage**\n" +
                                    "Ban-ID: **" + banId + "**" +
                                    beschreibung};
                        }
                        vubexDiscordAPI.sendEmbedMessage("1222293450284863498", title, lines, iconUrl, color);
                    } else {
                        sender.sendMessage(new TextComponent(Vubex_proxy.serverPrefix + "Fehler beim Bannen des Offline-Spielers."));
                    }
                } catch (SQLException e) {
                    Vubex_proxy.getInstance().getLogger().log(Level.SEVERE, "Ein Fehler ist beim Bannen des Offline-Spielers aufgetreten. 2", e);
                }
            } else {
                sender.sendMessage(new TextComponent(Vubex_proxy.serverPrefix + "Spieler nicht gefunden."));
            }
        }
    }

    private UUID getUUIDFromDatabase(String playerName) {
        try (Connection connection = mysqlManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT uuid FROM users WHERE last_player_name = ?")) {

            statement.setString(1, playerName);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return UUID.fromString(resultSet.getString("uuid"));
                }
            }
        } catch (SQLException e) {
            Vubex_proxy.getInstance().getLogger().log(Level.SEVERE, "Fehler beim Abrufen der UUID aus der Datenbank.", e);
        }
        return null;
    }

    private boolean isPlayerAlreadyBanned(Connection connection, String uuid) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT * FROM bans WHERE uuid = ? AND ban_active = 1")) {
            statement.setString(1, uuid);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    private String getTargetIP(ProxiedPlayer target) {
        InetSocketAddress targetSocketAddress = (InetSocketAddress) target.getSocketAddress();
        return targetSocketAddress.getAddress().getHostAddress();
    }

    private String getBanReason(int reasonNumber) {
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
            case 99 -> "Admin Ban";
            default -> "Unbekannter Grund (Ban Bis der Grund geklärt ist.)";
        };
    }
    private int calculateBanDurationDays(int reasonNumber) {
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

    private String generateUniqueBanId(Connection connection) throws SQLException {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder banId = new StringBuilder();

        while (true) {
            for (int i = 0; i < 6; i++) {
                banId.append(characters.charAt(random.nextInt(characters.length())));
            }

            try (PreparedStatement checkStatement = connection.prepareStatement("SELECT * FROM bans WHERE ban_id = ?")) {
                checkStatement.setString(1, banId.toString());
                ResultSet resultSet = checkStatement.executeQuery();

                if (!resultSet.next()) {
                    return banId.toString();
                }
            } catch (SQLException e) {
                Vubex_proxy.getInstance().getLogger().log(Level.SEVERE, "Ein Fehler ist beim Generieren einer BanID aufgetreten.", e);
            }

            banId.setLength(0);
        }
    }

    private void sendBroadcastMessage(CommandSender sender, String message) {
        Vubex_proxy.getInstance().getProxy().getPlayers().stream()
                .filter(player -> player.hasPermission("vubex.ban"))
                .forEach(player -> player.sendMessage(new TextComponent(message)));

        Vubex_proxy.getInstance().getLogger().info(sender.getName() + " sent a broadcast message: " + message);
    }

    private String fetchUrlContent(String apiUrl) throws IOException {
        URL url = new URL(apiUrl);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            return response.toString();
        }
    }

    private String parseValueFromXmlTag(String xml, String tag) {
        int startIndex = xml.indexOf("<" + tag + ">") + tag.length() + 2;
        int endIndex = xml.indexOf("</" + tag + ">");
        return xml.substring(startIndex, endIndex);
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        List<String> completions = new ArrayList<>();
        if(sender.hasPermission("vubex.ban") && args.length == 1){
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