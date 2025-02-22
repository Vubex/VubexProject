package de.flavius.vubex.vubexproxy.utils;

import de.flavius.vubex.vubexproxy.Vubex_proxy;
import de.flavius.vubex.vubexproxy.mysql.MySQLManager;
import net.md_5.bungee.api.ProxyServer;
import java.sql.*;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author : flavius
 * project : VubexProject
 **/
public class MuteManager {

    private static final Logger logger = Logger.getLogger(MuteManager.class.getName());
    private final MySQLManager mysqlManager;

    public MuteManager(MySQLManager mysqlManager) {
        this.mysqlManager = mysqlManager;
    }

    public boolean mutePlayer(UUID uuid, UUID mutedBy, int reasonNumber, String duration, String muteID) throws SQLException {
        try (Connection connection = mysqlManager.getConnection()) {

            try (PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO mutes (uuid, reason, duration, muted_by, mute_id, mute_active, mute_time) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?)")) {

                statement.setString(1, uuid.toString());
                statement.setInt(2, reasonNumber);
                if (reasonNumber == 8 || reasonNumber == 9 || reasonNumber == 99) {
                    // Set duration to permanent for reasons 8, 9, and 99
                    duration = "Permanent";
                }
                statement.setString(3, duration);
                statement.setString(4, String.valueOf(mutedBy));
                statement.setString(5, muteID);
                statement.setInt(6, 1);
                statement.setTimestamp(7, new Timestamp(System.currentTimeMillis()));

                ProxyServer.getInstance().getLogger().info("Executing query: " + statement);

                statement.executeUpdate();

                ProxyServer.getInstance().getLogger().info("Player successfully muted: " + uuid);
            } catch (SQLException e) {
                ProxyServer.getInstance().getLogger().log(Level.SEVERE, "Error executing mute query: " + e.getMessage());
            }
        } catch (SQLException e) {
            ProxyServer.getInstance().getLogger().log(Level.SEVERE, "Error connecting to the database: " + e.getMessage());
        }
        return true;
    }

    public boolean isMuted(UUID player) {
        try (Connection connection = mysqlManager.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(
                    "SELECT * FROM mutes WHERE uuid = ? AND mute_active = 1")) {

                statement.setString(1, player.toString());
                ResultSet resultSet = statement.executeQuery();

                return resultSet.next();

            } catch (SQLException e) {
                Vubex_proxy.getInstance().getLogger().log(Level.SEVERE, "Error checking mute status for player.", e);
            }
        } catch (SQLException e) {
            Vubex_proxy.getInstance().getLogger().log(Level.SEVERE, "Error connecting to the database.", e);
        }
        return false;
    }

    public void checkAndUpdateMuteStatus() {
        try (Connection connection = mysqlManager.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(
                    "UPDATE mutes SET mute_active = 0 WHERE mute_active = 1 AND (mute_time + INTERVAL ? SECOND <= NOW()) AND duration != 'Permanent'")) {

                try (PreparedStatement durationStatement = connection.prepareStatement(
                        "SELECT duration, mute_time FROM mutes WHERE mute_active = 1 AND duration != 'Permanent'")) {

                    ResultSet durationResultSet = durationStatement.executeQuery();

                    while (durationResultSet.next()) {
                        String duration = durationResultSet.getString("duration");
                        long durationSeconds = parseDurationToSeconds(duration);
                        Timestamp muteTime = durationResultSet.getTimestamp("mute_time");

                        long currentTimeMillis = System.currentTimeMillis();
                        long elapsedTimeMillis = currentTimeMillis - muteTime.getTime();
                        long remainingSeconds = durationSeconds - TimeUnit.MILLISECONDS.toSeconds(elapsedTimeMillis);

                        statement.setLong(1, remainingSeconds);
                        statement.addBatch();
                    }

                    statement.executeBatch();

                } catch (SQLException e) {
                    Vubex_proxy.getInstance().getLogger().log(Level.SEVERE, "Error updating mute status in the database.", e);
                }

            } catch (SQLException e) {
                Vubex_proxy.getInstance().getLogger().log(Level.SEVERE, "Error updating mute status in the database.", e);
            }
        } catch (SQLException e) {
            Vubex_proxy.getInstance().getLogger().log(Level.SEVERE, "Error connecting to the database.", e);
        }
    }

    public boolean isPlayerAlreadyMuted(Connection connection, String uuid) {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT * FROM mutes WHERE uuid = ? AND mute_active = 1")) {

            statement.setString(1, uuid);
            ResultSet resultSet = statement.executeQuery();

            return resultSet.next();

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error checking if player is already muted.", e);
            return false;
        }
    }

    public String getMuteMessage(UUID player) {
        try (Connection connection = mysqlManager.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(
                    "SELECT reason, duration, mute_id, mute_time FROM mutes WHERE uuid = ? AND mute_active = 1")) {

                statement.setString(1, player.toString());
                ResultSet resultSet = statement.executeQuery();

                if (resultSet.next()) {
                    int reasonNumber = resultSet.getInt("reason");
                    String duration = resultSet.getString("duration");
                    String muteId = resultSet.getString("mute_id");

                    long muteTimeMillis = resultSet.getTimestamp("mute_time").getTime();
                    long currentTimeMillis = System.currentTimeMillis();
                    long elapsedTimeMillis = currentTimeMillis - muteTimeMillis;
                    long remainingSeconds = parseDurationToSeconds(duration) - TimeUnit.MILLISECONDS.toSeconds(elapsedTimeMillis);


                    StringBuilder muteMessageBuilder = new StringBuilder();
                    if (duration.equalsIgnoreCase("Permanent")) {
                        muteMessageBuilder.append("\n")
                        .append("""
                                &e&lVubex.DE &r&8»
                                &e&lVubex.DE &r&8» &7Du bist zurzeit Permanent &7Stummgeschaltet.
                                &e&lVubex.DE &r&8»
                                &e&lVubex.DE &r&8» &7Grund: &e""").append(getMuteReason(reasonNumber).replace("/", "&7/&e"));
                    } else {
                        muteMessageBuilder.append("\n")
                                .append("""
                                &e&lVubex.DE &r&8»
                                &e&lVubex.DE &r&8» &7Du bist zurzeit vorübergehend &7Stummgeschaltet.
                                &e&lVubex.DE &r&8»
                                &e&lVubex.DE &r&8» &7Grund: &e""").append(getMuteReason(reasonNumber).replace("/", "&7/&e"));
                        if (remainingSeconds <= 0) {
                            muteMessageBuilder.append("\n&e&lVubex.DE &r&8» &7Verbleibende Zeit: &eKeine");
                        }else{
                            if (remainingSeconds >= 60 * 60 * 24) {
                                long remainingDays = remainingSeconds / (60 * 60 * 24);
                                muteMessageBuilder.append("\n&e&lVubex.DE &r&8» &7Verbleibende Zeit: &e").append(remainingDays).append(" Tag(e)");
                            }else{
                                if (remainingSeconds >= 60 * 60) {
                                    long remainingHours = remainingSeconds / (60 * 60);
                                    muteMessageBuilder.append("\n&e&lVubex.DE &r&8» &7Verbleibende Zeit: &e").append(remainingHours).append(" Stunde(n)");
                                }else{
                                    if (remainingSeconds >= 60) {
                                        long remainingMinutes = remainingSeconds / 60;
                                        muteMessageBuilder.append("\n&e&lVubex.DE &r&8» &7Verbleibende Zeit: &e").append(remainingMinutes).append(" Minute(n)");
                                    }else{
                                        muteMessageBuilder.append("\n&e&lVubex.DE &r&8» &7Verbleibende Zeit: &e").append(remainingSeconds).append(" Sekunde(n)");
                                    }
                                }
                            }
                        }
                    }
                    muteMessageBuilder.append("\n&e&lVubex.DE &r&8»\n&e&lVubex.DE &r&8» &7Mute-ID: &e").append(muteId).append("\n&e&lVubex.DE &r&8»\n");
                    return muteMessageBuilder.toString();
                }

            } catch (SQLException e) {
                Vubex_proxy.getInstance().getLogger().log(Level.SEVERE, "Error retrieving mute message for player.", e);
            }
        } catch (SQLException e) {
            Vubex_proxy.getInstance().getLogger().log(Level.SEVERE, "Error connecting to the database.", e);
        }
        return "Du wurdest gemutet.";
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

    private long parseDurationToSeconds(String duration) {
        if (duration.equalsIgnoreCase("Permanent")) {
            return Long.MAX_VALUE;
        }

        String[] parts = duration.split(" ");
        long totalSeconds = 0;

        for (String part : parts) {
            char unit = part.charAt(part.length() - 1);
            int value = Integer.parseInt(part.substring(0, part.length() - 1));

            switch (unit) {
                case 'd', 'D' -> totalSeconds += TimeUnit.DAYS.toSeconds(value);
                case 'h', 'H' -> totalSeconds += TimeUnit.HOURS.toSeconds(value);
                case 'm', 'M' -> totalSeconds += TimeUnit.MINUTES.toSeconds(value);
                case 's', 'S' -> totalSeconds += value;
                default -> throw new IllegalArgumentException("Unsupported time unit: " + unit);
            }
        }

        return totalSeconds;
    }
}