package de.flavius.vubex.vubexproxy.api;

import de.flavius.vubex.vubexproxy.Vubex_proxy;
import de.flavius.vubex.vubexproxy.mysql.MySQLManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.logging.Level;

/**
 * @author : flavius
 * project : VubexProject
 **/
public class VubexCoinAPI {
    private final MySQLManager mysqlManager;

    public VubexCoinAPI(MySQLManager mysqlManager) {
        this.mysqlManager = mysqlManager;
    }

    private void executeUpdate(String query, Object... params) {
        try (Connection connection = mysqlManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            for (int i = 0; i < params.length; i++) {
                statement.setObject(i + 1, params[i]);
            }
            statement.executeUpdate();
        } catch (SQLException e) {
            Vubex_proxy.getInstance().getLogger().log(Level.SEVERE, "An error occurred while executing query.", e);
        }
    }

    private int executeQueryForInt(String query, Object... params) {
        try (Connection connection = mysqlManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            for (int i = 0; i < params.length; i++) {
                statement.setObject(i + 1, params[i]);
            }
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            Vubex_proxy.getInstance().getLogger().log(Level.SEVERE, "An error occurred while executing query.", e);
        }
        return 0;
    }

    public boolean playerExists(UUID uuid) {
        String query = "SELECT 1 FROM coins WHERE uuid = ?";
        return executeQueryForInt(query, uuid.toString()) == 1;
    }

    public void createPlayer(UUID uuid) {
        if (!playerExists(uuid)) {
            String query = "INSERT INTO coins (uuid, coins) VALUES (?, 1000)";
            executeUpdate(query, uuid.toString());
        }
    }

    public int getCoins(UUID uuid) {
        String query = "SELECT coins FROM coins WHERE uuid = ?";
        return executeQueryForInt(query, uuid.toString());
    }

    String updateQuery = "UPDATE coins SET coins = ? WHERE uuid = ?";

    public void addCoins(UUID uuid, int coins) {
        int currentCoins = getCoins(uuid);
        currentCoins += coins;
        executeUpdate(updateQuery, currentCoins, uuid.toString());
    }

    public void removeCoins(UUID uuid, int coins) {
        int currentCoins = getCoins(uuid);
        currentCoins -= coins;
        executeUpdate(updateQuery, currentCoins, uuid.toString());
    }

    public void setCoins(UUID uuid, int coins) {
        executeUpdate(updateQuery, coins, uuid.toString());
    }
}
