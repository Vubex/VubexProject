package de.flavius.vubex.vubexproxy.listeners;

import de.flavius.vubex.vubexproxy.Vubex_proxy;
import de.flavius.vubex.vubexproxy.mysql.MySQLManager;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

/**
 * @author : flavius
 * project : VubexProject
 * created : 12.09.2023, Dienstag
 **/
public class OnlineTimeListener implements Listener {

    private final MySQLManager mysqlManager;
    private final Map<UUID, Long> loginTimes;

    public OnlineTimeListener(MySQLManager mysqlManager) {
        this.mysqlManager = mysqlManager;
        this.loginTimes = new HashMap<>();
    }

    @EventHandler
    public void onPostLogin(PostLoginEvent event) {
        ProxiedPlayer player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        long currentTime = System.currentTimeMillis();
        loginTimes.put(uuid, currentTime);

        // Speichere den Login-Zeitpunkt in der Datenbank
        try (Connection connection = mysqlManager.getConnection();
             PreparedStatement insertStatement = connection.prepareStatement(
                     "INSERT INTO users_onlinetime (uuid, login_time) VALUES (?, ?) " +
                             "ON DUPLICATE KEY UPDATE login_time = ?"
             )) {
            insertStatement.setString(1, uuid.toString());
            insertStatement.setLong(2, currentTime);
            insertStatement.setLong(3, currentTime);
            insertStatement.executeUpdate();
        } catch (SQLException e) {
            Vubex_proxy.getInstance().getLogger().log(Level.SEVERE, "Ein Fehler ist aufgetreten, während eine OnlineZeit von einem Spieler beim PostLoginEvent gesetzt werden sollte", e);
        }
    }

    @EventHandler
    public void onPlayerChat(ChatEvent event) {
        UUID uuid = null;
        String message = event.getMessage();

        if (message.startsWith("/onlinetime") && event.getSender() instanceof ProxiedPlayer player) {
            String[] parts = message.split(" ", 2); // Trenne Befehl und Spielername
            if (parts.length == 2) {
                // Wenn Nachricht "/onlinetime <spieler>" ist
                uuid = getUUIDFromDatabase(parts[1]);
                if(uuid != null){
                    ProxiedPlayer targetPlayer = ProxyServer.getInstance().getPlayer(uuid);
                    if (targetPlayer == null) {
                        return;
                    }
                }
            } else if (parts.length == 1) {
                // Wenn Nachricht nur "/onlinetime" ist
                uuid = player.getUniqueId();
            }
            if(uuid != null){
                long currentTime = System.currentTimeMillis();
                long loginTime = loginTimes.get(uuid);

                // Berechne die verstrichene Zeit seit dem Login
                long elapsedTime = (currentTime - loginTime) / 1000; // in Sekunden

                try (Connection connection = mysqlManager.getConnection();
                     PreparedStatement updateStatement = connection.prepareStatement(
                             "UPDATE users_onlinetime SET total_onlinetime = total_onlinetime + ? WHERE uuid = ?"
                     )) {
                    updateStatement.setLong(1, elapsedTime);
                    updateStatement.setString(2, uuid.toString());
                    updateStatement.executeUpdate();
                } catch (SQLException e) {
                    Vubex_proxy.getInstance().getLogger().log(Level.SEVERE, "Ein Fehler ist aufgetreten, während eine OnlineZeit von einem Spieler beim Command aktualisiert werden sollte", e);
                }

                loginTimes.put(uuid, currentTime);
            }
        }
    }

    @EventHandler
    public void onPlayerDisconnect(PlayerDisconnectEvent event) {
        ProxiedPlayer player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        long currentTime = System.currentTimeMillis();

        if (loginTimes.containsKey(uuid)) {
            long loginTime = loginTimes.get(uuid);
            long elapsedTime = (currentTime - loginTime) / 1000; // in Sekunden

            try (Connection connection = mysqlManager.getConnection();
                 PreparedStatement updateStatement = connection.prepareStatement(
                         "UPDATE users_onlinetime SET total_onlinetime = total_onlinetime + ? WHERE uuid = ?"
                 )) {
                updateStatement.setLong(1, elapsedTime);
                updateStatement.setString(2, uuid.toString());
                updateStatement.executeUpdate();
            } catch (SQLException e) {
                Vubex_proxy.getInstance().getLogger().log(Level.SEVERE, "Ein Fehler ist aufgetreten, während eine OnlineZeit von einem Spieler beim PlayerDisconnectEvent gesetzt werden sollte", e);
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
            Vubex_proxy.getInstance().getLogger().log(Level.SEVERE, "Fehler beim Abrufen der UUID aus der Datenbank. identifier: " + playerName, e);
        }
        return null;
    }
}