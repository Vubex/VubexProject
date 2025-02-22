package de.flavius.vubex.vubexproxy.listeners;

import de.flavius.vubex.vubexproxy.api.VubexCoinAPI;
import de.flavius.vubex.vubexproxy.mysql.MySQLManager;
import de.flavius.vubex.vubexproxy.utils.BanManager;
import de.flavius.vubex.vubexproxy.utils.ChatUtils;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.UUID;

/**
 * @author : flavius
 * project : VubexProject
 **/
public class JoinListener implements Listener {
    private final MySQLManager mysqlManager;
    private final VubexCoinAPI coinAPI;

    public JoinListener(MySQLManager mysqlManager) {
        this.mysqlManager = mysqlManager;
        this.coinAPI = new VubexCoinAPI(mysqlManager);
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    public void onServerJoin(PostLoginEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();

        try (Connection connection2 = mysqlManager.getConnection();
             PreparedStatement statement = connection2.prepareStatement(
                     "UPDATE users SET last_joined = ? WHERE uuid = ?")) {
            statement.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
            statement.setString(2, uuid.toString());
            statement.executeUpdate();
        } catch (SQLException e) {
            // Handle the exception appropriately
        }
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    public void onLogin(LoginEvent event) {
        UUID uuid = event.getConnection().getUniqueId();

        coinAPI.createPlayer(uuid);

        InetSocketAddress targetSocketAddress = (InetSocketAddress) event.getConnection().getSocketAddress();
        String ipAddress = targetSocketAddress.getAddress().getHostAddress();
        String apiUrl = "http://ip-api.com/xml/" + ipAddress + "?fields=country,regionName,city,isp,proxy&lang=de";
        try {
            URL url = new URL(apiUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                StringBuilder response = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }

                String isp = parseValueFromXmlTag(response.toString(), "isp");
                String proxy = parseValueFromXmlTag(response.toString(), "proxy");
                String country = parseValueFromXmlTag(response.toString(), "country");
                String region = parseValueFromXmlTag(response.toString(), "regionName");
                String city = parseValueFromXmlTag(response.toString(), "city");

                BanManager banManager = new BanManager(mysqlManager);
                banManager.insertPlayerInfo(uuid, ipAddress, isp, proxy, city, region, country);
                mysqlManager.insertPlayerInfo(uuid, ipAddress, isp, proxy, city, region, country);
                try (Connection connection2 = mysqlManager.getConnection();
                     PreparedStatement statement = connection2.prepareStatement(
                             "UPDATE users SET last_player_name = ? WHERE uuid = ?")) {
                    statement.setString(1, ChatUtils.getOfflinePlayerName(uuid));
                    statement.setString(2, uuid.toString());
                    statement.executeUpdate();
                } catch (SQLException e) {
                    // Handle the exception appropriately
                }

                try (Connection connection2 = mysqlManager.getConnection();
                     PreparedStatement statement = connection2.prepareStatement(
                             "UPDATE users SET last_join_attempt = ? WHERE uuid = ?")) {
                    statement.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
                    statement.setString(2, uuid.toString());
                    statement.executeUpdate();
                } catch (SQLException e) {
                    // Handle the exception appropriately
                }
            }finally {
                connection.disconnect();
            }
        } catch (IOException e) {
            event.setCancelled(true);
            event.setCancelReason(new TextComponent("Datenbankfehler"));
        }
    }

    private String parseValueFromXmlTag(String xml, String tag) {
        int startIndex = xml.indexOf("<" + tag + ">") + tag.length() + 2;
        int endIndex = xml.indexOf("</" + tag + ">");
        return xml.substring(startIndex, endIndex);
    }
}