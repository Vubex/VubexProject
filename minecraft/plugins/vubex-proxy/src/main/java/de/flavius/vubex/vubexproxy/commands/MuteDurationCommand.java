package de.flavius.vubex.vubexproxy.commands;

import de.flavius.vubex.vubexproxy.Vubex_proxy;
import de.flavius.vubex.vubexproxy.api.VubexDiscordAPI;
import de.flavius.vubex.vubexproxy.mysql.MySQLManager;
import de.flavius.vubex.vubexproxy.utils.ChatUtils;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

import java.awt.*;
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
public class MuteDurationCommand extends Command implements TabExecutor {

    public final VubexDiscordAPI vubexDiscordAPI;
    private static final String INVALID_TIME_FORMAT = "Ungültiges Zeitformat. Verwende d, h oder m";
    private final MySQLManager mysqlManager;

    public MuteDurationCommand(MySQLManager mysqlManager, VubexDiscordAPI vubexDiscordAPI) {
        super("muteduration", null);
        this.mysqlManager = mysqlManager;
        this.vubexDiscordAPI = vubexDiscordAPI;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(!sender.hasPermission("vubex.mute.modify")){
            sender.sendMessage(new TextComponent(Vubex_proxy.serverPrefix + ChatUtils.getColoredText("&7Diesen &7Befehl &7darfst &7du &7nicht &7ausführen.")));
            return;
        }

        if (args.length < 1) {
            sender.sendMessage(new TextComponent(Vubex_proxy.serverPrefix + ChatUtils.getColoredText("&7Verwendung: &e/muteduration &e[MuteID] &e<Zeit>")));
            return;
        } else if (args.length > 2) {
            sender.sendMessage(new TextComponent(Vubex_proxy.serverPrefix + ChatUtils.getColoredText("&7Zu viele Argumente. Verwende &e/muteduration &e[MuteID] &e<Zeit>")));
            return;
        }

        String targetIdentifier = args[0];
        UUID uuid = parseUUID(targetIdentifier);
        String identifierString = uuid != null ? uuid.toString() : targetIdentifier;

        if (args.length > 1) {
            if (args[1].matches("\\d+[dDhHmMsS]")) {
                setMuteDuration(sender, identifierString, args[1]);
            } else {
                sender.sendMessage(new TextComponent(Vubex_proxy.serverPrefix + INVALID_TIME_FORMAT));
            }
            return;
        }

        MuteInfo muteInfo = getMuteInfo(identifierString);
        showMuteInfo(sender, targetIdentifier, identifierString, muteInfo);
    }

    private UUID parseUUID(String identifier) {
        try {
            return UUID.fromString(identifier);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    private MuteInfo getMuteInfo(String identifier) {
        try (Connection connection = mysqlManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT reason FROM mutes WHERE uuid = ? OR mute_id = ?")) {

            statement.setString(1, identifier);
            statement.setString(2, identifier);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    int reasonNumber = resultSet.getInt("reason");
                    return new MuteInfo(reasonNumber);
                }
            }
        } catch (SQLException e) {
            Vubex_proxy.getInstance().getLogger().log(Level.SEVERE, "Fehler beim Abrufen von Mute-Informationen für Identifier: " + identifier, e);
        }
        return new MuteInfo(-1);
    }

