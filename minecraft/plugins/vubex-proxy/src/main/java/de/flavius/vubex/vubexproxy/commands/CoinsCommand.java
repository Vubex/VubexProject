package de.flavius.vubex.vubexproxy.commands;

import de.flavius.vubex.vubexproxy.Vubex_proxy;
import de.flavius.vubex.vubexproxy.api.VubexCoinAPI;
import de.flavius.vubex.vubexproxy.mysql.MySQLManager;
import de.flavius.vubex.vubexproxy.utils.ChatUtils;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

import static de.flavius.vubex.vubexproxy.utils.ChatUtils.getOfflinePlayerName;

/**
 * @author : flavius
 * project : VubexProject
 **/
public class CoinsCommand extends Command implements TabExecutor {
    private final MySQLManager mysqlManager;

    public CoinsCommand(MySQLManager mysqlManager) {
        super("coins", null,"mycoins");
        this.mysqlManager = mysqlManager;
    }
    @Override
    public void execute(CommandSender sender, String[] args) {
        VubexCoinAPI coinAPI = new VubexCoinAPI(mysqlManager);

        if (args.length == 0) {
            if (!(sender instanceof ProxiedPlayer player)) {
                sender.sendMessage(new TextComponent(Vubex_proxy.serverPrefix + Vubex_proxy.onlyPlayerCommand));
                return;
            }
            int coins = coinAPI.getCoins(player.getUniqueId());
            int rank = getRank(player.getUniqueId());
            player.sendMessage(new TextComponent(Vubex_proxy.serverPrefix + ChatUtils.getColoredText("&7Dein Kontostand: &e" + coins + " &eCoins&7. &e(" + rank + "&e. &ePlatz)")));
        } else if (args.length == 1) {
            if (sender.hasPermission("vubex.coins.seeothers")) {
                String targetName = args[0];
                UUID targetUUID = findUUIDFromDatabase(targetName);

                if (targetUUID == null) {
                    sender.sendMessage(new TextComponent(Vubex_proxy.serverPrefix + ChatUtils.getColoredText("&7Spieler wurde nicht gefunden.")));
                    return;
                }

                int coins = coinAPI.getCoins(targetUUID);
                int rank = getRank(targetUUID);
                sender.sendMessage(new TextComponent(Vubex_proxy.serverPrefix + ChatUtils.getColoredText("&e" + targetName + "&e's &7Kontostand: &e" + coins + " &eCoins&7. &e(" + rank + "&e. &ePlatz)")));

            } else {
                sender.sendMessage(new TextComponent(Vubex_proxy.serverPrefix + ChatUtils.getColoredText("&7Du &7hast &7keine &7Berechtigung, &7die &7Münzen &7anderer &7Spieler &7zu &7sehen.")));
            }
        }
    }

    private UUID findUUIDFromDatabase(String playerName) {
        try (Connection connection = mysqlManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT uuid FROM users WHERE last_player_name = ?")) {
            statement.setString(1, playerName);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return UUID.fromString(rs.getString("uuid"));
                }
            }
        } catch (SQLException e) {
            Vubex_proxy.getInstance().getLogger().log(Level.SEVERE, "Fehler beim Suchen der UUID aus der Datenbank für Spielername: " + playerName, e);
        }
        return null;
    }

    private int getRank(UUID playerUUID) {
        try (Connection connection = mysqlManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT COUNT(*) AS rank FROM users WHERE coins > (SELECT coins FROM users WHERE uuid = ?)")) {
            statement.setString(1, playerUUID.toString());
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("rank") + 1;
                }
            }
        } catch (SQLException e) {
            Vubex_proxy.getInstance().getLogger().log(Level.SEVERE, "Fehler beim Ermitteln des Coin-Rangs für UUID: " + playerUUID.toString(), e);
        }
        return -1;
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        List<String> completions = new ArrayList<>();
        if (sender.hasPermission("vubex.coins.seeothers")) {
            try (Connection connection = mysqlManager.getConnection();
                 PreparedStatement selectStatement = connection.prepareStatement(
                         "SELECT uuid FROM coins"
                 );
                 ResultSet resultSet = selectStatement.executeQuery()) {

                while (resultSet.next()) {
                    String uuidString = resultSet.getString("uuid");
                    UUID uuid = UUID.fromString(uuidString);
                    String playerName = getOfflinePlayerName(uuid);
                    String partialName = args[0].toLowerCase();
                    if(playerName.toLowerCase().startsWith(partialName)){
                        completions.add(playerName);
                    }
                }
            } catch (SQLException e) {
                Vubex_proxy.getInstance().getLogger().log(Level.SEVERE, "Ein Fehler ist aufgetreten, während Spieler von der Coins Datenbank abgerufen wurden.", e);
            }
        }

        return completions;
    }
}