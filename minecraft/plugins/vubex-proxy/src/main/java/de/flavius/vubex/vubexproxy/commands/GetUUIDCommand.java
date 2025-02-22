package de.flavius.vubex.vubexproxy.commands;

import de.flavius.vubex.vubexproxy.Vubex_proxy;
import de.flavius.vubex.vubexproxy.mysql.MySQLManager;
import de.flavius.vubex.vubexproxy.utils.ChatUtils;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
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

/**
 * @author : flavius
 * project : VubexProject
 **/
public class GetUUIDCommand extends Command implements TabExecutor {
    public final MySQLManager mySQLManager;

    public GetUUIDCommand(MySQLManager mySQLManager) {
        super("getuuid", null, "uuid");
        this.mySQLManager = mySQLManager;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length == 1) {
            UUID target = ChatUtils.getUUIDFromPlayerName(args[0]);

            if(target == null){
                sender.sendMessage(new TextComponent(Vubex_proxy.serverPrefix + ChatUtils.getColoredText("&7Ein Spieler namens &e" + args[0] + " &7gibt es nicht.")));
                return;
            }
            sender.sendMessage(new TextComponent(Vubex_proxy.serverPrefix + ChatUtils.getColoredText("&7Die UUID von &e" + ChatUtils.getOfflinePlayerName(target) + "&7 ist: &e" + ChatUtils.getUUIDFromPlayerName(args[0]))));
        } else {
            sender.sendMessage(new TextComponent(Vubex_proxy.serverPrefix + ChatUtils.getColoredText("&7Verwendung: &e/getuuid [Spieler]")));
        }
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        List<String> completions = new ArrayList<>();
        if(sender.hasPermission("vubex.getuuid") && args.length == 1){
            String partialName = args[0].toLowerCase();

            try (Connection connection = mySQLManager.getConnection();
                 PreparedStatement selectStatement = connection.prepareStatement(
                         "SELECT uuid FROM users"
                 );
                 ResultSet resultSet = selectStatement.executeQuery()) {

                while (resultSet.next()) {
                    String uuidString = resultSet.getString("uuid");
                    UUID uuid = UUID.fromString(uuidString);
                    String playerName = ChatUtils.getOfflinePlayerName(uuid);

                    if (playerName.toLowerCase().startsWith(partialName)) {
                        completions.add(playerName);
                    }
                }
            } catch (SQLException e) {
                Vubex_proxy.getInstance().getLogger().log(Level.SEVERE, "Ein Fehler ist aufgetreten, während Spieler UUIDs von der Datenbank Users abgerufen wurden.", e);
            }
        }

        return completions;
    }
}