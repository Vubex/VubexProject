package de.flavius.vubex.vubexproxy.commands;

import de.flavius.vubex.vubexproxy.api.VubexCoinAPI;
import de.flavius.vubex.vubexproxy.api.VubexVanishAPI;
import de.flavius.vubex.vubexproxy.mysql.MySQLManager;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;
import de.flavius.vubex.vubexproxy.Vubex_proxy;
import de.flavius.vubex.vubexproxy.utils.ChatUtils;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.TabExecutor;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

/**
 * @author : flavius
 * project : VubexProject
 * created : 10.09.2023, Sonntag
 **/
public class AdvertCommand extends Command implements TabExecutor {

    private final MySQLManager mySQLManager;

    public AdvertCommand(MySQLManager mySQLManager) {
        super("advert", null);
        this.mySQLManager = mySQLManager;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof ProxiedPlayer player)) {
            sender.sendMessage(new TextComponent(Vubex_proxy.serverPrefix + Vubex_proxy.onlyPlayerCommand));
            return;
        }

        if (args.length != 1) {
            player.sendMessage(new TextComponent(Vubex_proxy.serverPrefix + ChatUtils.getColoredText("&7Verwendung: &e/advert [Spieler]")));
            return;
        }

        String playerName = args[0];
        UUID playerUUID = getUUIDFromDatabase(playerName);

        if (playerUUID == null) {
            player.sendMessage(new TextComponent(Vubex_proxy.serverPrefix + ChatUtils.getColoredText("&7Der &7angegebene &7Spieler &7konnte &7nicht &7gefunden &7werden.")));
            return;
        }

        try {
            Connection connection = mySQLManager.getConnection();

            // Überprüfe, ob der Spieler bereits einen Advert gemacht hat
            try (PreparedStatement checkAdvertStatement = connection.prepareStatement(
                    "SELECT advert_uuid FROM users WHERE uuid = ?"
            )) {
                checkAdvertStatement.setString(1, player.getUniqueId().toString());

                String existingAdvertUUID = null;
                try (var resultSet = checkAdvertStatement.executeQuery()) {
                    if (resultSet.next()) {
                        existingAdvertUUID = resultSet.getString("advert_uuid");
                    }
                }

                if (existingAdvertUUID != null) {
                    player.sendMessage(new TextComponent(Vubex_proxy.serverPrefix + ChatUtils.getColoredText("Du wurdest bereits vom Spieler &e" + ChatUtils.getOfflinePlayerName(UUID.fromString(existingAdvertUUID)) + " &7beworben.")));
                    return;
                }
            }

            // Überprüfe, ob der Spieler den Advert-Befehl noch verwenden darf (maximal 30 Tage nach dem ersten Server betritt)
            try (PreparedStatement checkFirstJoinedStatement = connection.prepareStatement(
                    "SELECT first_joined FROM users WHERE uuid = ?"
            )) {
                checkFirstJoinedStatement.setString(1, player.getUniqueId().toString());

                try (var resultSet = checkFirstJoinedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        Timestamp firstJoined = resultSet.getTimestamp("first_joined");
                        Timestamp thirtyDaysLater = new Timestamp(firstJoined.getTime() + (30L * 24L * 60L * 60L * 1000L));

                        if (System.currentTimeMillis() > thirtyDaysLater.getTime()) {
                            player.sendMessage(new TextComponent(Vubex_proxy.serverPrefix + ChatUtils.getColoredText("&7Du &7kannst &7den &7Advert-Befehl &7nur &7innerhalb &7der &7ersten &e30 &eTage &7nach &7deinem &7ersten &7Server &7betritt &7verwenden.")));
                            return;
                        }
                    }
                }
            }

            // Überprüfe, ob der ausgewählte Spieler bezahlt werden kann.
            try (PreparedStatement checkCoinsStatement = connection.prepareStatement(
                    "SELECT uuid FROM coins WHERE uuid = ?"
            )) {
                checkCoinsStatement.setString(1, playerUUID.toString());

                try (ResultSet resultSet = checkCoinsStatement.executeQuery()) {
                    if (!resultSet.next()) {
                        player.sendMessage(new TextComponent(Vubex_proxy.serverPrefix + ChatUtils.getColoredText("&7Der &7Spieler &e" + playerName + " &7ist &7noch &7nicht &in &7unserer &7Coin-Datenbank.")));
                        player.sendMessage(new TextComponent(Vubex_proxy.serverPrefix + ChatUtils.getColoredText("&7Er &7sollte &7versuchen &7sich &7erneut &7zum &7Server &7zu &7verbinden.")));
                        return;
                    }
                }
            }

            // Füge den Advert in die Datenbank ein
            try (PreparedStatement insertAdvertStatement = connection.prepareStatement(
                    "UPDATE users SET advert_uuid = ? WHERE uuid = ?"
            )) {
                insertAdvertStatement.setString(1, playerUUID.toString());
                insertAdvertStatement.setString(2, player.getUniqueId().toString());
                insertAdvertStatement.executeUpdate();

                VubexCoinAPI coinAPI = new VubexCoinAPI(mySQLManager);

                int currentCoins;
                currentCoins = coinAPI.getCoins(playerUUID);

                int coinsAmount = 750; // Belohnungs Coins

                int maxToAdd = 999999999 - currentCoins;
                coinsAmount = Math.min(coinsAmount, maxToAdd);

                int newCoinsAmount = Math.min(currentCoins + coinsAmount, 999999999);
                coinAPI.setCoins(playerUUID, newCoinsAmount);

                player.sendMessage(new TextComponent(Vubex_proxy.serverPrefix + ChatUtils.getColoredText("Du wurdest von &e" + playerName + " &7beworben. &7Dafür &7erhält &7er &e" + coinsAmount + " &eCoins&7.")));
                ProxiedPlayer targetPlayer = ProxyServer.getInstance().getPlayer(playerName);
                if (targetPlayer.isConnected()) {
                    targetPlayer.sendMessage(new TextComponent(Vubex_proxy.serverPrefix + ChatUtils.getColoredText("&7Weil &7du &e" + player.getName() + " &7beworben &7hast, &7erhältst &7du &e" + coinsAmount + " &eCoins&7.")));
                }
            }
        } catch (SQLException e) {
            Vubex_proxy.getInstance().getLogger().log(Level.SEVERE, "Ein SQL-Fehler ist aufgetreten beim Versuch, einen Advert zu erstellen:", e);
        }
    }

    private UUID getUUIDFromDatabase(String playerName) {
        try (Connection connection = mySQLManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT uuid FROM users WHERE last_player_name = ?")) {

            statement.setString(1, playerName);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return UUID.fromString(resultSet.getString("uuid"));
                }
            }
        } catch (SQLException e) {
            Vubex_proxy.getInstance().getLogger().log(Level.SEVERE,
                    () -> "Fehler beim Abrufen der UUID aus der Datenbank. identifier: " + playerName + ", " + e.getMessage()
            );
        }
        return null;
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        List<String> completions = new ArrayList<>();
        if (sender instanceof ProxiedPlayer player) {
            VubexVanishAPI vanishAPI = new VubexVanishAPI(mySQLManager);

            if (args.length == 1) {

                String currentServerName = player.getServer().getInfo().getName();
                String partialName = args[0].toLowerCase();

                for (ProxiedPlayer onlinePlayer : ProxyServer.getInstance().getPlayers()) {
                    String playerName = onlinePlayer.getName().toLowerCase();
                    boolean isVanished = vanishAPI.getVanishStatus(player.getUniqueId());

                    if ((!isVanished || player.hasPermission(VubexVanishAPI.VANISH_SEE_PERMISSION)) && onlinePlayer.getServer().getInfo().getName().equals(currentServerName) && playerName.startsWith(partialName)) {
                        completions.add(player.getName());
                    }
                }
            }
        }

        return completions;
    }
}