    private void showMuteInfo(CommandSender sender, String targetIdentifier, String identifierString, MuteInfo muteInfo) {
        try (Connection connection = mysqlManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT * FROM mutes WHERE uuid = ? OR mute_id = ?")) {

            statement.setString(1, identifierString);
            statement.setString(2, targetIdentifier);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    String duration = resultSet.getString("duration");
                    String muteId = resultSet.getString("mute_id");
                    Timestamp muteTime = resultSet.getTimestamp("mute_time");
                    Date now = new Date();

                    long remainingMillis = 0;
                    if(!duration.equals("Permanent")){
                        remainingMillis = muteTime.getTime() + parseDuration(duration) - now.getTime();
                    }
                    long remainingDays = TimeUnit.MILLISECONDS.toDays(remainingMillis);
                    long remainingHours = TimeUnit.MILLISECONDS.toHours(remainingMillis) % 24;
                    long remainingMinutes = TimeUnit.MILLISECONDS.toMinutes(remainingMillis) % 60;
                    UUID mutedUuid = UUID.fromString(resultSet.getString("uuid"));
                    UUID mutedByUuid = UUID.fromString(resultSet.getString("muted_by"));
                    boolean mute_duration_changed = resultSet.getBoolean("mute_duration_changed");
                    String mutedPlayerName = ChatUtils.getOfflinePlayerName(mutedUuid);
                    String mutedByPlayerName;
                    if (mutedByUuid.toString().equals("1b6b1876-df7f-4ac5-b352-84aa28a5749c")) {
                        mutedByPlayerName = "System oder Console";
                    } else {
                        mutedByPlayerName = ChatUtils.getOfflinePlayerName(mutedByUuid);
                    }
                    String infoMessage = "\n" + Vubex_proxy.serverPrefix + "Informationen für Mute-ID &e" + muteId + "&7:";
                    infoMessage += "\n&8» &7Spieler: &e" + mutedPlayerName;
                    infoMessage += "\n&8» &7Grund: &e" + getMuteReason(muteInfo.reasonNumber());
                    if (mute_duration_changed) {
                        String mute_duration_old_duration = resultSet.getString("mute_duration_old_duration");
                        infoMessage += "\n&8» &7Dauer: &e" + duration + " &7(Davor: &e" + mute_duration_old_duration + "&7)";
                        UUID mute_duration_changed_whoUuid = UUID.fromString(resultSet.getString("mute_duration_changed_who"));
                        String mute_duration_changed_who;
                        if (mute_duration_changed_whoUuid.toString().equals("1b6b1876-df7f-4ac5-b352-84aa28a5749c")) {
                            mute_duration_changed_who = "System oder Console";
                        } else {
                            mute_duration_changed_who = ChatUtils.getOfflinePlayerName(mute_duration_changed_whoUuid);
                        }
                        infoMessage += "\n&8» &7Dauer geändert von: &e" + mute_duration_changed_who;
                    } else {
                        infoMessage += "\n&8» &7Dauer: &e" + duration;
                    }
                    infoMessage += "\n&8» &7Gemutet von: &e" + mutedByPlayerName;
                    if(duration.equals("Permanent") && resultSet.getBoolean("mute_active")){
                        infoMessage += "\n&8» &7Verbleibende Zeit: &ePermanent\n";
                        sender.sendMessage(new TextComponent(ChatUtils.getColoredText(infoMessage)));
                    } else if (!duration.equals("Permanent") && remainingMillis > 0 && remainingDays == 0 && remainingHours == 0 && remainingMinutes == 0) {
                        infoMessage += "\n&8» &7Verbleibende Zeit: &eUnter einer Minute\n";
                        sender.sendMessage(new TextComponent(ChatUtils.getColoredText(infoMessage)));
                    } else if (!duration.equals("Permanent") && remainingMillis < 0) {
                        infoMessage += "\n&8» &7Verbleibende Zeit: &eKeine - Mute ist abgelaufen\n";
                        sender.sendMessage(new TextComponent(ChatUtils.getColoredText(infoMessage)));
                    } else {
                        if (!resultSet.getBoolean("mute_active")) {
                            infoMessage += "\n&8» &7Verbleibende Zeit: &eKeine - Spieler wurde entmutet\n";
                            sender.sendMessage(new TextComponent(ChatUtils.getColoredText(infoMessage)));
                            return;
                        }
                        infoMessage += "\n&8» &7Verbleibende Zeit: &e" + remainingDays + "&e Tage, " + remainingHours + "&e Stunden, " + remainingMinutes + "&e Minuten\n";
                        sender.sendMessage(new TextComponent(ChatUtils.getColoredText(infoMessage)));
                    }
                } else {
                    sender.sendMessage(new TextComponent(Vubex_proxy.serverPrefix + "Mute-Informationen nicht gefunden."));
                }
            }
        } catch (SQLException e) {
            Vubex_proxy.getInstance().getLogger().log(Level.SEVERE, "Fehler beim Zugriff auf die Datenbank: mutes", e);
        }
    }

    private void setMuteDuration(CommandSender sender, String identifier, String newDuration) {
        String identifierString = null;
        try (Connection connection = mysqlManager.getConnection();
             PreparedStatement selectStatement = connection.prepareStatement(
                     "SELECT * FROM mutes WHERE uuid = ? OR mute_id = ?")) {

            UUID uuid = null;
            try {
                if (!identifier.contains("-")) {
                    String uuidString = identifier.replaceAll("(.{8})(.{4})(.{4})(.{4})(.{12})", "$1-$2-$3-$4-$5");
                    uuid = UUID.fromString(uuidString);
                } else {
                    uuid = UUID.fromString(identifier);
                }
            } catch (IllegalArgumentException ignored) {
                // Ignore, uuid will remain null
            }

            identifierString = uuid != null ? uuid.toString() : identifier;

            selectStatement.setString(1, identifierString);
            selectStatement.setString(2, identifier);

            try (ResultSet resultSet = selectStatement.executeQuery()) {
                if (resultSet.next()) {
                    int reasonNumber = resultSet.getInt("reason");
                    if (reasonNumber == 99 && !sender.hasPermission("vubex.mute.modify.adminmute")) {
                        sender.sendMessage(new TextComponent(Vubex_proxy.serverPrefix + "&7Du &7hast &7keine &7Berechtigung, &7die &7Admin-Mute-Dauer &7zu &7ändern."));
                        return;
                    }
                    if (!resultSet.getBoolean("mute_active")) {
                        sender.sendMessage(new TextComponent(Vubex_proxy.serverPrefix + "Der Mute ist nicht aktiv."));
                    } else {
                        Timestamp muteTime = resultSet.getTimestamp("mute_time");
                        Date now = new Date();
                        long remainingMillis = muteTime.getTime() + parseDuration(newDuration) - now.getTime();
                        long remainingDays = TimeUnit.MILLISECONDS.toDays(remainingMillis);
                        long remainingHours = TimeUnit.MILLISECONDS.toHours(remainingMillis) % 24;
                        long remainingMinutes = TimeUnit.MILLISECONDS.toMinutes(remainingMillis) % 60;
                        UUID mutedUuid = UUID.fromString(resultSet.getString("uuid"));
                        String oldDuration = resultSet.getString("duration");
                        String mutedPlayerName = ChatUtils.getOfflinePlayerName(mutedUuid);
                        String muteId = resultSet.getString("mute_id");

                        try (PreparedStatement updateStatement = connection.prepareStatement(
                                "UPDATE mutes SET duration = ?, mute_duration_changed = true, mute_duration_changed_who = ?, mute_duration_old_duration = ?, mute_duration_changed_when = ? WHERE uuid = ? OR mute_id = ?")) {

                            updateStatement.setString(1, newDuration);
                            if (sender instanceof ProxiedPlayer) {
                                updateStatement.setString(2, ((ProxiedPlayer) sender).getUniqueId().toString());
                            } else {
                                updateStatement.setString(2, "1b6b1876-df7f-4ac5-b352-84aa28a5749c");
                            }
                            updateStatement.setString(3, oldDuration);
                            updateStatement.setTimestamp(4, new Timestamp(System.currentTimeMillis()));
                            updateStatement.setString(5, identifierString);
                            updateStatement.setString(6, identifier);

                            updateStatement.executeUpdate();

                            sender.sendMessage(new TextComponent(Vubex_proxy.serverPrefix + "Mute-Dauer für " + mutedPlayerName + " erfolgreich geändert."));
                            String title = "Team-Information";
                            String iconUrl = "https://mc-heads.net/head/" + mutedUuid + "/600";
                            Color color = Color.ORANGE;
                            String[] lines = new String[]{"Die Mute-Dauer von **" + mutedPlayerName + "** wurde von **" + sender.getName() + "** auf **" + newDuration + "** aktualisiert (Davor: **" + oldDuration + "**). Mute-ID: **" + muteId + "**\n\nVon Jetzt an verbliebende Ban Dauer:\n**" + remainingDays + " Tage, " + remainingHours + " Stunden, " + remainingMinutes + " Minuten**"};
                            vubexDiscordAPI.sendEmbedMessage("1222293450284863498", title, lines, iconUrl, color);
                        } catch (SQLException e) {
                            Vubex_proxy.getInstance().getLogger().log(Level.SEVERE, "Fehler beim Aktualisieren der Mute-Dauer für UUID: " + identifierString, e);
                        }
                    }
                } else {
                    sender.sendMessage(new TextComponent(Vubex_proxy.serverPrefix + "Spieler oder Mute-ID nicht gefunden."));
                }
            }
        } catch (SQLException e) {
            Vubex_proxy.getInstance().getLogger().log(Level.SEVERE, "Fehler beim Abfragen der Datenbank mutes für UUID: " + identifierString, e);
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

    private record MuteInfo(int reasonNumber) {
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        List<String> completions = new ArrayList<>();
        if (sender.hasPermission("vubex.mute.modify") && args.length == 1) {
            try (Connection connection = mysqlManager.getConnection();
                 PreparedStatement selectStatement = connection.prepareStatement(
                         "SELECT mute_id FROM mutes WHERE mute_active = true"
                 );
                 ResultSet resultSet = selectStatement.executeQuery()) {

                while (resultSet.next()) {
                    String partialMuteID = args[0];
                    String mute_ids = resultSet.getString("mute_id");
                    if (mute_ids.startsWith(partialMuteID)) {
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