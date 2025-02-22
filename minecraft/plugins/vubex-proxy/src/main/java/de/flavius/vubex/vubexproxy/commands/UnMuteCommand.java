package de.flavius.vubex.vubexproxy.commands;

import de.flavius.vubex.vubexproxy.Vubex_proxy;
import de.flavius.vubex.vubexproxy.api.VubexDiscordAPI;
import de.flavius.vubex.vubexproxy.mysql.MySQLManager;
import de.flavius.vubex.vubexproxy.utils.ChatUtils;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

/**
 * @author : flavius
 * project : VubexProject
 **/
public class UnMuteCommand extends Command implements TabExecutor {

    private final MySQLManager mysqlManager;
    private final VubexDiscordAPI vubexDiscordAPI;

    public UnMuteCommand(MySQLManager mysqlManager, VubexDiscordAPI vubexDiscordAPI) {
        super("unmute", null);
        this.mysqlManager = mysqlManager;
        this.vubexDiscordAPI = vubexDiscordAPI;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(!sender.hasPermission("vubex.unmute")){
            sender.sendMessage(new TextComponent(Vubex_proxy.serverPrefix + ChatUtils.getColoredText("&7Diesen &7Befehl &7darfst &7du &7nicht &7ausführen.")));
            return;
        }

        if (args.length != 1) {
            sender.sendMessage(new TextComponent(Vubex_proxy.serverPrefix + "Verwendung: /unmute [Spieler/UUID/MuteID]"));
            return;
        }

        String targetIdentifier = args[0];
        UUID uuid = null;
        boolean foundMuteInfo = false;

        try (Connection connection = mysqlManager.getConnection();
             PreparedStatement selectStatement = connection.prepareStatement(
                     "SELECT * FROM mutes WHERE uuid = ? OR mute_id = ?")) {

            try {
                uuid = UUID.fromString(targetIdentifier);
            } catch (IllegalArgumentException ignored) {
                // Ignore, uuid will remain null
            }

            if (uuid != null) {
                selectStatement.setString(1, uuid.toString());
                selectStatement.setString(2, "invalid_mute_id");
            } else {
                // Wenn keine gültige UUID gefunden wurde, versuche mit der Mute-ID
                selectStatement.setString(1, "invalid_uuid");
                selectStatement.setString(2, targetIdentifier);
            }

            try (ResultSet resultSet = selectStatement.executeQuery()) {
                if (resultSet.next()) {
                    foundMuteInfo = true;
                    if (!resultSet.getBoolean("mute_active")) {
                        sender.sendMessage(new TextComponent(Vubex_proxy.serverPrefix + "Der Mute (ID: " + args[0] + ") ist bereits abgelaufen."));
                        return;
                    }
                    uuid = UUID.fromString(resultSet.getString("uuid"));
                }
            }
            if (foundMuteInfo) {
                try (PreparedStatement updateStatement = connection.prepareStatement(
                        "UPDATE mutes SET mute_active = 0 WHERE uuid = ? AND mute_active = 1")) {

                    updateStatement.setString(1, uuid.toString());
                    int updatedRows = updateStatement.executeUpdate();

                    if (updatedRows > 0) {
                        sendBroadcastMessage(sender, Vubex_proxy.serverPrefix);
                        sendBroadcastMessage(sender, ChatUtils.getColoredText(Vubex_proxy.serverPrefix + "&e" + ChatUtils.getOfflinePlayerName(uuid) + "&7 wurde von &e" + sender.getName() + "&7 entmutet."));
                        sendBroadcastMessage(sender, Vubex_proxy.serverPrefix);

                        String title = "Team-Information";
                        String iconUrl = "https://mc-heads.net/head/" + uuid + "/600";
                        Color color = Color.ORANGE;
                        String[] lines = new String[]{"Der Spieler **" + ChatUtils.getOfflinePlayerName(uuid) + "** wurde von **" + sender.getName() + "** entmutet.\n"};
                        vubexDiscordAPI.sendEmbedMessage("1222293450284863498", title, lines, iconUrl, color);
                        Vubex_proxy.getInstance().getLogger().log(Level.INFO, "Spieler mit UUID " + uuid + " wurde entmutet.");
                    }
                }
            } else {
                uuid = ChatUtils.getUUIDFromPlayerName(targetIdentifier);
                if (uuid != null) {
                    try (PreparedStatement secondSelectStatement = connection.prepareStatement(
                            "SELECT * FROM mutes WHERE uuid = ? AND mute_active = true")) {

                        secondSelectStatement.setString(1, uuid.toString());

                        try (ResultSet playerNameResultSet = secondSelectStatement.executeQuery()) {
                            if (playerNameResultSet.next()) {
                                foundMuteInfo = true;
                            }
                        }

                        if (foundMuteInfo) {
                            try (PreparedStatement updateStatement = connection.prepareStatement(
                                    "UPDATE mutes SET mute_active = 0 WHERE uuid = ? AND mute_active = 1")) {

                                updateStatement.setString(1, uuid.toString());
                                int updatedRows = updateStatement.executeUpdate();

                                if (updatedRows > 0) {
                                    sendBroadcastMessage(sender, Vubex_proxy.serverPrefix);
                                    sendBroadcastMessage(sender, ChatUtils.getColoredText(Vubex_proxy.serverPrefix + "&e" + ChatUtils.getOfflinePlayerName(uuid) + "&7 wurde von &e" + sender.getName() + "&7 entmutet."));
                                    sendBroadcastMessage(sender, Vubex_proxy.serverPrefix);

                                    String title = "Team-Information";
                                    String iconUrl = "https://mc-heads.net/head/" + uuid + "/600";
                                    Color color = Color.ORANGE;
                                    String[] lines = new String[]{"Der Spieler **" + ChatUtils.getOfflinePlayerName(uuid) + "** wurde von **" + sender.getName() + "** entmutet.\n"};
                                    vubexDiscordAPI.sendEmbedMessage("1222293450284863498", title, lines, iconUrl, color);
                                    Vubex_proxy.getInstance().getLogger().log(Level.INFO, "Spieler mit UUID " + uuid + " wurde entmutet.");
                                }
                            }
                        }
                    } catch (SQLException e) {
                        handleError(sender, e);
                    }
                }
            }

            if (!foundMuteInfo) {
                sender.sendMessage(new TextComponent(Vubex_proxy.serverPrefix + "Dieser Spieler wurde nicht gefunden."));
            }

        } catch (SQLException e) {
            handleError(sender, e);
        }
    }

