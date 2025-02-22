package de.flavius.vubex.vubexproxy.mysql;

import de.flavius.vubex.vubexproxy.Vubex_proxy;
import java.sql.*;
import java.util.UUID;
import java.util.logging.Level;

/**
 * @author : flavius
 * project : VubexProject
 **/
public class MySQLManager {

    private final String host, database, username, password;
    private Connection connection;

    public MySQLManager(String host, String database, String username, String password) {
        this.host = host;
        this.database = database;
        this.username = username;
        this.password = password;
    }

    public void connect() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection("jdbc:mysql://" + host + "/" + database, username, password);
        }
    }

    public void disconnect() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    public Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connect();
        }
        return connection;
    }

    public void insertPlayerInfo(UUID uuid, String ipAddress, String isp, String proxy, String city, String region, String country) {
        try (Connection conn = getConnection();
             PreparedStatement statement = conn.prepareStatement(
                     "INSERT INTO users (uuid, last_ip_address, isp, proxy, city, region, country, first_joined) " +
                             "VALUES (?, ?, ?, ?, ?, ?, ?, ?) " +
                             "ON DUPLICATE KEY UPDATE " +
                             "last_ip_address = VALUES(last_ip_address), " +
                             "isp = VALUES(isp), " +
                             "proxy = VALUES(proxy), " +
                             "city = VALUES(city), " +
                             "region = VALUES(region), " +
                             "country = VALUES(country)")) {
            statement.setString(1, uuid.toString());
            statement.setString(2, ipAddress);
            statement.setString(3, isp);
            statement.setString(4, proxy);
            statement.setString(5, city);
            statement.setString(6, region);
            statement.setString(7, country);
            statement.setTimestamp(8, new Timestamp(System.currentTimeMillis()));
            statement.executeUpdate();
        } catch (SQLException e) {
            Vubex_proxy.getInstance().getLogger().log(Level.SEVERE, "Fehler beim Einfügen oder Aktualisieren von Spielerinformationen in die Datenbank für UUID: " + uuid.toString(), e);
        }
    }

    public void createTablesIfNotExists() {
        try (Connection conn = getConnection();
             Statement statement = conn.createStatement()) {
            String createUsersTableSQL = "CREATE TABLE IF NOT EXISTS users (" +
                    "uuid VARCHAR(36) PRIMARY KEY," +
                    "last_ip_address VARCHAR(255)," +
                    "last_player_name VARCHAR(255)," +
                    "isp VARCHAR(255)," +
                    "proxy VARCHAR(255)," +
                    "city VARCHAR(255)," +
                    "region VARCHAR(255)," +
                    "country VARCHAR(255)," +
                    "first_joined TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "last_joined TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "last_join_attempt TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "advert_uuid VARCHAR(36)," +
                    "INDEX(last_player_name)" +
                    ")";
            statement.executeUpdate(createUsersTableSQL);

            String createBansTableSQL = "CREATE TABLE IF NOT EXISTS bans (" +
                    "uuid VARCHAR(36) NOT NULL," +
                    "reason INT NOT NULL," +
                    "duration VARCHAR(24) NOT NULL," +
                    "banned_by VARCHAR(36) NOT NULL," +
                    "ip VARCHAR(255) NOT NULL," +
                    "ban_id VARCHAR(6) NOT NULL," +
                    "country VARCHAR(255)," +
                    "region VARCHAR(255)," +
                    "city VARCHAR(255)," +
                    "proxy VARCHAR(255)," +
                    "isp VARCHAR(255)," +
                    "ban_active BOOLEAN DEFAULT 1," +
                    "ban_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "ban_description VARCHAR(255)," +
                    "ban_duration_changed BOOLEAN DEFAULT 0," +
                    "ban_duration_changed_who VARCHAR(36)," +
                    "ban_duration_changed_when TIMESTAMP," +
                    "ban_duration_old_duration VARCHAR(24)," +
                    "INDEX(ban_id)" +
                    ")";
            statement.executeUpdate(createBansTableSQL);

            String createMutesTableSQL = "CREATE TABLE IF NOT EXISTS mutes (" +
                    "uuid VARCHAR(36) NOT NULL," +
                    "reason INT NOT NULL," +
                    "duration VARCHAR(24) NOT NULL," +
                    "muted_by VARCHAR(36) NOT NULL," +
                    "mute_id VARCHAR(6) NOT NULL," +
                    "mute_active BOOLEAN DEFAULT 1," +
                    "mute_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "mute_description VARCHAR(255)," +
                    "mute_duration_changed BOOLEAN DEFAULT 0," +
                    "mute_duration_changed_who VARCHAR(36)," +
                    "mute_duration_changed_when TIMESTAMP," +
                    "mute_duration_old_duration VARCHAR(24)," +
                    "INDEX(mute_id)" +
                    ")";
            statement.executeUpdate(createMutesTableSQL);

            String createCoinsTableSQL = "CREATE TABLE IF NOT EXISTS coins (" +
                    "uuid VARCHAR(36) NOT NULL," +
                    "coins INT NOT NULL DEFAULT 0," +
                    "PRIMARY KEY (uuid)" +
                    ")";

            statement.executeUpdate(createCoinsTableSQL);

            String createVanishTableSQL = "CREATE TABLE IF NOT EXISTS vanish (" +
                    "uuid VARCHAR(36) PRIMARY KEY," +
                    "vanish BOOLEAN NOT NULL" +
                    ")";
            statement.executeUpdate(createVanishTableSQL);

            String createModulesTableSQL = "CREATE TABLE IF NOT EXISTS modules (" +
                    "module_name VARCHAR(255)," +
                    "enabled BOOLEAN NOT NULL" +
                    ")";
            statement.executeUpdate(createModulesTableSQL);

            String createMaintenanceModuleWhitelistSQL = "CREATE TABLE IF NOT EXISTS maintenanceWhitelist (" +
                    "uuid VARCHAR(36) NOT NULL," +
                    "whitelisted_by VARCHAR(255) NOT NULL" +
                    ")";
            statement.executeUpdate(createMaintenanceModuleWhitelistSQL);

            String createUsersOnlineTimeTableSQL = "CREATE TABLE IF NOT EXISTS users_onlinetime (" +
                    "uuid VARCHAR(36) PRIMARY KEY," +
                    "login_time BIGINT DEFAULT 0," +
                    "total_onlinetime BIGINT DEFAULT 0" +
                    ")";
            statement.executeUpdate(createUsersOnlineTimeTableSQL);

        } catch (SQLException e) {
            Vubex_proxy.getInstance().getLogger().log(Level.SEVERE, "Fehler beim Erstellen von Datenbanktabellen", e);
        }
    }
    public void updateModule(String moduleName, boolean newEnabledStatus) {
        try (Connection conn = getConnection();
             PreparedStatement updateStatement = conn.prepareStatement(
                     "UPDATE modules SET enabled = ? WHERE module_name = ?"
             )) {
            updateStatement.setBoolean(1, newEnabledStatus);
            updateStatement.setString(2, moduleName);
            int rowsAffected = updateStatement.executeUpdate();

            if (rowsAffected == 0) {
                // Das Modul existiert nicht, füge es hinzu
                insertModuleIfNotExists(moduleName, newEnabledStatus);
            }
        } catch (SQLException e) {
            Vubex_proxy.getInstance().getLogger().log(Level.SEVERE, "Fehler beim Aktualisieren des Moduls auf " + newEnabledStatus + " für Modul: " + moduleName, e);
        }
    }

    public boolean getModule(String moduleName) {
        try (Connection conn = getConnection();
             PreparedStatement selectStatement = conn.prepareStatement(
                     "SELECT enabled FROM modules WHERE module_name = ?"
             )) {
            selectStatement.setString(1, moduleName);
            try (ResultSet resultSet = selectStatement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getBoolean("enabled");
                }
            }
        } catch (SQLException e) {
            Vubex_proxy.getInstance().getLogger().log(Level.SEVERE, "Fehler beim Abrufen des Modulstatus für Modul: ", e);
        }
        return false;
    }

    public void insertModuleIfNotExists(String moduleName, boolean enabled) {
        try (Connection conn = getConnection();
             PreparedStatement selectStatement = conn.prepareStatement(
                     "SELECT COUNT(*) FROM modules WHERE module_name = ?"
             )) {
            selectStatement.setString(1, moduleName);
            try (ResultSet resultSet = selectStatement.executeQuery()) {
                if (resultSet.next() && resultSet.getInt(1) == 0) {
                    try (PreparedStatement insertStatement = conn.prepareStatement(
                            "INSERT INTO modules (module_name, enabled) VALUES (?, ?)"
                    )) {
                        insertStatement.setString(1, moduleName);
                        insertStatement.setBoolean(2, enabled);
                        insertStatement.executeUpdate();
                    }
                }
            }
        } catch (SQLException e) {
            Vubex_proxy.getInstance().getLogger().log(Level.SEVERE, "Fehler beim Einfügen des Moduls: " + moduleName, e);
        }
    }
}
