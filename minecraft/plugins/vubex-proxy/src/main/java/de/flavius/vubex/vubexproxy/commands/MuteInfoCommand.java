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
public class MuteInfoCommand extends Command implements TabExecutor {

    private final MySQLManager mysqlManager;

    public MuteInfoCommand(MySQLManager mysqlManager) {
        super("muteinfo", null);
        this.mysqlManager = mysqlManager;
    }

    public void execute(CommandSender sender, String[] args) {
        if(!sender.hasPermission("vubex.muteinfo")){
            sender.sendMessage(new TextComponent(Vubex_proxy.serverPrefix + ChatUtils.getColoredText("&7Diesen &7Befehl &7darfst &7du &7nicht &7ausführen.")));
            return;
        }

        if (args.length != 1) {
            sender.sendMessage(new TextComponent(Vubex_proxy.serverPrefix + ChatUtils.getColoredText("&7Verwendung: &e/muteinfo &e[Spieler/UUID/MuteID]")));
            return;
        }

        String targetIdentifier = args[0];
        UUID uuid = null;
        boolean foundMuteInfo = false;

        try (Connection connection = mysqlManager.getConnection();
             PreparedStatement selectStatement = connection.prepareStatement(
                     "SELECT * FROM mutes WHERE uuid = ? OR mute_id = ?")) {

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
                    MuteInfo muteInfo = extractMuteInfoFromResultSet(resultSet);
                    sendMuteInfo(sender, muteInfo);
                    foundMuteInfo = true;
                }
            }

            if (!foundMuteInfo && uuid == null) {
                uuid = ChatUtils.getUUIDFromPlayerName(targetIdentifier);
                if (uuid != null) {
                    try (PreparedStatement secondSelectStatement = connection.prepareStatement(
                            "SELECT * FROM mutes WHERE uuid = ? AND mute_active = true")) {

                        secondSelectStatement.setString(1, uuid.toString());

                        try (ResultSet playerNameResultSet = secondSelectStatement.executeQuery()) {
                            if (playerNameResultSet.next()) {
                                MuteInfo muteInfo = extractMuteInfoFromResultSet(playerNameResultSet);
                                sendMuteInfo(sender, muteInfo);
                                foundMuteInfo = true;
                            }
                        }
                    } catch (SQLException e) {
                        Vubex_proxy.getInstance().getLogger().log(Level.SEVERE, "Fehler beim Abrufen von Mute-Informationen für UUID: " + uuid, e);
                    }
                }
            }

