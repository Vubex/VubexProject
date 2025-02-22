package de.flavius.vubex.vubexproxy.commands;

import de.flavius.vubex.vubexproxy.Vubex_proxy;
import de.flavius.vubex.vubexproxy.mysql.MySQLManager;
import de.flavius.vubex.vubexproxy.utils.ChatUtils;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.logging.Level;

/**
 * @author : flavius
 * project : VubexProject
 * created : 12.09.2023, Dienstag
 **/
public class OnlineTimeCommand extends Command {

    private final MySQLManager mysqlManager;

    public OnlineTimeCommand(MySQLManager mysqlManager) {
        super("onlinetime", null, "playtime");
        this.mysqlManager = mysqlManager;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(args.length > 1){
            sender.sendMessage(new TextComponent(Vubex_proxy.serverPrefix + ChatUtils.getColoredText("&7Verwendung: &e/onlinetime &e<Spieler>")));
        }

        if (!(sender instanceof ProxiedPlayer player)) {
            sender.sendMessage(new TextComponent(Vubex_proxy.serverPrefix + Vubex_proxy.onlyPlayerCommand));
            return;
        }

        UUID targetPlayer;
        if (args.length == 0) {

            try (Connection connection = mysqlManager.getConnection();
                 PreparedStatement selectStatement = connection.prepareStatement(
                         "SELECT total_onlinetime FROM users_onlinetime WHERE uuid = ?"
                 )) {

                selectStatement.setString(1, player.getUniqueId().toString());
                try (ResultSet resultSet = selectStatement.executeQuery()) {
                    if (resultSet.next()) {
                        long totalOnlineTime = resultSet.getLong("total_onlinetime");
                        String formattedTime = formatOnlineTime(totalOnlineTime);
                        player.sendMessage(new TextComponent("\n" + Vubex_proxy.serverPrefix + ChatUtils.getColoredText("&7Deine aktuelle &eOnline-Zeit &7beträgt:")));
                        player.sendMessage(new TextComponent(ChatUtils.getColoredText("&8» &e" + formattedTime + "\n")));
                    } else {
                        player.sendMessage(new TextComponent(Vubex_proxy.serverPrefix + ChatUtils.getColoredText("&7Deine &eOnline-Zeit &7wurde &7noch &7nicht &7erfasst.")));
                    }
                }
            } catch (SQLException e) {
                Vubex_proxy.getInstance().getLogger().log(Level.SEVERE, "Ein Fehler ist aufgetreten, während die OnlineTime vom Sender der Datenbank users_onlinetime abgerufen wurde.", e);
            }
        } else if (args.length == 1) {
            targetPlayer = getUUIDFromDatabase(args[0]);
            if (targetPlayer == null) {
                sender.sendMessage(new TextComponent(Vubex_proxy.serverPrefix + ChatUtils.getColoredText("&7Der &7angegebene &7Spieler &7konnte &7nicht &7gefunden &7werden.")));
                return;
            }

            String targetPlayerName = ChatUtils.getOfflinePlayerName(targetPlayer);
            try (Connection connection = mysqlManager.getConnection();
                 PreparedStatement selectStatement = connection.prepareStatement(
                         "SELECT total_onlinetime FROM users_onlinetime WHERE uuid = ?"
                 )) {

                selectStatement.setString(1, String.valueOf(targetPlayer));
                try (ResultSet resultSet = selectStatement.executeQuery()) {
                    if (resultSet.next()) {
                        long totalOnlineTime = resultSet.getLong("total_onlinetime");
                        String formattedTime = formatOnlineTime(totalOnlineTime);
                        sender.sendMessage(new TextComponent("\n" + Vubex_proxy.serverPrefix + ChatUtils.getColoredText("&7Die &eOnline-Zeit &7von &e" + targetPlayerName + " &7beträgt:")));
                        sender.sendMessage(new TextComponent(ChatUtils.getColoredText("&8» &e" + formattedTime + "\n")));
                    } else {
                        sender.sendMessage(new TextComponent(Vubex_proxy.serverPrefix + ChatUtils.getColoredText("&7Die &eOnline-Zeit &7von &e" + targetPlayerName + " &7wurde &7noch &7nicht &7erfasst.")));
                    }
                }
            } catch (SQLException e) {
                Vubex_proxy.getInstance().getLogger().log(Level.SEVERE, "Ein Fehler ist aufgetreten, während die OnlineTimes von der Datenbank users_onlinetime abgerufen wurden.", e);
            }
        }
    }

    private String formatOnlineTime(long totalOnlineTime) {
        long seconds = totalOnlineTime % 60;
        long minutes = (totalOnlineTime / 60) % 60;
        long hours = (totalOnlineTime / 3600) % 24;
        long days = (totalOnlineTime / 86400) % 365;
        long years = totalOnlineTime / 31536000;

        StringBuilder formattedTime = new StringBuilder();
        if (years > 1) {
            formattedTime.append(years).append(" &eJahre&7, &e");
        } else if (years > 0) {
            formattedTime.append(years).append(" &eJahr&7, &e");
        }
        if (days > 1) {
            formattedTime.append(days).append(" &eTage&7, &e");
        } else if (days > 0) {
            formattedTime.append(days).append(" &eTag&7, &e");
        }
        if (hours > 1) {
            formattedTime.append(hours).append(" &eStunden&7, &e");
        } else if (hours > 0) {
            formattedTime.append(hours).append(" &eStunde&7, &e");
        }
        if (minutes > 1) {
            formattedTime.append(minutes).append(" &eMinuten &7& &e");
        } else if (minutes > 0) {
            formattedTime.append(minutes).append(" &eMinute &7& &e");
        }
        if (seconds > 1) {
            formattedTime.append(seconds).append(" &eSekunden");
        }else if(seconds > 0){
            formattedTime.append(seconds).append(" &eSekunde");
        }

        return formattedTime.toString();
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