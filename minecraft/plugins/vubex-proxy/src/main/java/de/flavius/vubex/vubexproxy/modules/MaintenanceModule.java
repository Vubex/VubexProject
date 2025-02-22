package de.flavius.vubex.vubexproxy.modules;

import de.flavius.vubex.vubexproxy.Vubex_proxy;
import de.flavius.vubex.vubexproxy.mysql.MySQLManager;
import de.flavius.vubex.vubexproxy.utils.ChatUtils;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.event.ProxyPingEvent;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.TabExecutor;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

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
public class MaintenanceModule extends Command implements Listener, TabExecutor {
    MySQLManager mysqlManager;

    public MaintenanceModule(MySQLManager mysqlManager) {
        super("wartungsmodus", null, "wartung", "maintenance");
        this.mysqlManager = mysqlManager;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(!sender.hasPermission("vubex.module.MaintenanceModule")){
            sender.sendMessage(new TextComponent(Vubex_proxy.serverPrefix + ChatUtils.getColoredText("&7Diesen &7Befehl &7darfst &7du &7nicht &7ausführen.")));
            return;
        }

        // Zeige Nutzung
        if (args.length == 0) {
            String infoMessage = Vubex_proxy.serverPrefix + "\n"
                    + Vubex_proxy.serverPrefix + ChatUtils.getColoredText("&e/wartungsmodus an/aus") + "\n"
                    + Vubex_proxy.serverPrefix + ChatUtils.getColoredText("&e/wartungsmodus whitelist") + "\n"
                    + Vubex_proxy.serverPrefix;

            sender.sendMessage(new TextComponent(infoMessage));

            return;
        }else if(args.length == 1 && args[0].equalsIgnoreCase("whitelist")){
            String infoMessage = Vubex_proxy.serverPrefix + "\n"
                    + Vubex_proxy.serverPrefix + ChatUtils.getColoredText("&e/wartungsmodus whitelist add <Spieler>") + "\n"
                    + Vubex_proxy.serverPrefix + ChatUtils.getColoredText("&e/wartungsmodus whitelist remove <Spieler>") + "\n"
                    + Vubex_proxy.serverPrefix + ChatUtils.getColoredText("&e/wartungsmodus whitelist list") + "\n"
                    + Vubex_proxy.serverPrefix;

            sender.sendMessage(new TextComponent(infoMessage));
            return;
        }

        String subCommand = args[0].toLowerCase();
        switch (subCommand) {
            case "on", "an" -> {
                mysqlManager.updateModule("MaintenanceModule", true);
                sender.sendMessage(new TextComponent(Vubex_proxy.serverPrefix + ChatUtils.getColoredText("&7Wartungsmodus aktiviert.")));
            }
            case "off", "aus" -> {
                mysqlManager.updateModule("MaintenanceModule", false);
                sender.sendMessage(new TextComponent(Vubex_proxy.serverPrefix + ChatUtils.getColoredText("&7Wartungsmodus deaktiviert.")));
            }
            case "whitelist" -> {
                String whitelistSub = args[1].toLowerCase();
                switch (whitelistSub) {
                    case "add" -> {
                        if (args.length != 3) {
                            return;
                        }

                        String targetName = args[2];
                        ProxiedPlayer targetPlayer = ProxyServer.getInstance().getPlayer(targetName);
                        UUID targetUUID;

                        if (targetPlayer != null) {
                            targetUUID = targetPlayer.getUniqueId();
                        } else {
                            targetUUID = ChatUtils.getUUIDFromPlayerName(targetName);

                            if (targetUUID == null) {
                                sender.sendMessage(new TextComponent(Vubex_proxy.serverPrefix + ChatUtils.getColoredText("&7Spieler wurde nicht gefunden.")));
                                return;
                            }
                        }

                        String whitelistedBy = sender.getName();

                        if (isWhitelisted(targetUUID.toString())) {
                            sender.sendMessage(new TextComponent(Vubex_proxy.serverPrefix + ChatUtils.getColoredText("&e" + targetName + " &7ist bereits auf der Whitelist.")));
                            return;
                        }

                        try (Connection connection = mysqlManager.getConnection();
                             PreparedStatement insertStatement = connection.prepareStatement(
                                     "INSERT INTO maintenanceWhitelist (uuid, whitelisted_by) VALUES (?, ?)"
                             )) {
                            insertStatement.setString(1, targetUUID.toString());
                            insertStatement.setString(2, whitelistedBy);
                            insertStatement.executeUpdate();

                            sender.sendMessage(new TextComponent(Vubex_proxy.serverPrefix + ChatUtils.getColoredText("&e" + targetName + " &7wurde zur Whitelist hinzugefügt.")));
                        } catch (SQLException e) {
                            Vubex_proxy.getInstance().getLogger().log(Level.SEVERE, "An error occurred while adding player to Whitelist.", e);
                        }
                    }
                    case "remove" -> {
                        if (args.length != 3) {
                            return;
                        }

                        String targetName = args[2];
                        ProxiedPlayer targetPlayer = ProxyServer.getInstance().getPlayer(targetName);
                        UUID targetUUID;

                        if (targetPlayer != null) {
                            targetUUID = targetPlayer.getUniqueId();
                        } else {
                            targetUUID = ChatUtils.getUUIDFromPlayerName(targetName);

                            if (targetUUID == null) {
                                sender.sendMessage(new TextComponent(Vubex_proxy.serverPrefix + ChatUtils.getColoredText("&7Spieler wurde nicht gefunden.")));
                                return;
                            }
                        }

                        if (!isWhitelisted(targetUUID.toString())) {
                            sender.sendMessage(new TextComponent(Vubex_proxy.serverPrefix + ChatUtils.getColoredText("&e" + targetName + " &7ist nicht auf der Whitelist.")));
                            return;
                        }

                        try (Connection connection = mysqlManager.getConnection();
                             PreparedStatement deleteStatement = connection.prepareStatement(
                                     "DELETE FROM maintenanceWhitelist WHERE uuid = ?"
                             )) {
                            deleteStatement.setString(1, targetUUID.toString());
                            deleteStatement.executeUpdate();

                            String playerName = ChatUtils.getOfflinePlayerName(targetUUID);
                            sender.sendMessage(new TextComponent(Vubex_proxy.serverPrefix + ChatUtils.getColoredText("&e" + playerName + " &7wurde von der Whitelist entfernt.")));
                        } catch (SQLException e) {
                            Vubex_proxy.getInstance().getLogger().log(Level.SEVERE, "An error occurred while removing a player from Whitelist.", e);
                        }
                    }
                    case "list" -> {
                        try (Connection connection = mysqlManager.getConnection();
                             PreparedStatement selectStatement = connection.prepareStatement(
                                     "SELECT uuid, whitelisted_by FROM maintenanceWhitelist"
                             );
                             ResultSet resultSet = selectStatement.executeQuery()) {

                            sender.sendMessage(new TextComponent(Vubex_proxy.serverPrefix + ChatUtils.getColoredText("&7Whitelist vom &eMaintenanceModule&7:")));
                            while (resultSet.next()) {
                                String uuidString = resultSet.getString("uuid");
                                String whitelistedByName = resultSet.getString("whitelisted_by");

                                UUID uuid = UUID.fromString(uuidString);
                                String playerName = ChatUtils.getOfflinePlayerName(uuid);

                                sender.sendMessage(new TextComponent(Vubex_proxy.serverPrefix + ChatUtils.getColoredText("&7- &e" + playerName + " &7wurde von &e" + whitelistedByName + "&7 hinzugefügt.")));
                            }
                        } catch (SQLException e) {
                            Vubex_proxy.getInstance().getLogger().log(Level.SEVERE, "An error occurred while getting players from Whitelist.", e);
                        }
                    }
                    default ->
                        sender.sendMessage(new TextComponent(Vubex_proxy.serverPrefix + ChatUtils.getColoredText("&7Falscher Subcommand. Nutze &e/wartungsmodus whitelist &7für Hilfe.")));

                }
            }
            default ->
                    sender.sendMessage(new TextComponent(Vubex_proxy.serverPrefix + ChatUtils.getColoredText("&7Falscher Subcommand. Nutze &e/wartungsmodus &7für Hilfe.")));
        }

        mysqlManager.insertModuleIfNotExists("MaintenanceModule", false);
    }