            if (!foundMuteInfo) {
                sender.sendMessage(new TextComponent(Vubex_proxy.serverPrefix + "Mute-Informationen nicht gefunden."));
            }

        } catch (SQLException e) {
            Vubex_proxy.getInstance().getLogger().log(Level.SEVERE, "Fehler beim Zugriff auf die Datenbank: mutes", e);
        }
    }

    private MuteInfo extractMuteInfoFromResultSet(ResultSet resultSet) throws SQLException {
        int reasonNumber = resultSet.getInt("reason");
        String duration = resultSet.getString("duration");
        UUID mutedByUuid = UUID.fromString(resultSet.getString("muted_by"));
        String mutedBy;
        if(mutedByUuid.toString().equals("1b6b1876-df7f-4ac5-b352-84aa28a5749c")){
            mutedBy = "System oder Console";
        }else{
            mutedBy = ChatUtils.getOfflinePlayerName(mutedByUuid);
        }
        String muteId = resultSet.getString("mute_id");
        long muteTime = resultSet.getTimestamp("mute_time") != null ? resultSet.getTimestamp("mute_time").getTime() : 0;
        UUID uuid = UUID.fromString(resultSet.getString("uuid"));
        String playerName = ChatUtils.getOfflinePlayerName(uuid);
        String description = resultSet.getString("mute_description");
        Timestamp muteTime2 = resultSet.getTimestamp("mute_time");
        boolean muteActive = resultSet.getBoolean("mute_active");
        Date now = new Date();
        long remainingMillis = 0;
        if(!duration.equals("Permanent")){
            remainingMillis = muteTime2.getTime() + parseDuration(duration) - now.getTime();
        }
        long remainingDays = TimeUnit.MILLISECONDS.toDays(remainingMillis);
        long remainingHours = TimeUnit.MILLISECONDS.toHours(remainingMillis) % 24;
        long remainingMinutes = TimeUnit.MILLISECONDS.toMinutes(remainingMillis) % 60;
        boolean mute_duration_changed = resultSet.getBoolean("mute_duration_changed");
        String mute_duration_changed_whoUuid = resultSet.getString("mute_duration_changed_who");
        String mute_duration_old_duration = resultSet.getString("mute_duration_old_duration");

        return new MuteInfo(uuid, playerName, reasonNumber, duration, mutedBy, muteId, muteTime, description, remainingDays, remainingHours, remainingMinutes, remainingMillis, muteActive, mute_duration_changed, mute_duration_changed_whoUuid, mute_duration_old_duration);
    }

    private void sendMuteInfo(CommandSender sender, MuteInfo muteInfo) {
        String infoMessage = "\n" + Vubex_proxy.serverPrefix + "Informationen für Mute-ID &e" + muteInfo.muteId() + "&7:\n";
        infoMessage += "&8» &7Spieler Name: &e" + muteInfo.playerName() + "\n";
        infoMessage += "&8» &7Spieler UUID: &e" + muteInfo.uuid() + "\n";
        infoMessage += "&8» &7Mute Grund: &e" + getMuteReason(muteInfo.reasonNumber()) + "\n";
        if(muteInfo.mute_duration_changed()){
            infoMessage += "&8» &7Dauer: &e" + muteInfo.duration() + " &7(Davor: &e" + muteInfo.mute_duration_old_duration() +"&7)\n";
            String mute_duration_changed_who;
            if(muteInfo.mute_duration_changed_whoUuid().equals("1b6b1876-df7f-4ac5-b352-84aa28a5749c")){
                mute_duration_changed_who = "System oder Console";
            }else{
                mute_duration_changed_who = ChatUtils.getOfflinePlayerName(UUID.fromString(muteInfo.mute_duration_changed_whoUuid()));
            }
            infoMessage += "&8» &7Dauer geändert von: &e" + mute_duration_changed_who + "\n";
        }else{
            infoMessage += "&8» &7Dauer: &e" + muteInfo.duration() + "\n";
        }
        infoMessage += "&8» &7Gemutet von: &e" + muteInfo.mutedBy() + "\n";
        if(muteInfo.duration.equals("Permanent") && muteInfo.muteActive){
            infoMessage += "&8»&7 Verbleibende Zeit: &ePermanent\n";
        } else if (!muteInfo.duration.equals("Permanent") && muteInfo.remainingMillis() > 0 && muteInfo.remainingDays() == 0 && muteInfo.remainingHours() == 0 && muteInfo.remainingMinutes() == 0) {
            infoMessage += "&8»&7 Verbleibende Zeit: &eUnter einer Minute\n";
        } else if (!muteInfo.duration.equals("Permanent") && muteInfo.remainingMillis() < 0) {
            infoMessage += "&8»&7 Verbleibende Zeit: &eKeine - Mute ist abgelaufen\n";
        } else if (!muteInfo.muteActive()) {
            infoMessage += "&8»&7 Verbleibende Zeit: &eKeine - Spieler wurde entmutet\n";
        } else {
            infoMessage += "&8»&7 Verbleibende Zeit: &e" + muteInfo.remainingDays() + "&e Tage, " + muteInfo.remainingHours() + "&e Stunden, " + muteInfo.remainingMinutes() + "&e Minuten\n";
        }
        infoMessage += "&8» &7Gemutet seit: &e" + new Date(muteInfo.muteTime()) + "\n";
        if (muteInfo.description() == null) {
            infoMessage += "&8» &7Mute Beschreibung: &eKeine\n";
        } else {
            infoMessage += "&8» &7Mute Beschreibung: " + muteInfo.description() + "\n";
        }

        sender.sendMessage(new TextComponent(ChatUtils.getColoredText(infoMessage)));
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

    private record MuteInfo(UUID uuid, String playerName, int reasonNumber, String duration, String mutedBy,
                           String muteId, long muteTime, String description, long remainingDays, long remainingHours,
                            long remainingMinutes, long remainingMillis, boolean muteActive,
                            boolean mute_duration_changed, String mute_duration_changed_whoUuid, String mute_duration_old_duration) {
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        List<String> completions = new ArrayList<>();
        if(sender.hasPermission("vubex.muteinfo") && args.length == 1){
            try (Connection connection = mysqlManager.getConnection();
                 PreparedStatement selectStatement = connection.prepareStatement(
                         "SELECT mute_id FROM mutes"
                 );
                 ResultSet resultSet = selectStatement.executeQuery()) {

                while (resultSet.next()) {
                    String partialMuteID = args[0];
                    String mute_ids = resultSet.getString("mute_id");
                    if(mute_ids.startsWith(partialMuteID)){
                        completions.add(mute_ids);
                    }
                }
            } catch (SQLException e) {
                Vubex_proxy.getInstance().getLogger().log(Level.SEVERE, "Ein Fehler ist aufgetreten, während MuteIDs von der Datenbank Mutes abgerufen wurden. (für den TabExecutor)", e);
            }
        }

        return completions;
    }
}