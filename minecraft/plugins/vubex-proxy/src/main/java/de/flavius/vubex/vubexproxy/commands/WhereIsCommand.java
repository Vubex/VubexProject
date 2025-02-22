package de.flavius.vubex.vubexproxy.commands;

import de.flavius.vubex.vubexproxy.Vubex_proxy;
import de.flavius.vubex.vubexproxy.api.VubexVanishAPI;
import de.flavius.vubex.vubexproxy.mysql.MySQLManager;
import de.flavius.vubex.vubexproxy.utils.ChatUtils;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author : flavius
 * project : VubexProject
 * created : 10.09.2023, Sonntag
 **/
public class WhereIsCommand extends Command implements TabExecutor {

    private final MySQLManager mySQLManager;

    public WhereIsCommand(MySQLManager mySQLManager) {
        super("whereis", null);
        this.mySQLManager = mySQLManager;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(!sender.hasPermission("vubex.whereis")){
            sender.sendMessage(new TextComponent(Vubex_proxy.serverPrefix + ChatUtils.getColoredText("&7Diesen &7Befehl &7darfst &7du &7nicht &7ausführen.")));
            return;
        }

        if (args.length != 1) {
            sender.sendMessage(new TextComponent(Vubex_proxy.serverPrefix + ChatUtils.getColoredText("&7Verwendung: &e/whereis [Spieler]")));
            return;
        }

        ProxiedPlayer targetPlayer = Vubex_proxy.getInstance().getProxy().getPlayer(args[0]);

        if (targetPlayer != null) {
            String serverName = targetPlayer.getServer().getInfo().getName();
            sender.sendMessage(new TextComponent(Vubex_proxy.serverPrefix + ChatUtils.getColoredText("&e" + ChatUtils.getOfflinePlayerName(targetPlayer.getUniqueId()) + " &7ist &7auf &7dem &7Server: &e" + serverName + "&7.")));
        } else {
            UUID convertCaseRight_PlayerUuid = ChatUtils.getUUIDFromPlayerName(args[0]);
            if (convertCaseRight_PlayerUuid != null) {
                String convertCaseRight_PlayerName = ChatUtils.getOfflinePlayerName(convertCaseRight_PlayerUuid);
                sender.sendMessage(new TextComponent(Vubex_proxy.serverPrefix + ChatUtils.getColoredText("&7Der Spieler &e" + convertCaseRight_PlayerName + " &7ist &7nicht &7online.")));
            }else {
                sender.sendMessage(new TextComponent(Vubex_proxy.serverPrefix + ChatUtils.getColoredText("&7Der Spieler &e" + args[0] + " &7existiert &7nicht.")));
            }
        }
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        List<String> completions = new ArrayList<>();
        if(sender.hasPermission("vubex.whereis")){
            VubexVanishAPI vanishAPI = new VubexVanishAPI(mySQLManager);

            if (args.length == 1) {
                String partialName = args[0].toLowerCase();

                for (ProxiedPlayer player : ProxyServer.getInstance().getPlayers()) {
                    String playerName = player.getName().toLowerCase();
                    boolean isVanished = vanishAPI.getVanishStatus(player.getUniqueId());

                    if ((sender.hasPermission(VubexVanishAPI.VANISH_SEE_PERMISSION) || !isVanished) && playerName.startsWith(partialName)) {
                        completions.add(player.getName());
                    }
                }
            }
        }

        return completions;
    }
}