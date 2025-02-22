package de.flavius.vubex.vubexproxy.commands;

import de.flavius.vubex.vubexproxy.Vubex_proxy;
import de.flavius.vubex.vubexproxy.api.VubexCoinAPI;
import de.flavius.vubex.vubexproxy.mysql.MySQLManager;
import de.flavius.vubex.vubexproxy.utils.ChatUtils;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

import static de.flavius.vubex.vubexproxy.utils.ChatUtils.getOfflinePlayerName;

/**
 * @author : flavius
 * project : VubexProject
 **/
public class AdminCoinsCommand extends Command implements TabExecutor {
    private final MySQLManager mysqlManager;

    public AdminCoinsCommand(MySQLManager mysqlManager) {
        super("admincoins", null,"acoins");
        this.mysqlManager = mysqlManager;
    }
    @Override
    public void execute(CommandSender sender, String[] args) {
        if(!sender.hasPermission("vubex.admincoins")){
            sender.sendMessage(new TextComponent(Vubex_proxy.serverPrefix + ChatUtils.getColoredText("&7Diesen &7Befehl &7darfst &7du &7nicht &7ausführen.")));
            return;
        }

        if (args.length == 0) {
            // Zeige Nutzung
            String infoMessage = Vubex_proxy.serverPrefix + "\n"
                    + Vubex_proxy.serverPrefix + ChatUtils.getColoredText("&e/admincoins add [Spieler] [Coinwert]") + "\n"
                    + Vubex_proxy.serverPrefix + ChatUtils.getColoredText("&e/admincoins remove [Spieler] [Coinwert]") + "\n"
                    + Vubex_proxy.serverPrefix + ChatUtils.getColoredText("&e/admincoins set [Spieler] [Coinwert]") + "\n"
                    + Vubex_proxy.serverPrefix + ChatUtils.getColoredText("&e/admincoins clear [Spieler]") + "\n"
                    + Vubex_proxy.serverPrefix + ChatUtils.getColoredText("&e/admincoins see [Spieler]") + "\n"
                    + Vubex_proxy.serverPrefix + ChatUtils.getColoredText("&e/admincoins top") + "\n"
                    + Vubex_proxy.serverPrefix;

            sender.sendMessage(new TextComponent(infoMessage));
            return;
        }else if(args[0].equalsIgnoreCase("add") || args[0].equalsIgnoreCase("remove") || args[0].equalsIgnoreCase("set")){
            if(args.length != 3){
                sender.sendMessage(new TextComponent(Vubex_proxy.serverPrefix + ChatUtils.getColoredText("&7Falsche Benutzung. Nutze &e/admincoins &7für Hilfe.")));
                return;
            }
        }else if(args[0].equalsIgnoreCase("clear") || args[0].equalsIgnoreCase("see")){
            if(args.length != 2){
                sender.sendMessage(new TextComponent(Vubex_proxy.serverPrefix + ChatUtils.getColoredText("&7Falsche Benutzung. Nutze &e/admincoins &7für Hilfe.")));
                return;
            }
        }else if(args[0].equalsIgnoreCase("top")) {
            if (args.length != 1) {
                sender.sendMessage(new TextComponent(Vubex_proxy.serverPrefix + ChatUtils.getColoredText("&7Falsche Benutzung. Nutze &e/admincoins &7für Hilfe.")));
            }else {
                try (Connection connection = mysqlManager.getConnection();
                     PreparedStatement statement = connection.prepareStatement(
                             "SELECT uuid, coins FROM coins ORDER BY coins DESC LIMIT 10")) {

                    try (ResultSet resultSet = statement.executeQuery()) {
                        StringBuilder topMessage = new StringBuilder("\n" + Vubex_proxy.serverPrefix + "&eTop 10 &7Spieler &7mit &7den &7meisten &eCoins&7:\n");
                        int rank = 1;
                        while (resultSet.next()) {
                            String playerUUID = resultSet.getString("uuid");
                            String playerName = getOfflinePlayerName(UUID.fromString(playerUUID));
                            int coins = resultSet.getInt("coins");
                            topMessage.append("&8» &e").append(rank).append("&e. &ePlatz &7- &e").append(playerName).append("&7: &e").append(coins).append(" &eCoins\n");
                            rank++;
                        }
                        sender.sendMessage(new TextComponent(ChatUtils.getColoredText(topMessage.toString())));
                    }
                } catch (SQLException e) {
                    sender.sendMessage(new TextComponent(Vubex_proxy.serverPrefix + ChatUtils.getColoredText("&7Fehler beim Abrufen der Top-Spieler.")));
                    Vubex_proxy.getInstance().getLogger().log(Level.SEVERE, "Fehler beim Abrufen der 10 Top-Spieler mit den meisten Coins.", e);
                }
            }
            return;
        }else{
            sender.sendMessage(new TextComponent(Vubex_proxy.serverPrefix + ChatUtils.getColoredText("&7Falscher Subcommand. Nutze &e/admincoins &7für Hilfe.")));
            return;
        }

        if (args[1].equals("*")) {
            String subCommand = args[0].toLowerCase();

            VubexCoinAPI coinAPI = new VubexCoinAPI(mysqlManager);
            String invalidCoinAmountMessage = Vubex_proxy.serverPrefix + ChatUtils.getColoredText("&7Ungültiger Coinwert.");

            for (ProxiedPlayer player : ProxyServer.getInstance().getPlayers()) {
                UUID targetUUID = player.getUniqueId();
                String targetName = player.getName();
                int currentCoins = coinAPI.getCoins(targetUUID);

                switch (subCommand) {
                    case "add" -> {
                        int coinsAmount;
                        try {

                            coinsAmount = Integer.parseInt(args[2]);
                            if (coinsAmount < 0) {
                                sender.sendMessage(new TextComponent(Vubex_proxy.serverPrefix + ChatUtils.getColoredText("&7Coinwert kann nicht negativ sein.")));
                                return;
                            }

                        } catch (NumberFormatException e) {
                            sender.sendMessage(new TextComponent(invalidCoinAmountMessage));
                            return;
                        }

                        int maxToAdd = 999999999 - currentCoins;
                        coinsAmount = Math.min(coinsAmount, maxToAdd);

                        int newCoinsAmount = Math.min(currentCoins + coinsAmount, 999999999);
                        coinAPI.setCoins(targetUUID, newCoinsAmount);
                        sender.sendMessage(new TextComponent(Vubex_proxy.serverPrefix + ChatUtils.getColoredText("&e" + coinsAmount + " &eCoins &7hinzugefügt &7zu &e" + targetName + "&7.")));
                    }
                    case "remove" -> {
                        int coinsAmount;
                        try {

                            coinsAmount = Integer.parseInt(args[2]);
                            if (coinsAmount < 0) {
                                sender.sendMessage(new TextComponent(Vubex_proxy.serverPrefix + ChatUtils.getColoredText("&7Coinwert kann nicht negativ sein.")));
                                return;
                            }

                        } catch (NumberFormatException e) {
                            sender.sendMessage(new TextComponent(invalidCoinAmountMessage));
                            return;
                        }

                        int newCoinsAmount = Math.max(currentCoins - coinsAmount, 0);
                        coinAPI.setCoins(targetUUID, newCoinsAmount);
                        sender.sendMessage(new TextComponent(Vubex_proxy.serverPrefix + ChatUtils.getColoredText("&e" + coinsAmount + " &eCoins &7entfernt &7von &e" + targetName + "&7.")));
                    }
                    case "set" -> {
                        int coinsAmount;
                        try {

                            coinsAmount = Integer.parseInt(args[2]);
                            if (coinsAmount < 0) {
                                sender.sendMessage(new TextComponent(Vubex_proxy.serverPrefix + ChatUtils.getColoredText("&7Coinwert kann nicht negativ sein.")));
                                return;
                            }

                        } catch (NumberFormatException e) {
                            sender.sendMessage(new TextComponent(invalidCoinAmountMessage));
                            return;
                        }

                        int newCoinsAmount = coinsAmount;
                        coinAPI.setCoins(targetUUID, newCoinsAmount);
                        sender.sendMessage(new TextComponent(Vubex_proxy.serverPrefix + ChatUtils.getColoredText("&e" + targetName + "&e's &7Kontostand &7auf &e" + coinsAmount + " &eCoins &7gesetzt.")));
                    }
                    case "clear" -> {
                        coinAPI.setCoins(targetUUID, 1000); // Coin Standartwert
                        sender.sendMessage(new TextComponent(Vubex_proxy.serverPrefix + ChatUtils.getColoredText("&e" + targetName + "&e's &7Kontostand &7wurde &7auf &7den &eStandartwert &7gesetzt.")));
                    }
                    case "see" -> {
                        int targetCoins = coinAPI.getCoins(targetUUID);
                        int targetRank = getRank(targetUUID);
                        sender.sendMessage(new TextComponent(Vubex_proxy.serverPrefix + ChatUtils.getColoredText("&e" + targetName + "&e's &7Kontostand: &e" + targetCoins + " &eCoins&7. &e(" + targetRank + "&e. &ePlatz)")));
                    }
                    default -> sender.sendMessage(new TextComponent(Vubex_proxy.serverPrefix + ChatUtils.getColoredText("&7Falscher Subcommand. Nutze &e/admincoins &7für Hilfe.")));
                }
            }
            return;
        }

        String subCommand = args[0].toLowerCase();
        String targetName = args[1];
        int coinsAmount;

        UUID targetUUID = findUUIDFromDatabase(targetName);

        if (targetUUID == null) {
            sender.sendMessage(new TextComponent(Vubex_proxy.serverPrefix + ChatUtils.getColoredText("&7Spieler wurde nicht gefunden.")));
            return;
        }

        VubexCoinAPI coinAPI = new VubexCoinAPI(mysqlManager);

        int currentCoins;
        currentCoins = coinAPI.getCoins(targetUUID);

        String negativeCoinAmountMessage = Vubex_proxy.serverPrefix + ChatUtils.getColoredText("&7Coinwert kann nicht negativ sein.");
        String invalidCoinAmountMessage = Vubex_proxy.serverPrefix + ChatUtils.getColoredText("&7Ungültiger Coinwert.");

        switch (subCommand) {
            case "add" -> {
                try {
                    coinsAmount = Integer.parseInt(args[2]);

                    if (coinsAmount < 0) {
                        sender.sendMessage(new TextComponent(negativeCoinAmountMessage));
                        return;
                    }

                    int maxToAdd = 999999999 - currentCoins;
                    coinsAmount = Math.min(coinsAmount, maxToAdd);

                    int newCoinsAmount = Math.min(currentCoins + coinsAmount, 999999999);
                    coinAPI.setCoins(targetUUID, newCoinsAmount);
                    sender.sendMessage(new TextComponent(Vubex_proxy.serverPrefix + ChatUtils.getColoredText("&e" + coinsAmount + " &eCoins &7hinzugefügt &7zu &e" + targetName + "&7.")));
                } catch (NumberFormatException e) {
                    sender.sendMessage(new TextComponent(invalidCoinAmountMessage));
                }
            }
            case "remove" -> {
                try {
                    coinsAmount = Integer.parseInt(args[2]);

                    if (coinsAmount < 0) {
                        sender.sendMessage(new TextComponent(negativeCoinAmountMessage));
                        return;
                    }
                } catch (NumberFormatException e) {
                    sender.sendMessage(new TextComponent(invalidCoinAmountMessage));
                    return;
                }

                int newCoinsAmount = Math.max(currentCoins - coinsAmount, 0);
                coinAPI.setCoins(targetUUID, newCoinsAmount);
                sender.sendMessage(new TextComponent(Vubex_proxy.serverPrefix + ChatUtils.getColoredText("&e" + coinsAmount + " &eCoins &7entfernt &7von &e" + targetName + "&7.")));
            }
            case "set" -> {
                try {
                    coinsAmount = Integer.parseInt(args[2]);

                    if (coinsAmount < 0) {
                        sender.sendMessage(new TextComponent(negativeCoinAmountMessage));
                        return;
                    }
                } catch (NumberFormatException e) {
                    sender.sendMessage(new TextComponent(invalidCoinAmountMessage));
                    return;
                }

                int newCoinsAmount = coinsAmount;
                coinAPI.setCoins(targetUUID, newCoinsAmount);
                sender.sendMessage(new TextComponent(Vubex_proxy.serverPrefix + ChatUtils.getColoredText("&e" + targetName + "&e's &7Kontostand &7auf &e" + coinsAmount + " &eCoins &7gesetzt.")));
            }
            case "clear" -> {
                coinAPI.setCoins(targetUUID, 1000); // Coin Standartwert
                sender.sendMessage(new TextComponent(Vubex_proxy.serverPrefix + ChatUtils.getColoredText("&e" + targetName + "&e's &7Kontostand &7wurde &7auf &7den &eStandartwert &7gesetzt.")));
            }
            case "see" -> {
                int targetCoins = coinAPI.getCoins(targetUUID);
                int targetRank = getRank(targetUUID);
                sender.sendMessage(new TextComponent(Vubex_proxy.serverPrefix + ChatUtils.getColoredText("&e" + targetName + "&e's &7Kontostand: &e" + targetCoins + " &eCoins&7. &e(" + targetRank + "&e. &ePlatz)")));
            }
            default -> sender.sendMessage(new TextComponent(Vubex_proxy.serverPrefix + ChatUtils.getColoredText("&7Falscher Subcommand. Nutze &e/admincoins &7für Hilfe.")));
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
            Vubex_proxy.getInstance().getLogger().log(Level.SEVERE, "Fehler beim Suchen der UUID in der Datenbank", e);
        }
        return null;
    }

