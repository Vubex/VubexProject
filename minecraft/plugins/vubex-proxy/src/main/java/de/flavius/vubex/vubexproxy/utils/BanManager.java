package de.flavius.vubex.vubexproxy.utils;

import de.flavius.vubex.vubexproxy.Vubex_proxy;
import de.flavius.vubex.vubexproxy.mysql.MySQLManager;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.*;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author : flavius
 * project : VubexProject
 **/
public class BanManager implements Listener {
    private static final Logger logger = Logger.getLogger(BanManager.class.getName());

    private final MySQLManager mysqlManager;


    public BanManager(MySQLManager mysqlManager) {
        this.mysqlManager = mysqlManager;
    }

    public boolean banOfflinePlayer(UUID uuid, UUID bannedBy, int reasonNumber, String duration, String banID) throws SQLException {
        try (Connection connection = mysqlManager.getConnection()) {


            String ipAddress = null;
            try (PreparedStatement statement2 = connection.prepareStatement(
                         "SELECT last_ip_address FROM users WHERE uuid = ?")) {

                statement2.setString(1, uuid.toString());

                try (ResultSet resultSet = statement2.executeQuery()) {
                    if (resultSet.next()) {
                        ipAddress = resultSet.getString("last_ip_address");
                        logger.info("Player IP found in the database: " + ipAddress);
                    }
                }

            } catch (SQLException e) {
                logger.log(Level.SEVERE, "Error fetching player IP from the database.", e);
            }

            try (PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO bans (uuid, reason, duration, banned_by, ip, ban_id, country, region, city, proxy, isp, ban_active, ban_time) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")) {

                statement.setString(1, uuid.toString());
                statement.setInt(2, reasonNumber);
                if (reasonNumber == 8 || reasonNumber == 9 || reasonNumber == 99) {
                    // Set duration to permanent for reasons 8, 9, and 99
                    duration = "Permanent";
                }
                statement.setString(3, duration);
                statement.setString(4, String.valueOf(bannedBy));
                statement.setString(5, ipAddress);
                statement.setString(6, banID);

                String apiUrl = "http://ip-api.com/xml/" + ipAddress + "?fields=country,regionName,city,isp,proxy&lang=de";
                try {
                    URL url = new URL(apiUrl);
                    HttpURLConnection connection1 = (HttpURLConnection) url.openConnection();

                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection1.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    connection1.disconnect();
                    String line;

                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }

                    reader.close();

                    String isp = response.substring(response.indexOf("<isp>") + 5, response.indexOf("</isp>"));
                    String proxy = response.substring(response.indexOf("<proxy>") + 7, response.indexOf("</proxy>"));
                    String country = response.substring(response.indexOf("<country>") + 9, response.indexOf("</country>"));
                    String region = response.substring(response.indexOf("<regionName>") + 12, response.indexOf("</regionName>"));
                    String city = response.substring(response.indexOf("<city>") + 6, response.indexOf("</city>"));

                    statement.setString(7, country);
                    statement.setString(8, region);
                    statement.setString(9, city);
                    statement.setString(10, proxy);
                    statement.setString(11, isp);
                } catch (IOException e) {
                    Vubex_proxy.getInstance().getLogger().log(Level.SEVERE, "Fehler beim ip info api server.", e);
                    return false; // Fehler behandelt, Bannvorgang fehlgeschlagen
                }

                statement.setInt(12, 1);
                statement.setTimestamp(13, new Timestamp(System.currentTimeMillis()));

                ProxyServer.getInstance().getLogger().info("Executing query: " + statement);

                statement.executeUpdate();

                ProxyServer.getInstance().getLogger().info("Player successfully banned: " + uuid);
            } catch (SQLException e) {
                ProxyServer.getInstance().getLogger().severe("Error executing ban query: " + e);
            }
        } catch (SQLException e) {
            ProxyServer.getInstance().getLogger().severe("Error connecting to the database: " + e);
        }
        return true;
    }

    public boolean isBanned(UUID player) {
        try (Connection connection = mysqlManager.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(
                    "SELECT * FROM bans WHERE uuid = ? AND ban_active = 1")) {

                statement.setString(1, player.toString());
                ResultSet resultSet = statement.executeQuery();

                return resultSet.next();

            } catch (SQLException e) {
                Vubex_proxy.getInstance().getLogger().log(Level.SEVERE, "Error checking ban status for player.", e);
            }
        } catch (SQLException e) {
            Vubex_proxy.getInstance().getLogger().log(Level.SEVERE, "Error connecting to the database.", e);
        }
        return false;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerLogin(LoginEvent event) {
        UUID player = event.getConnection().getUniqueId();

        checkAndUpdateBanStatus();

        if (isBanned(player)) {
            event.setCancelled(true);
            event.setCancelReason(new TextComponent(ChatUtils.getColoredText(getBanMessage(player))));
        }
    }

    public void checkAndUpdateBanStatus() {
        try (Connection connection = mysqlManager.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(
                    "UPDATE bans SET ban_active = 0 WHERE ban_active = 1 AND (ban_time + INTERVAL ? SECOND <= NOW()) AND duration != 'Permanent'")) {

                try (PreparedStatement durationStatement = connection.prepareStatement(
                        "SELECT duration, ban_time FROM bans WHERE ban_active = 1 AND duration != 'Permanent'")) {

                    ResultSet durationResultSet = durationStatement.executeQuery();

                    while (durationResultSet.next()) {
                        String duration = durationResultSet.getString("duration");
                        long durationSeconds = parseDurationToSeconds(duration);
                        Timestamp banTime = durationResultSet.getTimestamp("ban_time");

                        long currentTimeMillis = System.currentTimeMillis();
                        long elapsedTimeMillis = currentTimeMillis - banTime.getTime();
                        long remainingSeconds = durationSeconds - TimeUnit.MILLISECONDS.toSeconds(elapsedTimeMillis);

                        statement.setLong(1, remainingSeconds);
                        statement.addBatch();
                    }

                    statement.executeBatch();

                } catch (SQLException e) {
                    Vubex_proxy.getInstance().getLogger().log(Level.SEVERE, "Error updating ban status in the database.", e);
                }

            } catch (SQLException e) {
                Vubex_proxy.getInstance().getLogger().log(Level.SEVERE, "Error updating ban status in the database.", e);
            }
        } catch (SQLException e) {
            Vubex_proxy.getInstance().getLogger().log(Level.SEVERE, "Error connecting to the database.", e);
        }
    }

    public boolean isPlayerAlreadyBanned(Connection connection, String uuid) {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT * FROM bans WHERE uuid = ? AND ban_active = 1")) {

            statement.setString(1, uuid);
            ResultSet resultSet = statement.executeQuery();

            return resultSet.next();

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error checking if player is already banned.", e);
            return false;
        }
    }

    public String getBanMessage(UUID player) {
        try (Connection connection = mysqlManager.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(
                    "SELECT reason, duration, ban_id, ban_time FROM bans WHERE uuid = ? AND ban_active = 1")) {

                statement.setString(1, player.toString());
                ResultSet resultSet = statement.executeQuery();

                if (resultSet.next()) {
                    int reasonNumber = resultSet.getInt("reason");
                    String duration = resultSet.getString("duration");
                    String banId = resultSet.getString("ban_id");

                    long banTimeMillis = resultSet.getTimestamp("ban_time").getTime();
                    long currentTimeMillis = System.currentTimeMillis();
                    long elapsedTimeMillis = currentTimeMillis - banTimeMillis;
                    long remainingSeconds = parseDurationToSeconds(duration) - TimeUnit.MILLISECONDS.toSeconds(elapsedTimeMillis);

                    StringBuilder banMessageBuilder = new StringBuilder();
                    if (duration.equalsIgnoreCase("Permanent")) {
                        banMessageBuilder.append("""
                                &8« &e&lVubex.DE &r&8»

                                &cDu bist Permanent von unserem Netzwerk ausgeschlossen.

                                &7Grund: &e""").append(getBanReason(reasonNumber).replace("/", "&7/&e"));
                    } else {
                        banMessageBuilder.append("""
                                &8« &e&lVubex.DE &r&8»

                                &cDu bist vorübergehend von unserem Netzwerk ausgeschlossen.

                                &7Grund: &e""").append(getBanReason(reasonNumber).replace("/", "&7/&e"));
                        long remainingDays = remainingSeconds / (60 * 60 * 24);
                        long remainingHours = (remainingSeconds % (60 * 60 * 24)) / (60 * 60);
                        long remainingMinutes = (remainingSeconds % (60 * 60)) / 60;
                        remainingSeconds = remainingSeconds % 60;

                        if (remainingSeconds <= 0) {
                            banMessageBuilder.append("\n\n").append("&7Verbleibende Zeit: &eKeine");
                        }else{
                            boolean timeAdded = false;

                            if (remainingDays > 0) {
                                banMessageBuilder.append("\n\n").append("&7Verbleibende Zeit: &e").append(remainingDays).append(" &eTag(e)");
                                timeAdded = true;
                            }
                            if (remainingHours > 0 || timeAdded) {
                                if (timeAdded) banMessageBuilder.append("&7, &e");
                                banMessageBuilder.append(remainingHours).append(" &eStunde(n)");
                                timeAdded = true;
                            }
                            if (remainingMinutes > 0 || timeAdded) {
                                if (timeAdded) banMessageBuilder.append("&7, &e");
                                banMessageBuilder.append(remainingMinutes).append(" &eMinute(n)");
                                timeAdded = true;
                            }
                            if (remainingSeconds > 1 || timeAdded) {
                                if (timeAdded) banMessageBuilder.append("&7, &e");
                                banMessageBuilder.append(remainingSeconds).append(" &eSekunde(n)");
                            }
                        }
                    }
                    banMessageBuilder.append("\n\n\n\n&7Ban-ID: &e").append(banId).append("\n\n").append("&7Unrecht gebannt? Wir helfen dir im Forum gerne weiter.\n\n").append("&ehttps://vubex.de/forum");
                    return banMessageBuilder.toString();
                }

            } catch (SQLException e) {
                Vubex_proxy.getInstance().getLogger().log(Level.SEVERE, "Error retrieving ban message for player.", e);
            }
        } catch (SQLException e) {
            Vubex_proxy.getInstance().getLogger().log(Level.SEVERE, "Error connecting to the database.", e);
        }
        return "Du wurdest gebannt.";
    }

    public void insertPlayerInfo(UUID uuid, String ipAddress, String isp, String proxy, String city, String region, String country) {
        try (Connection connection = mysqlManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "INSERT INTO users (uuid, last_ip_address, isp, proxy, city, region, country, last_player_name) " +
                             "VALUES (?, ?, ?, ?, ?, ?, ?, ?) " +
                             "ON DUPLICATE KEY UPDATE last_ip_address = VALUES(last_ip_address), isp = VALUES(isp), " +
                             "proxy = VALUES(proxy), city = VALUES(city), region = VALUES(region), country = VALUES(country), last_player_name = VALUES(last_player_name)")) {

            statement.setString(1, uuid.toString());
            statement.setString(2, ipAddress);
            statement.setString(3, isp);
            statement.setString(4, proxy);
            statement.setString(5, city);
            statement.setString(6, region);
            statement.setString(7, country);
            statement.setString(8, ChatUtils.getOfflinePlayerName(uuid));

            statement.executeUpdate();
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "An error occurred while inserting player info.", e);
        }
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