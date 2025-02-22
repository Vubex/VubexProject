package de.flavius.vubex.vubexproxy.commands;

import de.flavius.vubex.vubexproxy.Vubex_proxy;
import de.flavius.vubex.vubexproxy.mysql.MySQLManager;
import de.flavius.vubex.vubexproxy.utils.ChatUtils;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;
import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

/**
 * @author : flavius
 * project : VubexProject
 **/
public class BanInfoCommand extends Command implements TabExecutor {

    private final MySQLManager mysqlManager;

    public BanInfoCommand(MySQLManager mysqlManager) {
        super("baninfo", null);
        this.mysqlManager = mysqlManager;
    }

    public void execute(CommandSender sender, String[] args) {
        if(!sender.hasPermission("vubex.baninfo")){
            sender.sendMessage(new TextComponent(Vubex_proxy.serverPrefix + ChatUtils.getColoredText("&7Diesen &7Befehl &7darfst &7du &7nicht &7ausführen.")));
            return;
        }

        if (args.length != 1) {
            sender.sendMessage(new TextComponent(Vubex_proxy.serverPrefix + ChatUtils.getColoredText("&7Verwendung: &e/baninfo &e[Spieler/UUID/BanID]")));
            return;
        }

        String targetIdentifier = args[0];
        UUID uuid = null;
        boolean foundBanInfo = false;

        try (Connection connection = mysqlManager.getConnection();
             PreparedStatement selectStatement = connection.prepareStatement(
                     "SELECT * FROM bans WHERE uuid = ? OR ban_id = ?")) {

            try {
                if (!targetIdentifier.contains("-")) {
                    String uuidString = targetIdentifier.replaceAll("(.{8})(.{4})(.{4})(.{4})(.{12})", "$1-$2-$3-$4-$5");
                    uuid = UUID.fromString(uuidString);
                } else {
                    uuid = UUID.fromString(targetIdentifier);
                }
            } catch (IllegalArgumentException ignored) {
                // Ignore, uuid will remain null
            }

            selectStatement.setString(1, uuid != null ? uuid.toString() : "invalid_uuid");
            selectStatement.setString(2, targetIdentifier);

            try (ResultSet resultSet = selectStatement.executeQuery()) {
                if (resultSet.next()) {
                    BanInfo banInfo = extractBanInfoFromResultSet(resultSet);
                    sendBanInfo(sender, banInfo);
                    foundBanInfo = true;
                }
            }

            if (!foundBanInfo && uuid == null) {
                uuid = ChatUtils.getUUIDFromPlayerName(targetIdentifier);
                if (uuid != null) {
                    try (PreparedStatement secondSelectStatement = connection.prepareStatement(
                            "SELECT * FROM bans WHERE uuid = ? AND ban_active = true")) {

                        secondSelectStatement.setString(1, uuid.toString());

                        try (ResultSet playerNameResultSet = secondSelectStatement.executeQuery()) {
                            if (playerNameResultSet.next()) {
                                BanInfo banInfo = extractBanInfoFromResultSet(playerNameResultSet);
                                sendBanInfo(sender, banInfo);
                                foundBanInfo = true;
                            }
                        }
                    } catch (SQLException e) {
                        Vubex_proxy.getInstance().getLogger().log(Level.SEVERE, "Fehler beim Abrufen von Ban-Informationen für UUID: " + uuid, e);
                    }
                }
            }

            if (!foundBanInfo) {
                sender.sendMessage(new TextComponent(Vubex_proxy.serverPrefix + "Ban-Informationen nicht gefunden."));
            }

        } catch (SQLException e) {
            Vubex_proxy.getInstance().getLogger().log(Level.SEVERE, "Fehler beim Zugriff auf die Datenbank: bans", e);
        }
    }