    private int getRank(UUID playerUUID) {
        try (Connection connection = mysqlManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT COUNT(*) AS rank FROM coins WHERE coins > (SELECT coins FROM coins WHERE uuid = ?)")) {
            statement.setString(1, playerUUID.toString());
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("rank") + 1;
                }
            }
        } catch (SQLException e) {
            Vubex_proxy.getInstance().getLogger().log(Level.SEVERE,
                    () -> "Fehler beim Ermitteln des Coin-Rangs für UUID: " + playerUUID.toString() + ": " + e.getMessage()
            );
        }
        return -1;
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        List<String> completions = new ArrayList<>();
        if (sender.hasPermission("vubex.admincoins") && args.length == 1) {
            String partialSubCommand = args[0].toLowerCase();
            List<String> subCommands = Arrays.asList("add", "remove", "set", "clear", "see", "top");

            for (String subCommand : subCommands) {
                if (subCommand.startsWith(partialSubCommand)) {
                    completions.add(subCommand);
                }
            }
        } else if (args.length == 2) {
            String partialName = args[1].toLowerCase();

            try (Connection connection = mysqlManager.getConnection();
                 PreparedStatement selectStatement = connection.prepareStatement(
                         "SELECT uuid FROM coins"
                 );
                 ResultSet resultSet = selectStatement.executeQuery()) {

                while (resultSet.next()) {
                    String uuidString = resultSet.getString("uuid");
                    UUID uuid = UUID.fromString(uuidString);
                    String playerName = getOfflinePlayerName(uuid);

                    if (playerName.toLowerCase().startsWith(partialName)) {
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