    @EventHandler
    public void onPing(ProxyPingEvent e) {
        ServerPing server = e.getResponse();

        if (mysqlManager.getModule("MaintenanceModule")) {
            server.setVersion(new ServerPing.Protocol(ChatUtils.getColoredText("&cWartungsarbeiten"), -1));

            BaseComponent motd = new TextComponent();
            motd.addExtra("Wartung an\nline 2");
            server.setDescriptionComponent(motd);
        } else {
            BaseComponent motd = new TextComponent();
            motd.addExtra("Wartung aus\nline 2");
            server.setDescriptionComponent(motd);
        }
        e.setResponse(server);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerLogin(LoginEvent event) {
        String playerUUID = event.getConnection().getUniqueId().toString();

        if (playerUUID.equals("ba671a5b-5c79-448e-b40b-8bbd64445472") || playerUUID.equals("d9c2256d-e13b-4c8c-b3c9-3461033af680")) {
            return;
        }

        if(isMaintenanceEnabled() && !isWhitelisted(playerUUID)){
            event.setCancelled(true);
            event.setCancelReason(new TextComponent("Du bist nicht auf der Whitelist für die Wartung."));
        }
    }

    public boolean isWhitelisted(String playerUUID) {
        try (Connection connection = mysqlManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT * FROM maintenanceWhitelist WHERE uuid = ?"
             )) {
            statement.setString(1, playerUUID);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        } catch (SQLException e) {
            Vubex_proxy.getInstance().getLogger().log(Level.SEVERE, "Fehler beim Überprüfen ob der Spieler in der Whitelist ist", e);
        }
        return false;
    }