    private void sendBroadcastMessage(CommandSender sender, String message) {
        Vubex_proxy.getInstance().getProxy().getPlayers().stream()
                .filter(player -> player.hasPermission("vubex.mute.broadcast"))
                .forEach(player -> player.sendMessage(new TextComponent(message)));

        Vubex_proxy.getInstance().getLogger().info(sender.getName() + " sent a broadcast message: " + message);
    }

    private void handleError(CommandSender sender, Exception e) {
        if (sender != null) {
            sender.sendMessage(new TextComponent(Vubex_proxy.serverPrefix + "Ein Fehler ist aufgetreten."));
        }
        Vubex_proxy.getInstance().getLogger().log(Level.SEVERE, "Ein Fehler ist aufgetreten.", e);
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender commandSender, String[] args) {
        List<String> completions = new ArrayList<>();
        if(commandSender.hasPermission("vubex.unmute") && args.length == 1){
            try (Connection connection = mysqlManager.getConnection();
                 PreparedStatement selectStatement = connection.prepareStatement(
                         "SELECT uuid FROM mutes WHERE mute_active = true"
                 );
                 ResultSet resultSet = selectStatement.executeQuery()) {

                while (resultSet.next()) {
                    String partialName = args[0].toLowerCase();
                    String uuidString = resultSet.getString("uuid");
                    UUID uuid = UUID.fromString(uuidString);
                    String playerName = ChatUtils.getOfflinePlayerName(uuid);
                    if(playerName.toLowerCase().startsWith(partialName)){
                        completions.add(playerName);
                    }
                }
            } catch (SQLException e) {
                Vubex_proxy.getInstance().getLogger().log(Level.SEVERE, "Ein Fehler ist aufgetreten, während stummgeschaltete Spieler abgerufen wurden.", e);
            }
        }

        return completions;
    }
}