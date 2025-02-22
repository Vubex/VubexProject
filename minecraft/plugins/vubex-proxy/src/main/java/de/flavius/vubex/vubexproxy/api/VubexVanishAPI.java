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
public class VubexVanishAPI {
    private final MySQLManager mysqlManager;
    public static final String VANISH_SEE_PERMISSION = "vubex.vanish.see";

    public VubexVanishAPI(MySQLManager mysqlManager) {
        this.mysqlManager = mysqlManager;
    }

    public void setVanishStatus(UUID uuid, boolean vanished) {
        String updateVanishStatusSQL = "INSERT INTO vanish (uuid, vanish) VALUES (?, ?)" +
                "ON DUPLICATE KEY UPDATE vanish = ?";
        executeUpdate(updateVanishStatusSQL, uuid.toString(), vanished, vanished);
    }

    public boolean getVanishStatus(UUID uuid) {
        String selectVanishStatusSQL = "SELECT vanish FROM vanish WHERE uuid = ?";
        return executeQueryForBoolean(selectVanishStatusSQL, uuid.toString());
    }

    private void executeUpdate(String query, Object... params) {
        try (Connection connection = mysqlManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            for (int i = 0; i < params.length; i++) {
                statement.setObject(i + 1, params[i]);
            }
            statement.executeUpdate();
        } catch (SQLException e) {
            Vubex_proxy.getInstance().getLogger().log(Level.SEVERE, "An error occurred while executing update query.", e);
        }
    }

    private boolean executeQueryForBoolean(String query, Object... params) {
        try (Connection connection = mysqlManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            for (int i = 0; i < params.length; i++) {
                statement.setObject(i + 1, params[i]);
            }
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return rs.getBoolean(1);
                }
            }
        } catch (SQLException e) {
            Vubex_proxy.getInstance().getLogger().log(Level.SEVERE, "An error occurred while executing query for boolean.", e);
        }
        return false;
    }
}