    private BanInfo extractBanInfoFromResultSet(ResultSet resultSet) throws SQLException {
        int reasonNumber = resultSet.getInt("reason");
        String duration = resultSet.getString("duration");
        UUID bannedByUuid = UUID.fromString(resultSet.getString("banned_by"));
        String bannedBy;
        if(bannedByUuid.toString().equals("1b6b1876-df7f-4ac5-b352-84aa28a5749c")){
            bannedBy = "System oder Console";
        }else{
            bannedBy = ChatUtils.getOfflinePlayerName(bannedByUuid);
        }
        String ip = resultSet.getString("ip");
        String banId = resultSet.getString("ban_id");
        long banTime = resultSet.getTimestamp("ban_time") != null ? resultSet.getTimestamp("ban_time").getTime() : 0;
        UUID uuid = UUID.fromString(resultSet.getString("uuid"));
        String playerName = ChatUtils.getOfflinePlayerName(uuid);
        String country = resultSet.getString("country");
        String region = resultSet.getString("region");
        String city = resultSet.getString("city");
        String proxy = resultSet.getString("proxy");
        String isp = resultSet.getString("isp");
        String description = resultSet.getString("ban_description");
        Timestamp banTime2 = resultSet.getTimestamp("ban_time");
        boolean banActive = resultSet.getBoolean("ban_active");
        Date now = new Date();
        long remainingMillis = 0;
        if(!duration.equals("Permanent")){
            remainingMillis = banTime2.getTime() + parseDuration(duration) - now.getTime();
        }
        long remainingDays = TimeUnit.MILLISECONDS.toDays(remainingMillis);
        long remainingHours = TimeUnit.MILLISECONDS.toHours(remainingMillis) % 24;
        long remainingMinutes = TimeUnit.MILLISECONDS.toMinutes(remainingMillis) % 60;
        boolean ban_duration_changed = resultSet.getBoolean("ban_duration_changed");
        String ban_duration_changed_whoUuid = resultSet.getString("ban_duration_changed_who");
        String ban_duration_old_duration = resultSet.getString("ban_duration_old_duration");

        return new BanInfo(uuid, playerName, reasonNumber, duration, bannedBy, ip, banId, banTime, country, region, city, proxy, isp, description, remainingDays, remainingHours, remainingMinutes, remainingMillis, banActive, ban_duration_changed, ban_duration_changed_whoUuid, ban_duration_old_duration);
    }