    public boolean isMaintenanceEnabled() {
        try (Connection connection = mysqlManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT enabled FROM modules WHERE module_name = ?"
             )) {
            statement.setString(1, "MaintenanceModule");
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getBoolean("enabled");
                }
            }
        } catch (SQLException e) {
            Vubex_proxy.getInstance().getLogger().log(Level.SEVERE, "Fehler beim Überprüfen des Status des MaintenanceModule", e);
        }
        return false;
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        List<String> completions = new ArrayList<>();
        if(sender.hasPermission("vubex.module.MaintenanceModule")){
            if (args.length == 1) {
                String partialSubCommand = args[0].toLowerCase();
                if ("an".startsWith(partialSubCommand)) {
                    completions.add("an");
                }
                if ("aus".startsWith(partialSubCommand)) {
                    completions.add("aus");
                }
                if ("whitelist".startsWith(partialSubCommand)) {
                    completions.add("whitelist");
                }
            } else if (args.length == 2 && "whitelist".equalsIgnoreCase(args[0])) {
                String partialSubCommand = args[1].toLowerCase();
                if ("add".startsWith(partialSubCommand)) {
                    completions.add("add");
                }
                if ("remove".startsWith(partialSubCommand)) {
                    completions.add("remove");
                }
                if ("list".startsWith(partialSubCommand)) {
                    completions.add("list");
                }
            } else if (args.length == 3 && "whitelist".equalsIgnoreCase(args[0]) && "add".equalsIgnoreCase(args[1])) {
                try (Connection connection = mysqlManager.getConnection();
                     PreparedStatement selectStatement = connection.prepareStatement(
                             "SELECT uuid FROM users"
                     );
                     ResultSet resultSet = selectStatement.executeQuery()) {

                    while (resultSet.next()) {
                        String uuidString = resultSet.getString("uuid");
                        UUID uuid = UUID.fromString(uuidString);
                        String playerName = ChatUtils.getOfflinePlayerName(uuid);

                        while (resultSet.next()) {
                            String partialName = args[2].toLowerCase();
                            if(playerName.toLowerCase().startsWith(partialName)){
                                completions.add(playerName);
                            }
                        }
                    }
                } catch (SQLException e) {
                    Vubex_proxy.getInstance().getLogger().log(Level.SEVERE, "Fehler beim Abrufen von Spielernamen aus der Datenbank", e);
                }
            } else if (args.length == 3 && "whitelist".equalsIgnoreCase(args[0]) && "remove".equalsIgnoreCase(args[1])) {
                try (Connection connection = mysqlManager.getConnection();
                     PreparedStatement selectStatement = connection.prepareStatement(
                             "SELECT uuid FROM maintenanceWhitelist"
                     );
                     ResultSet resultSet = selectStatement.executeQuery()) {

                    while (resultSet.next()) {
                        String uuidString = resultSet.getString("uuid");
                        UUID uuid = UUID.fromString(uuidString);
                        String playerName = ChatUtils.getOfflinePlayerName(uuid);
                        String partialName = args[2].toLowerCase();
                        if(playerName.toLowerCase().startsWith(partialName)){
                            completions.add(playerName);
                        }
                    }
                } catch (SQLException e) {
                    Vubex_proxy.getInstance().getLogger().log(Level.SEVERE, "Fehler beim Abrufen von UUIDs aus der Whitelist-Datenbank", e);
                }
            }
        }

        return completions;
    }
}