    private void sendBanInfo(CommandSender sender, BanInfo banInfo) {
        String infoMessage = "\n" + Vubex_proxy.serverPrefix + "Informationen für Ban-ID &e" + banInfo.banId() + "&7:\n";
        infoMessage += "&8» &7Spieler Name: &e" + banInfo.playerName() + "\n";
        infoMessage += "&8» &7Spieler UUID: &e" + banInfo.uuid() + "\n";
        infoMessage += "&8» &7Ban Grund: &e" + getBanReason(banInfo.reasonNumber()) + "\n";
        if(banInfo.ban_duration_changed()){
            infoMessage += "&8» &7Dauer: &e" + banInfo.duration() + " &7(Davor: &e" + banInfo.ban_duration_old_duration() +"&7)\n";
            String ban_duration_changed_who;
            if(banInfo.ban_duration_changed_whoUuid().equals("1b6b1876-df7f-4ac5-b352-84aa28a5749c")){
                ban_duration_changed_who = "System oder Console";
            }else{
                ban_duration_changed_who = ChatUtils.getOfflinePlayerName(UUID.fromString(banInfo.ban_duration_changed_whoUuid()));
            }
            infoMessage += "&8» &7Dauer geändert von: &e" + ban_duration_changed_who + "\n";
        }else{
            infoMessage += "&8» &7Dauer: &e" + banInfo.duration() + "\n";
        }
        infoMessage += "&8» &7Gebannt von: &e" + banInfo.bannedBy() + "\n";
        if(banInfo.duration.equals("Permanent") && banInfo.banActive){
            infoMessage += "&8»&7 Verbleibende Zeit: &ePermanent\n";
        } else if (!banInfo.duration.equals("Permanent") && banInfo.remainingMillis() > 0 && banInfo.remainingDays() == 0 && banInfo.remainingHours() == 0 && banInfo.remainingMinutes() == 0) {
            infoMessage += "&8»&7 Verbleibende Zeit: &eUnter einer Minute\n";
        } else if (!banInfo.duration.equals("Permanent") && banInfo.remainingMillis() < 0) {
            infoMessage += "&8»&7 Verbleibende Zeit: &eKeine - Ban ist abgelaufen\n";
        } else if (!banInfo.banActive()) {
            infoMessage += "&8»&7 Verbleibende Zeit: &eKeine - Spieler wurde entbannt\n";
        } else {
            infoMessage += "&8»&7 Verbleibende Zeit: &e" + banInfo.remainingDays() + "&e Tage, " + banInfo.remainingHours() + "&e Stunden, " + banInfo.remainingMinutes() + "&e Minuten\n";
        }
        infoMessage += "&8» &7Gebannt seit: &e" + new Date(banInfo.banTime()) + "\n";
        infoMessage += "&8» &7Letzte IP: &e" + banInfo.ip() + "&7:\n";
        infoMessage += "  &8» &7Land: &e" + banInfo.country() + "\n";
        infoMessage += "  &8» &7Region: &e" + banInfo.region() + "\n";
        infoMessage += "  &8» &7Stadt: &e" + banInfo.city() + "\n";
        if (banInfo.proxy().equals("true")) {
            infoMessage += "  &8» &7Proxy/VPN: &eJa\n";
            infoMessage += "  &8» &7Info: &eDer Standort könnte gefälscht sein!\n";
        } else {
            infoMessage += "  &8» &7Proxy/VPN: &eNein\n";
        }
        infoMessage += "  &8» &7Internet-Anbieter: &e" + banInfo.isp() + "\n";
        if (banInfo.description() == null) {
            infoMessage += "&8» &7Ban Beschreibung: &eKeine\n";
        } else {
            infoMessage += "&8» &7Ban Beschreibung: " + banInfo.description() + "\n";
        }

        sender.sendMessage(new TextComponent(ChatUtils.getColoredText(infoMessage)));
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
            default -> "Unbekannter Grund";
        };
    }

    private long parseDuration(String duration) {
        long totalMillis = 0;
        String[] parts = duration.split(" ");
        for (String part : parts) {
            char unit = part.charAt(part.length() - 1);
            int value = Integer.parseInt(part.substring(0, part.length() - 1));
            switch (unit) {
                case 'd', 'D' -> totalMillis += TimeUnit.DAYS.toMillis(value);
                case 'h', 'H' -> totalMillis += TimeUnit.HOURS.toMillis(value);
                case 'm', 'M' -> totalMillis += TimeUnit.MINUTES.toMillis(value);
                case 's', 'S' -> totalMillis += TimeUnit.SECONDS.toMillis(value);
                default -> throw new IllegalArgumentException("Unsupported time unit: " + unit);
            }
        }
        return totalMillis;
    }

    private record BanInfo(UUID uuid, String playerName, int reasonNumber, String duration, String bannedBy, String ip,
                           String banId, long banTime, String country, String region, String city, String proxy,
                           String isp, String description, long remainingDays, long remainingHours, long remainingMinutes, long remainingMillis, boolean banActive,
                           boolean ban_duration_changed, String ban_duration_changed_whoUuid, String ban_duration_old_duration) {
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        List<String> completions = new ArrayList<>();
        if(sender.hasPermission("vubex.baninfo") && args.length == 1){
            try (Connection connection = mysqlManager.getConnection();
                 PreparedStatement selectStatement = connection.prepareStatement(
                         "SELECT ban_id FROM bans"
                 );
                 ResultSet resultSet = selectStatement.executeQuery()) {

                while (resultSet.next()) {
                    String partialBanID = args[0];
                    String ban_ids = resultSet.getString("ban_id");
                    if(ban_ids.startsWith(partialBanID)){
                        completions.add(ban_ids);
                    }
                }
            } catch (SQLException e) {
                Vubex_proxy.getInstance().getLogger().log(Level.SEVERE, "Ein Fehler ist aufgetreten, während BanIDs von der Datenbank Bans abgerufen wurden. (für den TabExecutor)", e);
            }
        }

        return completions;
    }
}